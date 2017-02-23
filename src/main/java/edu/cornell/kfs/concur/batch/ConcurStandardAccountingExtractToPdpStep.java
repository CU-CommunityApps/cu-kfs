package edu.cornell.kfs.concur.batch;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;

import com.google.common.io.Files;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractToPdpStep extends AbstractStep {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
	        .getLogger(ConcurStandardAccountingExtractToPdpStep.class);
	protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
	private String directoryPath;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		File folder = new File(directoryPath);
		File[] listOfFiles = folder.listFiles();

		if (LOG.isDebugEnabled()) {
			String numberOfFiles = listOfFiles != null ? String.valueOf(listOfFiles.length) : "NULL";
			LOG.debug("execute started, directoryPath: " + directoryPath + " number of files found to process: "
			        + numberOfFiles);
		}

		boolean success = true;
		for (int i = 0; i < listOfFiles.length; i++) {
			File currentFile = listOfFiles[i];
			success = processCurrentFileAndExtractPdpFeedFromSAEFile(currentFile) && success;
		}

		return success;
	}

	protected boolean processCurrentFileAndExtractPdpFeedFromSAEFile(File currentFile) {
		boolean success = true;
		if (!currentFile.isDirectory()) {
			LOG.debug("processCurrentFileAndExtractPdpFeedFromSAEFile, current File: " + currentFile.getName());
			try {
				ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
						.parseStandardAccoutingExtractFileToStandardAccountingExtractFile(currentFile);
				success = getConcurStandardAccountingExtractService().extractPdpFeedFromStandardAccounitngExtract(concurStandardAccoutingExtractFile);
				moveFile(currentFile, ConcurConstants.ACCEPT_SUB_FOLDER_NAME);
			} catch (ValidationException ve) {
				success = false;
				moveFile(currentFile, ConcurConstants.REJECT_SUB_FOLDER_NAME);
				LOG.error("processCurrentFileAndExtractPdpFeedFromSAEFile, There was a validation error processing " + currentFile.getName(), ve);
			}
		}
		return success;
	}

	protected void moveFile(File currentFile, String subPath) {
		File dropDirectory = new File(directoryPath + subPath + ConcurConstants.FORWARD_SLASH);
		if (!dropDirectory.exists()) {
			dropDirectory.mkdir();
		}

		DateFormat df = new SimpleDateFormat(ConcurConstants.CONCUR_PROCESSED_FILE_DATE_FORMAT);
		String newFileName = dropDirectory.getAbsolutePath() + ConcurConstants.FORWARD_SLASH + currentFile.getName() + KFSConstants.DELIMITER
		        + df.format(Calendar.getInstance().getTimeInMillis());
		LOG.debug("moveFile, moving " + currentFile.getAbsolutePath() + " to " + newFileName);

		try {
			Files.move(currentFile, new File(newFileName));
		} catch (IOException e) {
			LOG.error("moveFile, unable to move file from " + currentFile.getAbsolutePath() + " to " + newFileName, e);
			throw new RuntimeException(e);
		}
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public ConcurStandardAccountingExtractService getConcurStandardAccountingExtractService() {
		return concurStandardAccountingExtractService;
	}

	public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
		this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
	}

}
