package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CreateAccountingDocumentReportServiceImpl implements CreateAccountingDocumentReportService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateAccountingDocumentReportServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.initialize();
        if (reportItem.isXmlSuccessfullyLoaded()) {
            generateSummary(reportItem);
            reportWriterService.writeNewLines(2);
            generateErredDocumentSection(reportItem.getDocumentsInError());
            reportWriterService.writeNewLines(2);
            generateSuccessDocumentSection(reportItem.getDocumentsSuccessfullyRouted());
        } else {
            writeFileName(reportItem);
            reportWriterService.writeMultipleFormattedMessageLines(formatString(
                    configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_ERROR), 
                    reportItem.getReportItemMessage()));
        }
        reportWriterService.destroy();
    }
    
    private void generateSummary(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_SUB_HEADER));
        
        writeFileName(reportItem);
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_REPORT_EMAIL), reportItem.getReportEmailAddress()));
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_OVERVIEW_FILE), reportItem.getFileOverview()));
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENT_PROCESSED), reportItem.getNumberOfDocumentInFile()));
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENTS_SUCCESSFULLY_ROUTED), 
                reportItem.getDocumentsSuccessfullyRouted().size()));
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENTS_NOT_SAVED), reportItem.getDocumentsInError().size()));
    }

    protected void writeFileName(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_FILE_NAME), reportItem.getXmlFileName()));
    }
    
    private void generateErredDocumentSection(List<CreateAccountingDocumentReportItemDetail> documentsInError) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_SUB_HEADER));
        if (CollectionUtils.isNotEmpty(documentsInError)) {
            documentsInError.stream().forEach(detail -> generateErrorDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_NONE));
        }
    }
    
    private void generateSuccessDocumentSection(List<CreateAccountingDocumentReportItemDetail> successDocuments) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_SUB_HEADER));
        if (CollectionUtils.isNotEmpty(successDocuments)) {
            successDocuments.stream().forEach(detail -> generateSuccessDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_NONE));
        }
    }
    
    private void generateErrorDetail(CreateAccountingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_DOCUMENTS_MESSAGE), detail.getErrorMessage()));
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSuccessDetail(CreateAccountingDocumentReportItemDetail detail) {
        generateSharedDetails(detail);
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ROUTED_DOCUMENTS_DOCUMENT_NUMBER), detail.getDocumentNumber()));
        reportWriterService.writeNewLines(1);
    }
    
    private void generateSharedDetails(CreateAccountingDocumentReportItemDetail detail) {
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_INDEX_NUMBER), detail.getIndexNumber()));
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_DOCUMENT_TYPE), detail.getDocumentType()));
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_DOCUMENT_DESCRIPTION), detail.getDocumentDescription()));
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SHARED_DOCUMENT_EXPLANATION), detail.getDocumentExplanation()));
    }
    
    public String formatString(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
    }
    
    @Override
    public void sendReportEmail(String toAddress, String fromAddress) {
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        String subject = reportWriterService.getTitle();
        message.setSubject(subject);
        message.getToAddresses().add(toAddress);
        String body = concurBatchUtilityService.getFileContents(reportWriterService.getReportFile().getAbsolutePath());
        message.setMessage(body);

        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddress);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        try {
            emailService.sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }
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
