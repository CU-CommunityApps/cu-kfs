package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.pmw.batch.PaymentWorksDataTransformation;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportEmailService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksNewVendorPayeeAchReportServiceImpl extends PaymentWorksReportServiceImpl implements PaymentWorksNewVendorPayeeAchReportService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorPayeeAchReportServiceImpl.class);

    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksReportEmailService paymentWorksReportEmailService;

    private String kfsAchDocumentNumberLabel;
    private String bankAcctNameOnAccountLabel;
    private String disapprovedVendorsSubTitle;
    private String noAchDataProvidedVendorsSubTitle;
    private String recordsGeneratingExceptionSubTitle;
    private String recordsForeignAchBankSubTitle;

    @Override
    public void generateAndEmailProcessingReport(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        LOG.debug("generateAndEmailReport entered");
        File reportFile = generateReport(reportData);
        sendResultsEmail(reportData, reportFile);
    }
    
    private File generateReport(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        LOG.debug("generateReport: entered");
        reportData.populateOutstandingSummaryItemsForReport();
        initializeReportTitleAndFileName(getReportFileNamePrefix(), getReportTitle());
        writeSummarySubReport(reportData);
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsWithPayeeAchsProcessed(), getProcessedSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_PROCESSED_MESSAGE));
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsWithPayeeAchProcessingErrors(), getProcessingErrorsSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE));
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsWithUnprocessablePayeeAchs(), getUnprocessedSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.MANUAL_DATA_ENTRY_NOT_REQUIRED_MESSAGE));
        writeProcessingSubReport(reportData.getDisapprovedVendors(), getDisapprovedVendorsSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_DISAPPROVED_VENDORS_WITH_PENDING_ACH_DATA_MESSAGE));
        writeProcessingSubReport(reportData.getNoAchDataProvidedVendors(), getNoAchDataProvidedVendorsSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_VENDORS_WITHOUT_ACH_DATA_MESSAGE));
        writeProcessingSubReport(reportData.getRecordsGeneratingException(), getRecordsGeneratingExceptionSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_GENERATING_EXCEPTIONS_MESSAGE));
        writeProcessingSubReport(reportData.getForeignAchItems(), getRecordsForeignAchBankSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_FOREIGN_ACH_BANK));
        finalizeReport();
        return getReportWriterService().getReportFile();
    }

    private void sendResultsEmail(PaymentWorksNewVendorPayeeAchBatchReportData reportData, File reportFile) {
        LOG.info("sendResultsEmail: Preparing to send email for batch job results report " + reportData.retrieveReportName());
        String body = readReportFileToString(reportData, reportFile);
        String subject = buildEmailSubject(reportData);
        getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
        LOG.info("sendResultsEmail: Email was sent for batch job results report. toAddress = " + getToAddress() + "  fromAddress = " + getFromAddress() + "  subject = '" + subject + "'.");
    }

    protected String readReportFileToString(PaymentWorksNewVendorPayeeAchBatchReportData reportData, File reportFile) {
        String contents = this.getPaymentWorksBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString: could not read report file into a String");
            contents = "Could not read the " + reportData.retrieveReportName() + " file.";
        }
        return contents;
    }
    
    protected String buildEmailSubject(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        StringBuilder sb = new StringBuilder("The ");
        sb.append(reportData.retrieveReportName()).append(" batch job has been run.");
        if(!reportData.retrievePaymentWorksVendorsWithUnprocessablePayeeAchs().isEmpty()) {
            sb.append("  Vendors with unprocessable Payee ACH data were detected.");
        }
        if(!reportData.retrievePaymentWorksVendorsWithPayeeAchProcessingErrors().isEmpty()) {
            sb.append("  Vendors with Payee ACH processing errors exist.");
        }
        return sb.toString();
    }
    
    protected void writeSummarySubReport(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        LOG.debug("writeSummarySubReport: entered");
        ensureSummaryLabelsHaveValues(reportData);
        getReportWriterService().writeSubTitle(getSummarySubTitle());
        getReportWriterService().writeNewLines(1);

        String rowFormat = "%58s %20s";
        String hdrRowFormat = "%58s %20s";
        Object[] headerArgs =  { "                                                         ", "Record Count"};
        Object[] headerBreak = { "---------------------------------------------------------", "------------"};
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);

        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsFoundToProcessSummary().getItemLabel(), reportData.getRecordsFoundToProcessSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsThatCouldNotBeProcessedSummary().getItemLabel(), reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsProcessedSummary().getItemLabel(), reportData.getRecordsProcessedSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsWithProcessingErrorsSummary().getItemLabel(), reportData.getRecordsWithProcessingErrorsSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getDisapprovedVendorsSummary().getItemLabel(), reportData.getDisapprovedVendorsSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getNoAchDataProvidedVendorsSummary().getItemLabel(), reportData.getNoAchDataProvidedVendorsSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsGeneratingExceptionSummary().getItemLabel(), reportData.getRecordsGeneratingExceptionSummary().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsWithForeignAchSummary().getItemLabel(), reportData.getRecordsWithForeignAchSummary().getRecordCount());
        getReportWriterService().writeNewLines(4);
    }
    
    protected void writeProcessingSubReport(List<PaymentWorksBatchReportVendorItem> vendorReportDataItems, String processingSubReportTitle, String noDataToOutputMessage) {
        LOG.debug("writeProcessingSubReport: entered");
        String rowFormat = "%30s %-70s";

        if (CollectionUtils.isEmpty(vendorReportDataItems)) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(processingSubReportTitle);
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(noDataToOutputMessage);
            getReportWriterService().writeNewLines(4);
        }
        else {
            getReportWriterService().writeSubTitle(processingSubReportTitle);
            getReportWriterService().writeNewLines(1);

            for (PaymentWorksBatchReportVendorItem reportItem : vendorReportDataItems) {
                String vendorName = (StringUtils.isNotBlank(reportItem.getPmwVendorLegelName()) ? reportItem.getPmwVendorLegelName() : (reportItem.getPmwVendorLegelLastName() + "," + reportItem.getPmwVendorLegelFirstName()));

                getReportWriterService().writeFormattedMessageLine(rowFormat, getPaymentWorksVendorIdLabel(), reportItem.getPmwVendorId());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getSubmittedDateLabel(), PaymentWorksDataTransformation.formatReportSubmissionTimeStamp(reportItem.getPmwSubmissionTimeStamp()));
                getReportWriterService().writeFormattedMessageLine(rowFormat, getVendorTypeLabel(), reportItem.getPmwVendorType());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getVendorNameLabel(), vendorName);
                getReportWriterService().writeFormattedMessageLine(rowFormat, getTaxIdTypeLabel(), PaymentWorksDataTransformation.convertPmwTinTypeCodeToPmwTinTypeText(reportItem.getPmwTaxIdType()));
                getReportWriterService().writeFormattedMessageLine(rowFormat, getVendorSubmitterEmailLabel(), reportItem.getPmwSubmitterEmailAddress());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getInitiatorNetidLabel(), reportItem.getPmwInitiatorNetId());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getKfsVendorNumberLabel(), PaymentWorksDataTransformation.formatReportVendorNumber(reportItem));
                getReportWriterService().writeFormattedMessageLine(rowFormat, getKfsAchDocumentNumberLabel(), reportItem.getKfsAchDocumentNumber());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getBankAcctNameOnAccountLabel(), reportItem.getBankAcctNameOnAccount());
                getReportWriterService().writeNewLines(1);
                writeErrorItemMessages(reportItem.getErrorMessages());
                getReportWriterService().writeNewLines(4);
            }
        }
    }
    
    private void writeErrorItemMessages(List<String> errorMessages) {
        LOG.debug("writeErrorItemMessages, entered");
        getReportWriterService().writeFormattedMessageLine(this.getErrorsLabel());
        if (CollectionUtils.isEmpty(errorMessages)) {
            getReportWriterService().writeFormattedMessageLine(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_VALIDATION_ERRORS_TO_OUTPUT_MESSAGE));
        }
        else {
            for (String errorMessage : errorMessages) {
                getReportWriterService().writeFormattedMessageLine(errorMessage);
            }
        }
    }

    private void ensureSummaryLabelsHaveValues(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsFoundToProcessSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_APPROVED_VENDORS_WITH_PENDING_ACH_FOUND_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsThatCouldNotBeProcessedSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_PENDING_ACH_NOT_PROCESSED_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsProcessedSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_PENDING_ACH_SUCCESSFULLY_PROCESSED_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsWithProcessingErrorsSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_PENDING_ACH_ERRORED_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getDisapprovedVendorsSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_DISAPPROVED_VENDORS_WITH_PENDING_ACH_FOUND_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getNoAchDataProvidedVendorsSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_VENDORS_WITH_NO_PENDING_ACH_DATA_FOUND_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsGeneratingExceptionSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_PENDING_ACH_GENERATING_EXCEPTIONS_LABEL);
        setSummaryItemLabelToDefaultWhenBlank(reportData.getRecordsWithForeignAchSummary(), PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_FOREIGN_ACH_BANK);
        
    }
    
    private void setSummaryItemLabelToDefaultWhenBlank(PaymentWorksBatchReportSummaryItem summaryItem, String defaultLabelParameterKey) {
        if (StringUtils.isEmpty(summaryItem.getItemLabel())) {
            summaryItem.setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(defaultLabelParameterKey));
        }
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public PaymentWorksReportEmailService getPaymentWorksReportEmailService() {
        return paymentWorksReportEmailService;
    }

    public void setPaymentWorksReportEmailService(PaymentWorksReportEmailService paymentWorksReportEmailService) {
        this.paymentWorksReportEmailService = paymentWorksReportEmailService;
    }

    public String getReportFileNamePrefix() {
        if (ObjectUtils.isNull(reportFileNamePrefix)) {
            setReportFileNamePrefix(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_FILE_NAME_PREFIX));
            LOG.info("getReportFileNamePrefix: just set reportFileNamePrefix to param value = " + getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_FILE_NAME_PREFIX));
        }
        else
        {
            LOG.info("getReportFileNamePrefix: sees reportFileNamePrefix as = " + reportFileNamePrefix);
        }
        return reportFileNamePrefix;
    }

    public String getReportTitle() {
        if (ObjectUtils.isNull(reportTitle)) {
            setReportTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_TITLE));
        }
        return reportTitle;
    }

    public String getSummarySubTitle() {
        if (ObjectUtils.isNull(summarySubTitle)) {
            setSummarySubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_SUMMARY_SUB_TITLE));
        }
        return summarySubTitle;
    }

    public String getToAddress() {
        if (ObjectUtils.isNull(toAddress)) {
            setToAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_EMAIL_TO_ADDRESS));
        }
        return toAddress;
    }

    public String getFromAddress() {
        if (ObjectUtils.isNull(fromAddress)) {
            setFromAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_EMAIL_FROM_ADDRESS));
        }
        return fromAddress;
    }
    
    public String getProcessedSubTitle() {
        if (ObjectUtils.isNull(processedSubTitle)) {
            setProcessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_VENDORS_PROCESSED_SUB_TITLE));
        }
        return processedSubTitle;
    }

    public String getProcessingErrorsSubTitle() {
        if (ObjectUtils.isNull(processingErrorsSubTitle)) {
            setProcessingErrorsSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_VENDORS_WITH_PROCESSING_ERRORS_SUB_TITLE));
        }
        return processingErrorsSubTitle;
    }

    public String getUnprocessedSubTitle() {
        if (ObjectUtils.isNull(unprocessedSubTitle)) {
            setUnprocessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_UNPROCESSED_VENDOR_ACHS_SUB_TITLE));
        }
        return unprocessedSubTitle;
    }

    public String getKfsAchDocumentNumberLabel() {
        if (ObjectUtils.isNull(kfsAchDocumentNumberLabel)) {
            setKfsAchDocumentNumberLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_KFS_ACH_DOCUMENT_NUMBER_LABEL));
        }
        return kfsAchDocumentNumberLabel;
    }

    public void setKfsAchDocumentNumberLabel(String kfsAchDocumentNumberLabel) {
        this.kfsAchDocumentNumberLabel = kfsAchDocumentNumberLabel;
    }

    public String getBankAcctNameOnAccountLabel() {
        if (ObjectUtils.isNull(bankAcctNameOnAccountLabel)) {
            setBankAcctNameOnAccountLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_BANK_ACCT_NAME_ON_ACCOUNT_LABEL));
        }
        return bankAcctNameOnAccountLabel;
    }

    public void setBankAcctNameOnAccountLabel(String bankAcctNameOnAccountLabel) {
        this.bankAcctNameOnAccountLabel = bankAcctNameOnAccountLabel;
    }

    public String getDisapprovedVendorsSubTitle() {
        if (ObjectUtils.isNull(disapprovedVendorsSubTitle)) {
            setDisapprovedVendorsSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_DISAPPROVED_VENDORS_WITH_PENDING_ACH_FOUND_SUB_TITLE));
        }
        return disapprovedVendorsSubTitle;
    }

    public void setDisapprovedVendorsSubTitle(String disapprovedVendorsSubTitle) {
        this.disapprovedVendorsSubTitle = disapprovedVendorsSubTitle;
    }

    public String getNoAchDataProvidedVendorsSubTitle() {
        if (ObjectUtils.isNull(noAchDataProvidedVendorsSubTitle)) {
            setNoAchDataProvidedVendorsSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_VENDORS_WITH_NO_PENDING_ACH_DATA_FOUND_SUB_TITLE));
        }
        return noAchDataProvidedVendorsSubTitle;
    }

    public void setNoAchDataProvidedVendorsSubTitle(String noAchDataProvidedVendorsSubTitle) {
        this.noAchDataProvidedVendorsSubTitle = noAchDataProvidedVendorsSubTitle;
    }

    public String getRecordsGeneratingExceptionSubTitle() {
        if (ObjectUtils.isNull(recordsGeneratingExceptionSubTitle)) {
            setRecordsGeneratingExceptionSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_PENDING_ACH_GENERATING_EXCEPTIONS_SUB_TITLE));
        }
        return recordsGeneratingExceptionSubTitle;
    }

    public void setRecordsGeneratingExceptionSubTitle(String recordsGeneratingExceptionSubTitle) {
        this.recordsGeneratingExceptionSubTitle = recordsGeneratingExceptionSubTitle;
    }

    public String getRecordsForeignAchBankSubTitle() {
        if (StringUtils.isBlank(recordsForeignAchBankSubTitle)) {
            setRecordsGeneratingExceptionSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_FOREIGN_ACH_BANK_SUB_TITLE));
        }
        return recordsForeignAchBankSubTitle;
    }

    public void setRecordsForeignAchBankSubTitle(String recordsForeignAchBankSubTitle) {
        this.recordsForeignAchBankSubTitle = recordsForeignAchBankSubTitle;
    }

    public ReportWriterService getReportWriterService() {
        return super.reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        super.reportWriterService = reportWriterService;
    }
    
    public ConfigurationService getConfigurationService() {
        return super.configurationService;
    }
    
    public void setConfigurationService(ConfigurationService configurationService) {
        super.configurationService = configurationService;
    }
}
