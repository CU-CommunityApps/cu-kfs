package edu.cornell.kfs.module.ld.batch.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.service.impl.RequiredFilesMissingStatus;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.batch.service.impl.FileEnterpriseFeederServiceImpl;
import org.kuali.kfs.module.ld.report.EnterpriseFeederReportData;
import org.kuali.kfs.sys.Message;

public class CuFileEnterpriseFeederServiceImpl extends FileEnterpriseFeederServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public void feed(final String processName, final boolean performNotifications) {
        // ensure that this feeder implementation may not be run concurrently on this JVM
        // to consider: maybe use java NIO classes to perform done file locking?
        synchronized (this) {
            if (StringUtils.isBlank(directoryName)) {
                throw new IllegalArgumentException("directoryName not set for FileEnterpriseFeederServiceImpl.");
            }
            final FileFilter doneFileFilter = new SuffixFileFilter(GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);

            File enterpriseFeedFile = null;
            final String enterpriseFeedFileName = LaborConstants.BatchFileSystem.LABOR_ENTERPRISE_FEED + LaborConstants.BatchFileSystem.EXTENSION;
            enterpriseFeedFile = new File(laborOriginEntryDirectoryName + File.separator + 
                                          enterpriseFeedFileName);

            LOG.info("New File created for enterprise feeder service run: {}", enterpriseFeedFileName);

            final File directory = new File(directoryName);
            if (!directory.exists() || !directory.isDirectory()) {
                LOG.error("Directory doesn't exist and or it's not really a directory {}", directoryName);
                throw new RuntimeException("Directory doesn't exist and or it's not really a directory " + directoryName);
            }

            final File[] doneFiles = directory.listFiles(doneFileFilter);
            reorderDoneFiles(doneFiles);
            boolean fatal = false;
            
            final LedgerSummaryReport ledgerSummaryReport = new LedgerSummaryReport();

            //  keeps track of statistics for reporting
            final EnterpriseFeederReportData feederReportData = new EnterpriseFeederReportData();

            final List<EnterpriseFeederStatusAndErrorMessagesWrapper> statusAndErrorsList = new ArrayList<EnterpriseFeederStatusAndErrorMessagesWrapper>();

            try (PrintStream enterpriseFeedPs = new PrintStream(enterpriseFeedFile, StandardCharsets.UTF_8)) {
                for (final File doneFile : doneFiles) {
                    final EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors =
                            new EnterpriseFeederStatusAndErrorMessagesWrapper();
                    statusAndErrors.setErrorMessages(new ArrayList<>());

                    final File dataFile = getDataFile(doneFile);
                    final File reconFile = getReconFile(doneFile);
    
                    statusAndErrors.setFileNames(dataFile, reconFile, doneFile);
    
                    if (dataFile == null) {
                        LOG.error("Unable to find data file for done file: {}", doneFile::getAbsolutePath);
                        statusAndErrors.getErrorMessages().add(
                            new Message("Unable to find data file for done file: " + doneFile.getAbsolutePath(),
                                    Message.TYPE_FATAL));
                        statusAndErrors.setStatus(new RequiredFilesMissingStatus());
                    }
    
                    if (reconFile == null) {
                        LOG.error("Unable to find recon file for done file: {}", doneFile::getAbsolutePath);
                        statusAndErrors.getErrorMessages().add(
                            new Message("Unable to find recon file for done file: " + doneFile.getAbsolutePath(),
                                    Message.TYPE_FATAL));
                        statusAndErrors.setStatus(new RequiredFilesMissingStatus());
                    }
    
                    try {
                        if (dataFile != null && reconFile != null) {
                            LOG.info("Data file: {}", dataFile::getAbsolutePath);
                            LOG.info("Reconciliation File: {}", reconFile::getAbsolutePath);
    
                            fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFile, reconFile, enterpriseFeedPs,
                                    processName, reconciliationTableId, statusAndErrors, ledgerSummaryReport,
                                    errorStatisticsReport, feederReportData);
                        }
                    } catch (final RuntimeException e) {
                        // we need to be extremely resistant to a file load failing so that it doesn't prevent other files
                        // from loading
                        LOG.error("Caught exception when feeding done file: {}", doneFile::getAbsolutePath);
                        fatal = true;
                    } finally {
                        statusAndErrorsList.add(statusAndErrors);
                        final boolean doneFileDeleted = doneFile.delete();
                        if (!doneFileDeleted) {
                            statusAndErrors.getErrorMessages().add(
                                new Message("Unable to delete done file: " + doneFile.getAbsolutePath(),
                                        Message.TYPE_FATAL));
                        }
                        if (performNotifications) {
                            enterpriseFeederNotificationService.notifyFileFeedStatus(processName,
                                    statusAndErrors.getStatus(), doneFile, dataFile, reconFile,
                                    statusAndErrors.getErrorMessages());
                        }
                    }
                }
            } catch (final IOException e) {
                LOG.error("enterpriseFeedFile doesn't exist {}", enterpriseFeedFileName);
                throw new RuntimeException("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
            }

            // if errors encountered is greater than max allowed the enterprise feed file should not be sent
            boolean enterpriseFeedFileCreated = false;
            if (feederReportData.getNumberOfErrorEncountered() > getMaximumNumberOfErrorsAllowed() || fatal) {
                enterpriseFeedFile.delete();
            }
            else {
                // generate done file
                final String enterpriseFeedDoneFileName = enterpriseFeedFileName.replace(
                        LaborConstants.BatchFileSystem.EXTENSION, LaborConstants.BatchFileSystem.DONE_FILE_EXTENSION);
                final File enterpriseFeedDoneFile = new File(laborOriginEntryDirectoryName + File.separator
                        + enterpriseFeedDoneFileName);
                if (!enterpriseFeedDoneFile.exists()) {
                    try {
                        enterpriseFeedDoneFile.createNewFile();
                    }
                    catch (final IOException e) {
                        LOG.error("Unable to create done file for enterprise feed output group.", e);
                        throw new RuntimeException("Unable to create done file for enterprise feed output group.", e);
                    }
                }

                enterpriseFeedFileCreated = true;
            }

            // write out totals to log file
            LOG.info("Total records read: {}", feederReportData::getNumberOfRecordsRead);
            LOG.info("Total amount read: {}", feederReportData::getTotalAmountRead);
            LOG.info("Total records written: {}", feederReportData::getNumberOfRecordsRead);
            LOG.info("Total amount written: {}", feederReportData::getTotalAmountWritten);

            generateReport(enterpriseFeedFileCreated, feederReportData, statusAndErrorsList, ledgerSummaryReport,
                    laborOriginEntryDirectoryName + File.separator + enterpriseFeedFileName);
        }
    }
}
