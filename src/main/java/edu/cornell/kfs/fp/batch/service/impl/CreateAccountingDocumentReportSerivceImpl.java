package edu.cornell.kfs.fp.batch.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.batch.CreateAccounntingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccounntingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportSerivce;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CreateAccountingDocumentReportSerivceImpl implements CreateAccountingDocumentReportSerivce {
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void generateReport(CreateAccounntingDocumentReportItem reportItem) {
        String newTitle = reportWriterService.getTitle() + reportItem.getXmlFileName();
        reportWriterService.setTitle(newTitle);
        reportWriterService.initialize();
        if (reportItem.isXmlSuccessfullyLoaded()) {
            generateSummary(reportItem);
            reportWriterService.writeNewLines(2);
            generateErredDocumentSection(reportItem.getDocumentsInError());
            reportWriterService.writeNewLines(2);
            generateSuccessDocumentSection(reportItem.getDocumentsSuccessfullyRouted());
        } else {
            reportWriterService.writeMultipleFormattedMessageLines("Unable to process file: " + reportItem.getReportItemMessage());
        }
        reportWriterService.destroy();
    }
    
    private void generateSummary(CreateAccounntingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle("**** Summary ****");
        reportWriterService.writeFormattedMessageLine("File Name: " + reportItem.getXmlFileName());
        reportWriterService.writeFormattedMessageLine("Report Email Address: " + reportItem.getReportEmailAddress());
        reportWriterService.writeFormattedMessageLine("File Overview: " + reportItem.getReportEmailAddress());
        reportWriterService.writeFormattedMessageLine("Number of documents processed in this file: " + reportItem.getNumberOfDocumentInFile());
        reportWriterService.writeFormattedMessageLine("Number of documents successfully routed: " + reportItem.getDocumentsSuccessfullyRouted().size());
        reportWriterService.writeFormattedMessageLine("Number of documents that could not be saved: " + reportItem.getDocumentsInError().size());
    }
    
    private void generateErredDocumentSection(List<CreateAccounntingDocumentReportItemDetail> documentsInError) {
        reportWriterService.writeSubTitle("**** Documents in Error ****");
        if (CollectionUtils.isNotEmpty(documentsInError)) {
            documentsInError.stream().forEach(detail -> generateErrorDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine("There are no documents in error.");
        }
    }
    
    private void generateSuccessDocumentSection(List<CreateAccounntingDocumentReportItemDetail> successDocuments) {
        reportWriterService.writeSubTitle("**** Documents successfully routed ****");
        if (CollectionUtils.isNotEmpty(successDocuments)) {
            successDocuments.stream().forEach(detail -> generateSuccessDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine("There are no documents successfully routed.");
        }
    }
    
    private void generateErrorDetail(CreateAccounntingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine("Error Messages: " + detail.getErrorMessage());
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSuccessDetail(CreateAccounntingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine("Document number: " + detail.getDocumentNumber());
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSharedDetails(CreateAccounntingDocumentReportItemDetail detail) {
        reportWriterService.writeFormattedMessageLine("Index number: " + detail.getIndexNumber());
        reportWriterService.writeFormattedMessageLine("Document Type: " + detail.getDocumentType());
        reportWriterService.writeFormattedMessageLine("Document Description: " + detail.getDocumentDescription());
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

}
