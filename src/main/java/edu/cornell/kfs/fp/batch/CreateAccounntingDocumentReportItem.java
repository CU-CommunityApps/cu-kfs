package edu.cornell.kfs.fp.batch;

import java.util.ArrayList;
import java.util.List;

public class CreateAccounntingDocumentReportItem {
    
    private String xmlFileName;
    private boolean xmlSuccessfullyLoaded;
    private String reportEmailAddress;
    private int numberOfDocumentInFile;
    private String reportItemMessage;
    private String fileOverview;
    private List<CreateAccounntingDocumentReportItemDetail> documentsInError;
    private List<CreateAccounntingDocumentReportItemDetail> documentsSuccessfullyRouted;
    
    public CreateAccounntingDocumentReportItem(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        documentsInError = new ArrayList<CreateAccounntingDocumentReportItemDetail>();
        documentsSuccessfullyRouted = new ArrayList<CreateAccounntingDocumentReportItemDetail>();
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

    public List<CreateAccounntingDocumentReportItemDetail> getDocumentsInError() {
        return documentsInError;
    }

    public void setDocumentsInError(List<CreateAccounntingDocumentReportItemDetail> documentsInError) {
        this.documentsInError = documentsInError;
    }

    public String getReportItemMessage() {
        return reportItemMessage;
    }

    public void setReportItemMessage(String reportItemMessage) {
        this.reportItemMessage = reportItemMessage;
    }

    public List<CreateAccounntingDocumentReportItemDetail> getDocumentsSuccessfullyRouted() {
        return documentsSuccessfullyRouted;
    }

    public void setDocumentsSuccessfullyRouted(
            List<CreateAccounntingDocumentReportItemDetail> documentsSuccessfullyRouted) {
        this.documentsSuccessfullyRouted = documentsSuccessfullyRouted;
    }

    public String getFileOverview() {
        return fileOverview;
    }

    public void setFileOverview(String fileOverview) {
        this.fileOverview = fileOverview;
    }
}
