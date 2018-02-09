package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.CuFPKeyConstants;
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
        reportWriterService.initialize();
        if (reportItem.isXmlSuccessfullyLoaded()) {
            generateSummary(reportItem);
            reportWriterService.writeNewLines(2);
            generateErredDocumentSection(reportItem.getDocumentsInError());
            reportWriterService.writeNewLines(2);
            generateSuccessDocumentSection(reportItem.getDocumentsSuccessfullyRouted());
        } else {
            writeFileName(reportItem);
            reportWriterService.writeMultipleFormattedMessageLines(fomratString(
                    configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_ERROR), 
                    reportItem.getReportItemMessage()));
        }
        reportWriterService.destroy();
    }
    
    private void generateSummary(CreateAccounntingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_SUB_HEADER));
        
        writeFileName(reportItem);
        
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_REPORT_EMAIL), reportItem.getReportEmailAddress()));
        
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_OVERVIEW_FILE), reportItem.getFileOverview()));
        
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENT_PROCESSED), reportItem.getNumberOfDocumentInFile()));
        
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENTS_SUCCESSFULLY_ROUTED), 
                reportItem.getDocumentsSuccessfullyRouted().size()));
        
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENTS_NOT_SAVED), reportItem.getDocumentsInError().size()));
    }

    protected void writeFileName(CreateAccounntingDocumentReportItem reportItem) {
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_FILE_NAME), reportItem.getXmlFileName()));
    }
    
    private void generateErredDocumentSection(List<CreateAccounntingDocumentReportItemDetail> documentsInError) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_SUB_HEADER));
        if (CollectionUtils.isNotEmpty(documentsInError)) {
            documentsInError.stream().forEach(detail -> generateErrorDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_NONE));
        }
    }
    
    private void generateSuccessDocumentSection(List<CreateAccounntingDocumentReportItemDetail> successDocuments) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_SUB_HEADER));
        if (CollectionUtils.isNotEmpty(successDocuments)) {
            successDocuments.stream().forEach(detail -> generateSuccessDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_NONE));
        }
    }
    
    private void generateErrorDetail(CreateAccounntingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_MESSAGE), detail.getErrorMessage()));
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSuccessDetail(CreateAccounntingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_DOCUMENT_NUMBER), detail.getDocumentNumber()));
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSharedDetails(CreateAccounntingDocumentReportItemDetail detail) {
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_INDEX_NUMBER), detail.getIndexNumber()));
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_DOCUMENT_TYPE), detail.getDocumentType()));
        reportWriterService.writeFormattedMessageLine(fomratString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_DOCUMENT_DESCRIPTION), detail.getDocumentDescription()));
    }
    
    public String fomratString(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
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
