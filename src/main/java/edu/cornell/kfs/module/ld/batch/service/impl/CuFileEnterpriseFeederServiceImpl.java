package edu.cornell.kfs.module.ld.batch.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.gl.batch.service.impl.RequiredFilesMissingStatus;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.batch.service.impl.FileEnterpriseFeederServiceImpl;
import org.kuali.kfs.module.ld.report.EnterpriseFeederReportData;
import org.kuali.kfs.sys.Message;

public class CuFileEnterpriseFeederServiceImpl extends FileEnterpriseFeederServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuFileEnterpriseFeederServiceImpl.class);

    @Override
    public void feed(String processName, boolean performNotifications) {
        // ensure that this feeder implementation may not be run concurrently on this JVM
        // to consider: maybe use java NIO classes to perform done file locking?
        synchronized (this) {
            if (StringUtils.isBlank(directoryName)) {
                throw new IllegalArgumentException("directoryName not set for FileEnterpriseFeederServiceImpl.");
            }
            FileFilter doneFileFilter = new SuffixFileFilter(DONE_FILE_SUFFIX);

            File enterpriseFeedFile = null;
            String enterpriseFeedFileName = LaborConstants.BatchFileSystem.LABOR_ENTERPRISE_FEED + LaborConstants.BatchFileSystem.EXTENSION;
            enterpriseFeedFile = new File(laborOriginEntryDirectoryName + File.separator + enterpriseFeedFileName);

			PrintStream enterpriseFeedPs = null;
			try {
				enterpriseFeedPs = new PrintStream(enterpriseFeedFile);
			}
			catch (FileNotFoundException e) {
				LOG.error("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
				throw new RuntimeException("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
			}

			if ( LOG.isInfoEnabled() ) {
			    LOG.info("New File created for enterprise feeder service run: " + enterpriseFeedFileName);
			}

			File directory = new File(directoryName);
			if (!directory.exists() || !directory.isDirectory()) {
				LOG.error("Directory doesn't exist and or it's not really a directory " + directoryName);
				throw new RuntimeException("Directory doesn't exist and or it's not really a directory " + directoryName);
			}

			File[] doneFiles = directory.listFiles(doneFileFilter);
			reorderDoneFiles(doneFiles);
			boolean fatal = false;
			
			LedgerSummaryReport ledgerSummaryReport = new LedgerSummaryReport();

			//  keeps track of statistics for reporting
			EnterpriseFeederReportData feederReportData = new EnterpriseFeederReportData();

			List<EnterpriseFeederStatusAndErrorMessagesWrapper> statusAndErrorsList = new ArrayList<EnterpriseFeederStatusAndErrorMessagesWrapper>();

			for (File doneFile : doneFiles) {
				File dataFile = null;
				File reconFile = null;

				EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors = new EnterpriseFeederStatusAndErrorMessagesWrapper();
				statusAndErrors.setErrorMessages(new ArrayList<Message>());

				dataFile = getDataFile(doneFile);
				reconFile = getReconFile(doneFile);

				statusAndErrors.setFileNames(dataFile, reconFile, doneFile);

				if (dataFile == null) {
					LOG.error("Unable to find data file for done file: " + doneFile.getAbsolutePath());
					statusAndErrors.getErrorMessages().add(
							new Message("Unable to find data file for done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
					statusAndErrors.setStatus(new RequiredFilesMissingStatus());
				}

				if (reconFile == null) {
					LOG.error("Unable to find recon file for done file: " + doneFile.getAbsolutePath());
					statusAndErrors.getErrorMessages().add(
							new Message("Unable to find recon file for done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
					statusAndErrors.setStatus(new RequiredFilesMissingStatus());
				}

				try {
					if (dataFile != null && reconFile != null) {
					    if ( LOG.isInfoEnabled() ) {
					        LOG.info("Data file: " + dataFile.getAbsolutePath());
	                        LOG.info("Reconciliation File: " + reconFile.getAbsolutePath());
					    }

						fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFile, reconFile, enterpriseFeedPs, processName,
								reconciliationTableId, statusAndErrors, ledgerSummaryReport, errorStatisticsReport, feederReportData);
					}
				}
				catch (RuntimeException e) {
					// we need to be extremely resistant to a file load failing so that it doesn't prevent other files from loading
					LOG.error("Caught exception when feeding done file: " + doneFile.getAbsolutePath());
					fatal = true;
				}
				finally {
					statusAndErrorsList.add(statusAndErrors);
					boolean doneFileDeleted = doneFile.delete();
					if (!doneFileDeleted) {
						statusAndErrors.getErrorMessages().add(
								new Message("Unable to delete done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
					}
					if (performNotifications) {
						enterpriseFeederNotificationService.notifyFileFeedStatus(processName, statusAndErrors.getStatus(), doneFile, dataFile,
								reconFile, statusAndErrors.getErrorMessages());
					}
				}
			}

			enterpriseFeedPs.close();

			// if errors encountered is greater than max allowed the enterprise feed file should not be sent
			boolean enterpriseFeedFileCreated = false;
			if (feederReportData.getNumberOfErrorEncountered() > getMaximumNumberOfErrorsAllowed() || fatal) {
				enterpriseFeedFile.delete();
			}
			else {
				// generate done file
				String enterpriseFeedDoneFileName = enterpriseFeedFileName.replace(
						LaborConstants.BatchFileSystem.EXTENSION, LaborConstants.BatchFileSystem.DONE_FILE_EXTENSION);
				File enterpriseFeedDoneFile = new File(laborOriginEntryDirectoryName + File.separator
						+ enterpriseFeedDoneFileName);
				if (!enterpriseFeedDoneFile.exists()) {
					try {
						enterpriseFeedDoneFile.createNewFile();
					}
					catch (IOException e) {
						LOG.error("Unable to create done file for enterprise feed output group.", e);
						throw new RuntimeException("Unable to create done file for enterprise feed output group.", e);
					}
				}

				enterpriseFeedFileCreated = true;
			}

			// write out totals to log file
			if ( LOG.isInfoEnabled() ) {
	            LOG.info("Total records read: " + feederReportData.getNumberOfRecordsRead());
	            LOG.info("Total amount read: " + feederReportData.getTotalAmountRead());
	            LOG.info("Total records written: " + feederReportData.getNumberOfRecordsRead());
	            LOG.info("Total amount written: " + feederReportData.getTotalAmountWritten());
			}

			generateReport(enterpriseFeedFileCreated, feederReportData, statusAndErrorsList, ledgerSummaryReport,
					laborOriginEntryDirectoryName + File.separator + enterpriseFeedFileName);
        }
    }
}
