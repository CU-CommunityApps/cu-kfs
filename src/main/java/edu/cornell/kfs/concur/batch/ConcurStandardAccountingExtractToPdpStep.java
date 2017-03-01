package edu.cornell.kfs.concur.batch;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractToPdpStep extends AbstractStep {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractToPdpStep.class);
    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    private String incomingDirectoryName;
    private String acceptedDirectoryName;
    private String rejectedDirectoryName;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        File folder = new File(incomingDirectoryName);
        File[] listOfFiles = folder.listFiles();

        if (LOG.isDebugEnabled()) {
            String numberOfFiles = listOfFiles != null ? String.valueOf(listOfFiles.length) : "NULL";
            LOG.debug("execute started, directoryPath: " + incomingDirectoryName + " number of files found to process: "
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
        if (!currentFile.isDirectory() && isTextFile(currentFile)) {
            LOG.debug("processCurrentFileAndExtractPdpFeedFromSAEFile, current File: " + currentFile.getName());
            try {
                ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
                        .parseStandardAccoutingExtractFileToStandardAccountingExtractFile(currentFile);
                debugConcurStandardAccountingExtractFile(concurStandardAccoutingExtractFile);
                success = getConcurStandardAccountingExtractService()
                        .extractPdpFeedFromStandardAccounitngExtract(concurStandardAccoutingExtractFile);
                moveFile(currentFile, true);
            } catch (ValidationException ve) {
                success = false;
                moveFile(currentFile, false);
                LOG.error("processCurrentFileAndExtractPdpFeedFromSAEFile, There was a validation error processing "
                        + currentFile.getName(), ve);
            }
        }
        return success;
    }
    
    protected boolean isTextFile(File file) {
        String fileName = file.getName();
        return StringUtils.endsWith(fileName, ".txt");
    }
    
    protected void debugConcurStandardAccountingExtractFile(ConcurStandardAccountingExtractFile saeFile) {
        if (LOG.isDebugEnabled()) {
            if (saeFile != null) {
                LOG.debug("debugConcurStandardAccountingExtractFile, " + saeFile.getDebugInformation());
                if (saeFile.getConcurStandardAccountingExtractDetailLines() != null) {
                    LOG.debug("debugConcurStandardAccountingExtractFile, Number of line items: " + saeFile.getConcurStandardAccountingExtractDetailLines().size());
                    for (ConcurStandardAccountingExtractDetailLine line : saeFile.getConcurStandardAccountingExtractDetailLines()) {
                        LOG.debug("debugConcurStandardAccountingExtractFile, " + line.getDebugInformation());
                    }
                } else {
                    LOG.debug("debugConcurStandardAccountingExtractFile, The getConcurStandardAccountingExtractDetailLines is null");
                }
                
            } else {
                LOG.debug("debugConcurStandardAccountingExtractFile, The SAE file is null");
            }
        }
    }

    protected void moveFile(File currentFile, boolean accepted) {
        String moveToBaseDirectoryPath;
        if (accepted) {
            moveToBaseDirectoryPath = getAcceptedDirectoryName();
        } else {
            moveToBaseDirectoryPath = getRejectedDirectoryName();
        }

        DateFormat df = new SimpleDateFormat(ConcurConstants.CONCUR_PROCESSED_FILE_DATE_FORMAT);
        String newFileName = moveToBaseDirectoryPath + ConcurConstants.FORWARD_SLASH + currentFile.getName()
                + KFSConstants.DELIMITER + df.format(Calendar.getInstance().getTimeInMillis());
        LOG.debug("moveFile, moving " + currentFile.getAbsolutePath() + " to " + newFileName);

        try {
            FileUtils.moveFile(currentFile, new File(newFileName));
        } catch (IOException e) {
            LOG.error("moveFile, unable to move file from " + currentFile.getAbsolutePath() + " to " + newFileName, e);
            throw new RuntimeException(e);
        }
    }

    public ConcurStandardAccountingExtractService getConcurStandardAccountingExtractService() {
        return concurStandardAccountingExtractService;
    }

    public String getIncomingDirectoryName() {
        return incomingDirectoryName;
    }

    public void setIncomingDirectoryName(String incomingDirectoryName) {
        this.incomingDirectoryName = incomingDirectoryName;
    }

    public String getAcceptedDirectoryName() {
        return acceptedDirectoryName;
    }

    public void setAcceptedDirectoryName(String acceptedDirectoryName) {
        this.acceptedDirectoryName = acceptedDirectoryName;
    }

    public String getRejectedDirectoryName() {
        return rejectedDirectoryName;
    }

    public void setRejectedDirectoryName(String rejectedDirectoryName) {
        this.rejectedDirectoryName = rejectedDirectoryName;
    }

    public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
        this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
    }

}
