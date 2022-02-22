package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.KFSConstants;

import org.kuali.kfs.krad.util.ObjectUtils;

public class CreateAccountingDocumentReportItemDetail {

    private int indexNumber;
    private String documentType;
    private String documentDescription;
    private String documentExplanation;
    private String documentNumber;
    private String errorMessage;
    private boolean successfullyRouted;
    private boolean rawDataValidationError;
    private String rawDocumentData;
    private String warningMessage;

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

    public String getDocumentExplanation() {
        return documentExplanation;
    }

    public void setDocumentExplanation(String documentExplanation) {
        this.documentExplanation = documentExplanation;
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

    public boolean isRawDataValidationError() {
        return rawDataValidationError;
    }
    
    public boolean isNotRawDataValidationError() {
        return !rawDataValidationError;
    }

    public void setRawDataValidationError(boolean rawDataValidationError) {
        this.rawDataValidationError = rawDataValidationError;
    }

    public String getRawDocumentData() {
        return rawDocumentData;
    }

    public void setRawDocumentData(String rawDocumentData) {
        this.rawDocumentData = rawDocumentData;
    }
    
    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public void appendErrorMessageToExistingErrorMessage(String additionalErrorMessageContent) {
        this.setErrorMessage((ObjectUtils.isNotNull(this.getErrorMessage()) ? this.getErrorMessage() : KFSConstants.EMPTY_STRING)
                + KFSConstants.NEWLINE + KFSConstants.NEWLINE + additionalErrorMessageContent);
         
    }
    
    public void appendWarningrMessageToExistingWarningMessage(String additionalWarningMessageContent) {
        setWarningMessage((ObjectUtils.isNotNull(warningMessage) ? warningMessage : KFSConstants.EMPTY_STRING)
                + KFSConstants.NEWLINE + KFSConstants.NEWLINE + additionalWarningMessageContent);
         
    }

}
