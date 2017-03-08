package edu.cornell.kfs.concur.batch;

/**
 * Base class encapsulating summary information related to the extract
 * of a Concur SAE file to another KFS file format.
 */
// TODO: This class needs to be modified by KFSPTS-8040 to conform to the specs!
public abstract class ConcurStandardAccountingExtractSummaryBase {

    protected boolean fileProcessingSucceeded;
    protected String filePath;
    protected String failureReason;

    public boolean isFileProcessingSucceeded() {
        return fileProcessingSucceeded;
    }

    public void setFileProcessingSucceeded(boolean fileProcessingSucceeded) {
        this.fileProcessingSucceeded = fileProcessingSucceeded;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
