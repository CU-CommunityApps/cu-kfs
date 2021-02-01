package edu.cornell.kfs.pdp.batch;

public class PayeeACHAccountExtractFileResult extends PayeeACHAccountExtractResult {
    private String fileName;

    public PayeeACHAccountExtractFileResult(String fileName) {
        super();
        this.fileName = fileName;

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
