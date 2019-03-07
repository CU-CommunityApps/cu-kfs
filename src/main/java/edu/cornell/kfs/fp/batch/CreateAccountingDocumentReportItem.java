package edu.cornell.kfs.fp.batch;

import java.util.ArrayList;
import java.util.List;

public class CreateAccountingDocumentReportItem {

    private String xmlFileName;
    private boolean xmlSuccessfullyLoaded;
    private boolean nonBusinessRuleFailure;
    private String reportEmailAddress;
    private int numberOfDocumentInFile;
    private String reportItemMessage;
    private String fileOverview;
    private List<CreateAccountingDocumentReportItemDetail> documentsInError;
    private List<CreateAccountingDocumentReportItemDetail> documentsSuccessfullyRouted;
    private String validationErrorMessage;

    public CreateAccountingDocumentReportItem(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        documentsInError = new ArrayList<CreateAccountingDocumentReportItemDetail>();
        documentsSuccessfullyRouted = new ArrayList<CreateAccountingDocumentReportItemDetail>();
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public boolean isXmlSuccessfullyLoaded() {
        return xmlSuccessfullyLoaded;
    }

    public void setXmlSuccessfullyLoaded(boolean xmlSuccessfullyLoaded) {
        this.xmlSuccessfullyLoaded = xmlSuccessfullyLoaded;
    }

    public String getReportEmailAddress() {
        return reportEmailAddress;
    }

    public void setReportEmailAddress(String reportEmailAddress) {
        this.reportEmailAddress = reportEmailAddress;
    }

    public int getNumberOfDocumentInFile() {
        return numberOfDocumentInFile;
    }

    public void setNumberOfDocumentInFile(int numberOfDocumentInFile) {
        this.numberOfDocumentInFile = numberOfDocumentInFile;
    }

    public List<CreateAccountingDocumentReportItemDetail> getDocumentsInError() {
        return documentsInError;
    }

    public void setDocumentsInError(List<CreateAccountingDocumentReportItemDetail> documentsInError) {
        this.documentsInError = documentsInError;
    }

    public String getReportItemMessage() {
        return reportItemMessage;
    }

    public void setReportItemMessage(String reportItemMessage) {
        this.reportItemMessage = reportItemMessage;
    }

    public List<CreateAccountingDocumentReportItemDetail> getDocumentsSuccessfullyRouted() {
        return documentsSuccessfullyRouted;
    }

    public void setDocumentsSuccessfullyRouted(List<CreateAccountingDocumentReportItemDetail> documentsSuccessfullyRouted) {
        this.documentsSuccessfullyRouted = documentsSuccessfullyRouted;
    }

    public String getFileOverview() {
        return fileOverview;
    }

    public void setFileOverview(String fileOverview) {
        this.fileOverview = fileOverview;
    }

	public boolean isNonBusinessRuleFailure() {
		return nonBusinessRuleFailure;
	}

	public void setNonBusinessRuleFailure(boolean nonBusinessRuleFailure) {
		this.nonBusinessRuleFailure = nonBusinessRuleFailure;
	}

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }
}
