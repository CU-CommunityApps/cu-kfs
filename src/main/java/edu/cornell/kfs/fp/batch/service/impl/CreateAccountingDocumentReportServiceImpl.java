package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CreateAccountingDocumentReportServiceImpl implements CreateAccountingDocumentReportService {
	private static final Logger LOG = LogManager.getLogger();

    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        String dataFilePrefix = obtainDataFileName(reportItem.getXmlFileName());
        if (StringUtils.isNotBlank(reportItem.getXmlFileName()) && StringUtils.isNotBlank(dataFilePrefix)) {
            //use data XML file name as part of report file name
            reportWriterService.initalize(dataFilePrefix);
        } else {
            //use spring configured default report file name
            reportWriterService.initialize();
        }
        if (reportItem.isNonBusinessRuleFailure() 
                && (ObjectUtils.isNotNull(reportItem.getValidationErrorMessage()) && StringUtils.isNotBlank(reportItem.getValidationErrorMessage()))) {
            LOG.info("generateReport: generateFileFailureDueToHeaderValidationErrorSummary request was issued.");
            generateFileFailureDueToHeaderValidationErrorSummary(reportItem);
        } else {
            if (reportItem.isNonBusinessRuleFailure() && StringUtils.isNotBlank(reportItem.getReportItemMessage())) {
                LOG.info("generateReport: generateFileFailureSummary request was issued.");
                generateFileFailureSummary(reportItem);
            } else {
                LOG.info("generateReport: generateFileProcessingSummary request was issued.");
                generateFileProcessingSummary(reportItem);
            }
        }
        reportWriterService.destroy();
    }
    
    /**
     * This method generates a file name prefix from the fully qualified CreateAccountingDocumentReportItem
     * xmlFileName attribute. The input parameter is assumed to contain the fully qualified directory path
     * as well as a file name and extension. This method strips off both the directory path and file extension
     * returning just the file name portion of the string.
     *
     * Example:
     *  Input parameter : /infra/work/staging/fp/accountingXmlDocument/fp_ib_netsuite_20240229_050035.xml
     *  Return value    : fp_ib_netsuite_20240229_050035
     *
     * @param xmlDataFileName
     */
    private String obtainDataFileName(String fullyQualifiedDataFile) {
        String onlyDataFileName = KFSConstants.EMPTY_STRING;

        int startingIndexOfFileExtension = fullyQualifiedDataFile.lastIndexOf(CuFPConstants.XML_FILE_EXTENSION);

        if (startingIndexOfFileExtension != -1) {
            onlyDataFileName = fullyQualifiedDataFile.substring(0, startingIndexOfFileExtension);

            int startingIndexOfLastPathSeparator = onlyDataFileName.lastIndexOf(File.separator);

            if (startingIndexOfLastPathSeparator != -1) {
                onlyDataFileName = onlyDataFileName.substring(startingIndexOfLastPathSeparator + 1);
            }
        }
        return onlyDataFileName;
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

        writeFileName(reportItem);

        reportWriterService.writeNewLines(1);
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_FILE_FAILURE_SUMMARY_REPORT_ITEM_MESSAGE), reportItem.getReportItemMessage()));

        if (!StringUtils.containsIgnoreCase(reportItem.getReportItemMessage(), reportItem.getValidationErrorMessage())) {
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine(reportItem.getValidationErrorMessage());
        }
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
        
        if (reportItem.isDuplicateFile()) {
            reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_DUPLICATE_FILE_WARNING)));
        }
        
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
        
        if (reportItem.doWarningMessagesExist()) {
            reportItem.getDocumentTypeWarningMessageCountMap().entrySet()
                .forEach(entry -> writeWarningSummaryLine(entry.getKey(), entry.getValue()));
        }
    }
    
    protected void writeWarningSummaryLine(String docType, Integer docWarningCount) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_SUMMARY_DOCUMENTS_WITH_WARNING), docType, docWarningCount));
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
            if (StringUtils.isNotBlank(detail.getWarningMessage())) {
                reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                        CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_WARNING_DOCUMENTS_MESSAGE), detail.getWarningMessage()));
                reportWriterService.writeNewLines(1);
            }
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
        if (StringUtils.isNotBlank(detail.getWarningMessage())) {
            reportWriterService.writeFormattedMessageLine(formatString(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_WARNING_DOCUMENTS_MESSAGE), detail.getWarningMessage()));
            reportWriterService.writeNewLines(1);
        }
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
    public void sendReportEmail(String fromAddress, List<String> toAddresses) {
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        String subject = reportWriterService.getTitle();
        message.setSubject(subject);
        message.getToAddresses().addAll(toAddresses);
        String body = concurBatchUtilityService.getFileContents(reportWriterService.getReportFile().getAbsolutePath());
        message.setMessage(body);

        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to addresses: " + toAddresses);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email body: " + body);
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
