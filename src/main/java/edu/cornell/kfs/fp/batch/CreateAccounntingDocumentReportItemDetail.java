package edu.cornell.kfs.fp.batch;

public class CreateAccounntingDocumentReportItemDetail {
    private int indexNumber;
    private String documentType;
    private String documentDescription;
    private String documentNumber;
    private String errorMessage;
    private boolean successfullyRouted;
    public int getIndexNumber() {
        return indexNumber;
    }
    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }
    public String getDocumentType() {
        return documentType;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    public String getDocumentDescription() {
        return documentDescription;
    }
    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }
    public String getDocumentNumber() {
        return documentNumber;
    }
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public boolean isSuccessfullyRouted() {
        return successfullyRouted;
    }
    public void setSuccessfullyRouted(boolean successfullyRouted) {
        this.successfullyRouted = successfullyRouted;
    }
    

}
