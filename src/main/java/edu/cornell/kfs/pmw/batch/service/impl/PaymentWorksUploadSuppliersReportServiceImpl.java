package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDataTransformationService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksUploadSuppliersReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksUploadSuppliersReportServiceImpl extends PaymentWorksReportServiceImpl implements PaymentWorksUploadSuppliersReportService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUploadSuppliersServiceImpl.class);

    private static final int SUMMARY_ROW_COL1_LENGTH = 58;
    private static final int SUMMARY_ROW_COL2_LENGTH = 20;
    private static final int POST_SECTION_NEWLINES_COUNT = 4;
    private static final String SUMMARY_ROW_FORMAT = "%58s %20s";
    private static final String PROCESSING_ROW_FORMAT = "%30s %-70s";
    private static final String RECORD_COUNT_HEADER = "Record Count";

    protected PaymentWorksDataTransformationService paymentWorksDataTransformationService;

    private String globalMessagesSubTitle;

    @Override
    public void generateAndEmailProcessingReport(PaymentWorksUploadSuppliersBatchReportData reportData) {
        File reportFile = generateReport(reportData);
        sendResultsEmail(reportData, reportFile);
    }

    protected File generateReport(PaymentWorksUploadSuppliersBatchReportData reportData) {
        reportData.populateOutstandingSummaryItemsForReport();
        initializeReportTitleAndFileName(getReportFileNamePrefix(), getReportTitle());
        writeSummarySubReport(reportData);
        writeGlobalMessagesSubReport(reportData);
        writeProcessingSubReportBasedOnErrorStatus(reportData);
        finalizeReport();
        return getReportWriterService().getReportFile();
    }

    protected void sendResultsEmail(PaymentWorksUploadSuppliersBatchReportData reportData, File reportFile) {
        LOG.info("sendResultsEmail: Preparing to send email for batch job results report " + reportData.retrieveReportName());
        String body = readReportFileToString(reportData, reportFile);
        String subject = buildEmailSubject(reportData);
        getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
        LOG.info("sendResultsEmail: Email was sent for batch job results report. toAddress = "
                + getToAddress() + "  fromAddress = " + getFromAddress() + "  subject = '" + subject + "'.");
    }

    protected String readReportFileToString(PaymentWorksUploadSuppliersBatchReportData reportData, File reportFile) {
        String contents = getPaymentWorksBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString: could not read report file into a String");
            contents = "Could not read the " + reportData.retrieveReportName() + " file.";
        }
        return contents;
    }

    protected String buildEmailSubject(PaymentWorksUploadSuppliersBatchReportData reportData) {
        StringBuilder subject = new StringBuilder();
        subject.append("The ")
                .append(reportData.retrieveReportName())
                .append(" batch job has been run.");
        if (!reportData.isUpdatedKfsAndPaymentWorksSuccessfully()) {
            subject.append("  Vendors with processing errors exist.");
        }
        return subject.toString();
    }

    protected void writeSummarySubReport(PaymentWorksUploadSuppliersBatchReportData reportData) {
        ensureSummaryLabelsHaveValues(reportData);
        getReportWriterService().writeSubTitle(getSummarySubTitle());
        getReportWriterService().writeNewLines(1);
        
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(
                SUMMARY_ROW_FORMAT, buildBlankString(SUMMARY_ROW_COL1_LENGTH), RECORD_COUNT_HEADER);
        getReportWriterService().writeFormattedMessageLine(
                SUMMARY_ROW_FORMAT, buildDashString(SUMMARY_ROW_COL1_LENGTH), buildDashString(SUMMARY_ROW_COL2_LENGTH));
        
        writeFormattedSummaryLineForSubReport(reportData.getRecordsFoundToProcessSummary());
        writeFormattedSummaryLineForSubReport(reportData.getRecordsProcessedSummary());
        writeFormattedSummaryLineForSubReport(reportData.getRecordsProcessedByPaymentWorksSummary());
        writeFormattedSummaryLineForSubReport(reportData.getRecordsWithProcessingErrorsSummary());
        getReportWriterService().writeNewLines(POST_SECTION_NEWLINES_COUNT);
    }

    protected void writeFormattedSummaryLineForSubReport(PaymentWorksBatchReportSummaryItem summary) {
        getReportWriterService().writeFormattedMessageLine(
                SUMMARY_ROW_FORMAT, summary.getItemLabel(), summary.getRecordCount());
    }

    protected void writeGlobalMessagesSubReport(PaymentWorksUploadSuppliersBatchReportData reportData) {
        String subTitle = getGlobalMessagesSubTitle();
        String noElementsMessage = getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_NO_GLOBAL_MESSAGES_MESSAGE);
        writeSubReport(reportData.getGlobalMessages(), subTitle, noElementsMessage, this::writeGlobalMessagesList);
    }

    protected void writeGlobalMessagesList(List<String> globalMessages) {
        for (String globalMessage : globalMessages) {
            getReportWriterService().writeFormattedMessageLine(globalMessage);
        }
        getReportWriterService().writeNewLines(POST_SECTION_NEWLINES_COUNT);
    }

    protected void writeProcessingSubReportBasedOnErrorStatus(PaymentWorksUploadSuppliersBatchReportData reportData) {
        if (reportData.isUpdatedKfsAndPaymentWorksSuccessfully()) {
            writeProcessingSubReport(reportData.getRecordsProcessed(), getProcessedSubTitle(),
                    getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_PROCESSED_MESSAGE));
        } else {
            writeProcessingSubReport(reportData.getRecordsWithProcessingErrors(), getProcessingErrorsSubTitle(),
                    getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_GENERATING_EXCEPTIONS_MESSAGE));
        }
    }

    protected void writeProcessingSubReport(List<PaymentWorksBatchReportVendorItem> vendorReportDataItems,
            String processingSubReportTitle, String noDataToOutputMessage) {
        writeSubReport(vendorReportDataItems, processingSubReportTitle, noDataToOutputMessage, this::writeReportVendorItems);
    }

    protected void writeReportVendorItems(List<PaymentWorksBatchReportVendorItem> reportItems) {
        for (PaymentWorksBatchReportVendorItem reportItem : reportItems) {
            writeFormattedLineForProcessingSubReport(getPaymentWorksVendorIdLabel(), reportItem.getPmwVendorId());
            writeFormattedLineForProcessingSubReport(getSubmittedDateLabel(),
                    getPaymentWorksDataTransformationService().formatReportSubmissionTimeStamp(reportItem.getPmwSubmissionTimeStamp()));
            writeFormattedLineForProcessingSubReport(getVendorTypeLabel(), reportItem.getPmwVendorType());
            writeFormattedLineForProcessingSubReport(getVendorNameLabel(), reportItem.getPmwVendorLegalNameForDisplay());
            writeFormattedLineForProcessingSubReport(getTaxIdTypeLabel(),
                    getPaymentWorksDataTransformationService().convertPmwTinTypeCodeToPmwTinTypeText(reportItem.getPmwTaxIdType()));
            writeFormattedLineForProcessingSubReport(getVendorSubmitterEmailLabel(), reportItem.getPmwSubmitterEmailAddress());
            writeFormattedLineForProcessingSubReport(getInitiatorNetidLabel(), reportItem.getPmwInitiatorNetId());
            writeFormattedLineForProcessingSubReport(getKfsVendorNumberLabel(),
                    getPaymentWorksDataTransformationService().formatReportVendorNumber(reportItem));
            getReportWriterService().writeNewLines(POST_SECTION_NEWLINES_COUNT);
        }
    }

    protected void writeFormattedLineForProcessingSubReport(Object... placeholderArgs) {
        getReportWriterService().writeFormattedMessageLine(PROCESSING_ROW_FORMAT, placeholderArgs);
    }

    protected <T> void writeSubReport(List<T> subReportItems, String subTitle, String noElementsMessage, Consumer<List<T>> subReportItemsPrinter) {
        if (CollectionUtils.isEmpty(subReportItems)) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(subTitle);
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(noElementsMessage);
            getReportWriterService().writeNewLines(POST_SECTION_NEWLINES_COUNT);
        } else {
            getReportWriterService().writeSubTitle(subTitle);
            getReportWriterService().writeNewLines(1);
            subReportItemsPrinter.accept(subReportItems);
        }
    }

    protected void ensureSummaryLabelsHaveValues(PaymentWorksUploadSuppliersBatchReportData reportData) {
        ensureSummaryLabelHasValue(reportData.getRecordsFoundToProcessSummary(),
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDORS_READY_FOR_UPLOAD_FOUND_LABEL);
        ensureSummaryLabelHasValue(reportData.getRecordsProcessedSummary(),
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_SUCCESSFULLY_PROCESSED_LABEL);
        ensureSummaryLabelHasValue(reportData.getRecordsWithProcessingErrorsSummary(),
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_ERRORED_LABEL);
        ensureSummaryLabelHasValue(reportData.getRecordsProcessedByPaymentWorksSummary(),
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDORS_RECORDED_LABEL);
        ensureSummaryLabelHasValue(reportData.getGlobalMessagesSummary(),
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_GLOBAL_MESSAGES_LABEL);
    }

    protected void ensureSummaryLabelHasValue(PaymentWorksBatchReportSummaryItem summary, String parameterName) {
        if (StringUtils.isBlank(summary.getItemLabel())) {
            summary.setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(parameterName));
        }
    }

    protected String buildBlankString(int length) {
        return StringUtils.repeat(KFSConstants.BLANK_SPACE, length);
    }

    protected String buildDashString(int length) {
        return StringUtils.repeat(KFSConstants.DASH, length);
    }

    public PaymentWorksDataTransformationService getPaymentWorksDataTransformationService() {
        return paymentWorksDataTransformationService;
    }

    public void setPaymentWorksDataTransformationService(PaymentWorksDataTransformationService paymentWorksDataTransformationService) {
        this.paymentWorksDataTransformationService = paymentWorksDataTransformationService;
    }

    @Override
    public String getToAddress() {
        return getPropertyAndInitializeIfNecessary(toAddress, super::setToAddress,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_EMAIL_TO_ADDRESS);
    }

    @Override
    public String getFromAddress() {
        return getPropertyAndInitializeIfNecessary(fromAddress, super::setFromAddress,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_EMAIL_FROM_ADDRESS);
    }

    @Override
    public String getReportFileNamePrefix() {
        return getPropertyAndInitializeIfNecessary(reportFileNamePrefix, super::setReportFileNamePrefix,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_FILE_NAME_PREFIX);
    }

    @Override
    public String getReportTitle() {
        return getPropertyAndInitializeIfNecessary(reportTitle, super::setReportTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_TITLE);
    }

    @Override
    public String getSummarySubTitle() {
        return getPropertyAndInitializeIfNecessary(summarySubTitle, super::setSummarySubTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_SUMMARY_SUB_TITLE);
    }

    @Override
    public String getProcessedSubTitle() {
        return getPropertyAndInitializeIfNecessary(processedSubTitle, super::setProcessedSubTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDORS_PROCESSED_SUB_TITLE);
    }

    @Override
    public String getProcessingErrorsSubTitle() {
        return getPropertyAndInitializeIfNecessary(processingErrorsSubTitle, super::setProcessingErrorsSubTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDORS_WITH_PROCESSING_ERRORS_SUB_TITLE);
    }

    @Override
    public String getUnprocessedSubTitle() {
        return KFSConstants.EMPTY_STRING;
    }

    public String getGlobalMessagesSubTitle() {
        return getPropertyAndInitializeIfNecessary(globalMessagesSubTitle, this::setGlobalMessagesSubTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_GLOBAL_MESSAGES_SUB_TITLE);
    }

    public void setGlobalMessagesSubTitle(String globalMessagesSubTitle) {
        this.globalMessagesSubTitle = globalMessagesSubTitle;
    }

    private String getPropertyAndInitializeIfNecessary(String currentValue, Consumer<String> propertySetter, String parameterName) {
        if (StringUtils.isBlank(currentValue)) {
            String parameterValue = getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(parameterName);
            propertySetter.accept(parameterValue);
            return parameterValue;
        } else {
            return currentValue;
        }
    }

    @Override
    public ReportWriterService getReportWriterService() {
        return super.reportWriterService;
    }

    @Override
    public void setReportWriterService(ReportWriterService reportWriterService) {
        super.reportWriterService = reportWriterService;
    }

}
