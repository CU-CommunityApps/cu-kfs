package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.pmw.batch.PaymentWorksDataTransformation;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportEmailService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksNewVendorRequestsReportServiceImpl extends PaymentWorksReportServiceImpl implements PaymentWorksNewVendorRequestsReportService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorRequestsReportServiceImpl.class);

    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksReportEmailService paymentWorksReportEmailService;
    
    @Override
    public void generateAndEmailProcessingReport(PaymentWorksNewVendorRequestsBatchReportData reportData) {
        LOG.debug("generateAndEmailReport entered");
        File reportFile = generateReport(reportData);
        sendResultsEmail(reportData, reportFile);
    }
    
    private File generateReport(PaymentWorksNewVendorRequestsBatchReportData reportData) {
        LOG.debug("generateReport: entered");
        reportData.populateOutstandingSummaryItemsForReport();
        initializeReportTitleAndFileName(getReportFileNamePrefix(), getReportTitle());
        writeSummarySubReport(reportData);
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsProcessed(), getProcessedSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_PROCESSED_MESSAGE));
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsWithProcessingErrors(), getProcessingErrorsSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE));
        writeUnprocessedSubReport(reportData.retrieveUnprocessablePaymentWorksVendors(), getUnprocessedSubTitle(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.MANUAL_DATA_ENTRY_NOT_REQUIRED_MESSAGE));
        finalizeReport();
        return getReportWriterService().getReportFile();
    }

    private void sendResultsEmail(PaymentWorksNewVendorRequestsBatchReportData reportData, File reportFile) {
        LOG.info("sendResultsEmail: Preparing to send email for batch job results report " + reportData.retrieveReportName());
        String body = readReportFileToString(reportData, reportFile);
        String subject = buildEmailSubject(reportData);
        getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
        LOG.info("sendResultsEmail: Email was sent for batch job results report. toAddress = " + getToAddress() + "  fromAddress = " + getFromAddress() + "  subject = '" + subject + "'.");
    }

    protected String readReportFileToString(PaymentWorksNewVendorRequestsBatchReportData reportData, File reportFile) {
        String contents = getPaymentWorksBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString: could not read report file into a String");
            contents = "Could not read the " + reportData.retrieveReportName() + " file.";
        }
        return contents;
    }
    
    protected String buildEmailSubject(PaymentWorksNewVendorRequestsBatchReportData reportData) {
        StringBuilder sb = new StringBuilder("The ");
        sb.append(reportData.retrieveReportName()).append(" batch job has been run.");
        if(!reportData.retrieveUnprocessablePaymentWorksVendors().isEmpty()) {
            sb.append("  Vendors that could not be saved to staging table were detected.");
        }
        if(!reportData.retrievePaymentWorksVendorsWithProcessingErrors().isEmpty()) {
            sb.append("  Vendors with processing errors exist.");
        }
        return sb.toString();
    }
    
    protected void writeSummarySubReport(PaymentWorksNewVendorRequestsBatchReportData reportData) {
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
                getReportWriterService().writeNewLines(1);
                writeErrorItemMessages(reportItem.getErrorMessages());
                getReportWriterService().writeNewLines(4);
            }
        }
    }
    
    public void writeUnprocessedSubReport(List<PaymentWorksBatchReportRawDataItem> manualEntryDataForReport, String manualSubReportTitle, String noDataToOutputMessage) {
        LOG.debug("writeUnprocessedSubReport: entered");
        String rowFormat = "%74s";

        if (CollectionUtils.isEmpty(manualEntryDataForReport)) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(manualSubReportTitle);
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(noDataToOutputMessage);
            getReportWriterService().writeNewLines(4);
        }
        else {
            getReportWriterService().writeSubTitle(manualSubReportTitle);
            getReportWriterService().writeNewLines(1);

            for (PaymentWorksBatchReportRawDataItem reportItem : manualEntryDataForReport) {
                getReportWriterService().writeFormattedMessageLine(rowFormat, reportItem.getpmwFlatDdto());
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
    
    private void ensureSummaryLabelsHaveValues (PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (StringUtils.isEmpty(reportData.getRecordsFoundToProcessSummary().getItemLabel())) {
            reportData.getRecordsFoundToProcessSummary().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_APPROVED_NEW_VENDORS_FOUND_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getRecordsThatCouldNotBeProcessedSummary().getItemLabel())) {
            reportData.getRecordsThatCouldNotBeProcessedSummary().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_APPROVED_NEW_VENDORS_NOT_PROCESSED_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getRecordsProcessedSummary().getItemLabel())) {
            reportData.getRecordsProcessedSummary().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_APPROVED_NEW_VENDORS_SUCCESSFULLY_PROCESSED_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getRecordsWithProcessingErrorsSummary().getItemLabel())) {
            reportData.getRecordsWithProcessingErrorsSummary().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_APPROVED_NEW_VENDORS_ERRORED_LABEL));
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
            setReportFileNamePrefix(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_FILE_NAME_PREFIX));
            LOG.info("getReportFileNamePrefix just set reportFileNamePrefix to param value=" + getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_FILE_NAME_PREFIX) + "=");
        }
        else
        {
            LOG.info("getReportFileNamePrefix sees reportFileNamePrefix as null =" + reportFileNamePrefix + "=");
        }
        return reportFileNamePrefix;
    }

    public String getReportTitle() {
        if (ObjectUtils.isNull(reportTitle)) {
            setReportTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_TITLE));
        }
        return reportTitle;
    }

    public String getSummarySubTitle() {
        if (ObjectUtils.isNull(summarySubTitle)) {
            setSummarySubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_SUMMARY_SUB_TITLE));
        }
        return summarySubTitle;
    }

    public String getToAddress() {
        if (ObjectUtils.isNull(toAddress)) {
            setToAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_EMAIL_TO_ADDRESS));
        }
        return toAddress;
    }

    public String getFromAddress() {
        if (ObjectUtils.isNull(fromAddress)) {
            setFromAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_EMAIL_FROM_ADDRESS));
        }
        return fromAddress;
    }

    public String getProcessedSubTitle() {
        if (ObjectUtils.isNull(processedSubTitle)) {
            setProcessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDORS_PROCESSED_SUB_TITLE));
        }
        return processedSubTitle;
    }

    public String getProcessingErrorsSubTitle() {
        if (ObjectUtils.isNull(processingErrorsSubTitle)) {
            setProcessingErrorsSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDORS_WITH_PROCESSING_ERRORS_SUB_TITLE));
        }
        return processingErrorsSubTitle;
    }

    public String getUnprocessedSubTitle() {
        if (ObjectUtils.isNull(unprocessedSubTitle)) {
            setUnprocessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_UNPROCESSED_VENDORS_SUB_TITLE));
        }
        return unprocessedSubTitle;
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
