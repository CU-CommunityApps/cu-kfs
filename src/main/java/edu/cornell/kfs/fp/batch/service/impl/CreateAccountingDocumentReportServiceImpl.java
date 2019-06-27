package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
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
	private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentReportServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.initialize();
        if (reportItem.isNonBusinessRuleFailure() 
                && (ObjectUtils.isNotNull(reportItem.getValidationErrorMessage()) && StringUtils.isNotBlank(reportItem.getValidationErrorMessage()))) {
            LOG.info("generateReport: generateFileFailureDueToHeaderValidationErrorSummary request was issued.");
            generateFileFailureDueToHeaderValidationErrorSummary(reportItem);
        } else {
            if (reportItem.isNonBusinessRuleFailure()) {
                LOG.info("generateReport: generateFileFailureSummary request was issued.");
                generateFileFailureSummary(reportItem);
            } else {
                LOG.info("generateReport: generateFileProcessingSummary request was issued.");
                generateFileProcessingSummary(reportItem);
            }
        }
        reportWriterService.destroy();
    }
    
    private void generateFileProcessingSummary(CreateAccountingDocumentReportItem reportItem) {
        generateSummary(reportItem);
        reportWriterService.writeNewLines(2);
        generateErredDocumentSection(reportItem);
        reportWriterService.writeNewLines(2);
        generateSuccessDocumentSection(reportItem.getDocumentsSuccessfullyRouted());
    }
    
    private void generateFileFailureDueToHeaderValidationErrorSummary(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILURE_SUMMARY_SUB_HEADER));

        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILURE_SUMMARY_REPORT_ITEM_MESSAGE), reportItem.getReportItemMessage()));
        
        reportWriterService.writeNewLines(1);
        writeFileName(reportItem);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeFormattedMessageLine(reportItem.getValidationErrorMessage());
    }
    
    private void generateFileFailureSummary(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILURE_SUMMARY_SUB_HEADER));
        
        writeFileName(reportItem);
        
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILURE_SUMMARY_REPORT_ITEM_MESSAGE), reportItem.getReportItemMessage()));
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
    
    private void generateErredDocumentSection(CreateAccountingDocumentReportItem reportItem) {
        generateErredRawDataValidationDocumentSection(reportItem);
        reportWriterService.writeNewLines(2);
        generateErredBusinessRuleDocumentsSection(reportItem);
    }
    
    private void generateErredRawDataValidationDocumentSection(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_VALIDATION_SUB_HEADER));
        
        List<CreateAccountingDocumentReportItemDetail> documentsInError = 
                reportItem.getDocumentsInError().stream().filter(detail -> detail.isRawDataValidationError()).collect(Collectors.toList());
        
        if (CollectionUtils.isNotEmpty(documentsInError)) {
            documentsInError.stream().forEach(detail -> generateValidationErrorDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_VALIDATION_DOCUMENTS_NONE));
        }
    }
    
    private void generateErredBusinessRuleDocumentsSection(CreateAccountingDocumentReportItem reportItem) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_BUSINESS_RULE_SUB_HEADER));
        
        List<CreateAccountingDocumentReportItemDetail> documentsInError = 
                reportItem.getDocumentsInError().stream().filter(detail -> detail.isNotRawDataValidationError()).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(documentsInError)) {
            documentsInError.stream().forEach(detail -> generateBusinessRuleErrorDetail(detail));
        } else {
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_BUSINESS_RULE_DOCUMENTS_NONE));
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
    
    private void generateValidationErrorDetail(CreateAccountingDocumentReportItemDetail detail) {
        if (detail.isRawDataValidationError()) {
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_VALIDATION_RAW_DATA));
            reportWriterService.writeFormattedMessageLine(detail.getRawDocumentData());
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_VALIDATION_DOCUMENTS_MESSAGE), detail.getErrorMessage()));
            reportWriterService.writeNewLines(1);
        }
    }
    
    private void generateBusinessRuleErrorDetail(CreateAccountingDocumentReportItemDetail detail) {
        if (detail.isNotRawDataValidationError()) {
            generateSharedDetails(detail);
            reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_ERRED_BUSINESS_RULE_DOCUMENTS_MESSAGE), detail.getErrorMessage()));
            reportWriterService.writeNewLines(1);
        }
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
