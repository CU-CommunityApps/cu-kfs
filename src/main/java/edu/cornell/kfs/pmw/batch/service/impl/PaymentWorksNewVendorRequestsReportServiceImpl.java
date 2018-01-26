package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.kuali.kfs.krad.util.ObjectUtils;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksEmailableReportData;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDataTransformationService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportEmailService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksNewVendorRequestsReportServiceImpl implements PaymentWorksNewVendorRequestsReportService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorRequestsReportServiceImpl.class);

    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksReportEmailService paymentWorksReportEmailService;
    protected ReportWriterService reportWriterService;
    protected PaymentWorksDataTransformationService paymentWorksDataTransformationService;
    
    private String toAddress = null;
    private String fromAddress = null;
    private String reportFileNamePrefix = null;
    private String reportTitle = null;
    private String summarySubTitle = null; 
    private String processedSubTitle = null;
    private String processingErrorsSubTitle = null;
    private String unprocessedSubTitle = null;
    private String paymentWorksVendorIdLabel = null;
    private String submittedDateLabel = null;
    private String vendorTypeLabel = null;
    private String vendorNameLabel = null;
    private String taxIdTypeLabel = null;
    private String vendorSubmitterEmailLabel = null;
    private String initiatorNetidLabel = null;
    private String errorsLabel = null;
    
    @Override
    public PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorProcessed) {
        return new PaymentWorksBatchReportVendorItem(pmwVendorProcessed.getPmwVendorRequestId(),
                                                     pmwVendorProcessed.getProcessTimestamp(),
                                                     pmwVendorProcessed.getVendorType(), 
                                                     pmwVendorProcessed.getRequestingCompanyLegalName(),
                                                     pmwVendorProcessed.getRequestingCompanyLegalFirstName(),
                                                     pmwVendorProcessed.getRequestingCompanyLegalLastName(), 
                                                     pmwVendorProcessed.getRequestingCompanyTinType(),
                                                     pmwVendorProcessed.getRequestingCompanyCorporateEmail(),
                                                     pmwVendorProcessed.getInitiatorNetId(),
                                                     null);
    }

    @Override
    public PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorWithErrors, List<String> errorMessages) {
        return new PaymentWorksBatchReportVendorItem(pmwVendorWithErrors.getPmwVendorRequestId(),
                                                     pmwVendorWithErrors.getProcessTimestamp(),
                                                     pmwVendorWithErrors.getVendorType(), 
                                                     pmwVendorWithErrors.getRequestingCompanyLegalName(),
                                                     pmwVendorWithErrors.getRequestingCompanyLegalFirstName(),
                                                     pmwVendorWithErrors.getRequestingCompanyLegalLastName(), 
                                                     pmwVendorWithErrors.getRequestingCompanyTinType(),
                                                     pmwVendorWithErrors.getRequestingCompanyCorporateEmail(),
                                                     pmwVendorWithErrors.getInitiatorNetId(),
                                                     errorMessages);
    }

    @Override
    public void generateAndEmailProcessingReport(PaymentWorksNewVendorRequestsBatchReportData reportData) {
        LOG.debug("generateAndEmailReport entered");
        File reportFile = generateReport(reportData);
        sendResultsEmail(reportData, reportFile);
    }
    
    private File generateReport(PaymentWorksNewVendorRequestsBatchReportData reportData) {
        LOG.debug("generateReport: entered");
        reportData.populateOutstandingSummaryItemsForReport();
        initializeReportTitleAndFileName(reportData);
        writeSummarySubReport(reportData);
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsProcessed(), getProcessedSubTitle(), PaymentWorksConstants.PaymentWorksBatchReportMessages.NO_RECORDS_PROCESSED_MESSAGE);
        writeProcessingSubReport(reportData.retrievePaymentWorksVendorsWithProcessingErrors(), getProcessingErrorsSubTitle(), PaymentWorksConstants.PaymentWorksBatchReportMessages.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
        writeUnprocessedSubReport(reportData.retrieveUnprocessablePaymentWorksVendors(), getUnprocessedSubTitle(), PaymentWorksConstants.PaymentWorksBatchReportMessages.MANUAL_DATA_ENTRY_NOT_REQUIRED);
        finalizeReport();
        return getReportWriterService().getReportFile();
    }

    private void sendResultsEmail(PaymentWorksEmailableReportData reportData, File reportFile) {
        LOG.info("sendResultsEmail: entered");
        String body = readReportFileToString(reportData, reportFile);
        String subject = buildEmailSubject(reportData);
        if (StringUtils.isNotBlank(getToAddress()) && StringUtils.isNotBlank(getFromAddress()) && StringUtils.isNotBlank(body) && StringUtils.isNotBlank(subject)) {
            getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
            LOG.info("sendResultsEmail: Email was sent for batch job results report. toAddress = " + getToAddress() + "  fromAddress = " + getFromAddress() + "  subject = '" + subject + "'");
        }
        else {
            LOG.error("sendResultsEmail: Could not email batch job results report " + reportData.retrieveReportName() + " because " + 
                     ((StringUtils.isBlank(getToAddress())) ? "toAddress is blank. " : "") +
                     ((StringUtils.isBlank(getFromAddress())) ? "fromAddress is blank. " : "") +
                     ((StringUtils.isBlank(subject)) ? "subject is blank. " : "") +
                     ((StringUtils.isBlank(body)) ? "body is blank. " : ""));
        }
    }

    @Override
    public void sendEmailThatNoDataWasFoundToProcess() {
        LOG.info("sendEmailThatNoDataWasFoundToProcess: entered");
        String body = getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_PENDING_VENDORS_FOUND_EMAIL_BODY);
        String subject = getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_PENDING_VENDORS_FOUND_EMAIL_SUBJECT);
        if (StringUtils.isNotBlank(toAddress) && StringUtils.isNotBlank(fromAddress) && StringUtils.isNotBlank(body) && StringUtils.isNotBlank(subject)) {
            getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
            LOG.info("sendEmailThatNoDataWasFoundToProcess: Email was sent that no data was found to process");
        }
        else {
            LOG.error("sendEmailThatNoDataWasFoundToProcess: Could not email notification stating no data found to process for batch job report because " + 
                     ((StringUtils.isBlank(toAddress)) ? "toAddress is blank. " : "") +
                     ((StringUtils.isBlank(fromAddress)) ? "fromAddress is blank. " : "") +
                     ((StringUtils.isBlank(subject)) ? "subject is blank. " : "") +
                     ((StringUtils.isBlank(body)) ? "body is blank. " : ""));
        }
    }

    protected String readReportFileToString(PaymentWorksEmailableReportData reportData, File reportFile) {
        String contents = this.getPaymentWorksBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString: could not read report file into a String");
            contents = "Could not read the " + reportData.retrieveReportName() + " file.";
        }
        return contents;
    }
    
    protected String buildEmailSubject(PaymentWorksEmailableReportData reportData) {
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
    
    protected void initializeReportTitleAndFileName(PaymentWorksNewVendorRequestsBatchReportData  reportData) {
        LOG.debug("initializeReportTitleAndFileName: entered");
        getReportWriterService().setFileNamePrefix(getReportFileNamePrefix());
        getReportWriterService().setTitle(getReportTitle());
        getReportWriterService().initialize();
        getReportWriterService().writeNewLines(2);
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

        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getPendingNewVendorsFoundInPmw().getItemLabel(), reportData.getPendingNewVendorsFoundInPmw().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().getItemLabel(), reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getPendingPaymentWorksVendorsProcessed().getItemLabel(), reportData.getPendingPaymentWorksVendorsProcessed().getRecordCount());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getPendingPaymentWorksVendorsWithProcessingErrors().getItemLabel(), reportData.getPendingPaymentWorksVendorsWithProcessingErrors().getRecordCount());
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
                getReportWriterService().writeFormattedMessageLine(rowFormat, getSubmittedDateLabel(), getPaymentWorksDataTransformationService().PROCESSING_TIMESTAMP_REPORT_FORMATTER.format(reportItem.getPmwSubmissionTimeStamp()));
                getReportWriterService().writeFormattedMessageLine(rowFormat, getVendorTypeLabel(), reportItem.getPmwVendorType());
                getReportWriterService().writeFormattedMessageLine(rowFormat, getVendorNameLabel(), vendorName);
                getReportWriterService().writeFormattedMessageLine(rowFormat, getTaxIdTypeLabel(), getPaymentWorksDataTransformationService().convertPmwTinTypeCodeToPmwTinTypeText(reportItem.getPmwTaxIdType()));
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
            getReportWriterService().writeFormattedMessageLine(PaymentWorksConstants.PaymentWorksBatchReportMessages.NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT);
        }
        else {
            for (String errorMessage : errorMessages) {
                getReportWriterService().writeFormattedMessageLine(errorMessage);
            }
        }
    }
    
    protected void finalizeReport() {
        LOG.debug("finalizeReport, entered");
        getReportWriterService().writeNewLines(3);
        getReportWriterService().writeFormattedMessageLine(PaymentWorksConstants.PaymentWorksBatchReportMessages.END_OF_REPORT_MESSAGE);
        getReportWriterService().destroy();
    }

    private void ensureSummaryLabelsHaveValues (PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (StringUtils.isEmpty(reportData.getPendingNewVendorsFoundInPmw().getItemLabel())) {
            reportData.getPendingNewVendorsFoundInPmw().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_PENDING_NEW_VENDORS_FOUND_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().getItemLabel())) {
            reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_PENDING_NEW_VENDORS_NOT_PROCESSED_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getPendingPaymentWorksVendorsProcessed().getItemLabel())) {
            reportData.getPendingPaymentWorksVendorsProcessed().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_PENDING_NEW_VENDORS_SUCCESSFULLY_PROCESSED_LABEL));
        }
        if (StringUtils.isEmpty(reportData.getPendingPaymentWorksVendorsWithProcessingErrors().getItemLabel())) {
            reportData.getPendingPaymentWorksVendorsWithProcessingErrors().setItemLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_PENDING_NEW_VENDORS_ERRORED_LABEL));
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

    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public String getReportFileNamePrefix() {
        if (ObjectUtils.isNull(reportFileNamePrefix)) {
            setReportFileNamePrefix(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_FILE_NAME_PREFIX));
            LOG.info("getReportFileNamePrefix just set reportFileNamePrefix to param value=" +getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_FILE_NAME_PREFIX)+ "=");
        }
        else
        {
            LOG.info("getReportFileNamePrefix sees reportFileNamePrefix as null =" +reportFileNamePrefix+ "=");
        }
        return reportFileNamePrefix;
    }

    public void setReportFileNamePrefix(String reportFileNamePrefix) {
        this.reportFileNamePrefix = reportFileNamePrefix;
    }

    public String getReportTitle() {
        if (ObjectUtils.isNull(reportTitle)) {
            setReportTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_TITLE));
        }
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getSummarySubTitle() {
        if (ObjectUtils.isNull(summarySubTitle)) {
            setSummarySubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_SUMMARY_SUB_TITLE));
        }
        return summarySubTitle;
    }

    public void setSummarySubTitle(String summarySubTitle) {
        this.summarySubTitle = summarySubTitle;
    }

    public String getToAddress() {
        if (ObjectUtils.isNull(toAddress)) {
            setToAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_EMAIL_TO_ADDRESS));
        }
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getFromAddress() {
        if (ObjectUtils.isNull(fromAddress)) {
            setFromAddress(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_EMAIL_FROM_ADDRESS));
        }
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    
    public String getProcessedSubTitle() {
        if (ObjectUtils.isNull(processedSubTitle)) {
            setProcessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDORS_PROCESSED_SUB_TITLE));
        }
        return processedSubTitle;
    }

    public void setProcessedSubTitle(String processedSubTitle) {
        this.processedSubTitle = processedSubTitle;
    }

    public String getProcessingErrorsSubTitle() {
        if (ObjectUtils.isNull(processingErrorsSubTitle)) {
            setProcessingErrorsSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDORS_WITH_PROCESSING_ERRORS_SUB_TITLE));
        }
        return processingErrorsSubTitle;
    }

    public void setProcessingErrorsSubTitle(String processingErrorsSubTitle) {
        this.processingErrorsSubTitle = processingErrorsSubTitle;
    }

    public String getUnprocessedSubTitle() {
        if (ObjectUtils.isNull(unprocessedSubTitle)) {
            setUnprocessedSubTitle(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_UNPROCESSED_VENDORS_SUB_TITLE));
        }
        return unprocessedSubTitle;
    }

    public void setUnprocessedSubTitle(String unprocessedSubTitle) {
        this.unprocessedSubTitle = unprocessedSubTitle;
    }

    public String getPaymentWorksVendorIdLabel() {
        if (ObjectUtils.isNull(paymentWorksVendorIdLabel)) {
            setPaymentWorksVendorIdLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_PAYMENTWORKS_VENDOR_ID_LABEL ));
        }
        return paymentWorksVendorIdLabel;
    }

    public void setPaymentWorksVendorIdLabel(String paymentWorksVendorIdLabel) {
        this.paymentWorksVendorIdLabel = paymentWorksVendorIdLabel;
    }

    public String getSubmittedDateLabel() {
        if (ObjectUtils.isNull(submittedDateLabel)) {
            setSubmittedDateLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_DATE_SUBMITTED_LABEL));
        }
        return submittedDateLabel;
    }

    public void setSubmittedDateLabel(String submittedDateLabel) {
        this.submittedDateLabel = submittedDateLabel;
    }

    public String getVendorTypeLabel() {
        if (ObjectUtils.isNull(vendorTypeLabel)) {
            setVendorTypeLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDOR_TYPE_LABEL ));
        }
        return vendorTypeLabel;
    }

    public void setVendorTypeLabel(String vendorTypeLabel) {
        this.vendorTypeLabel = vendorTypeLabel;
    }

    public String getVendorNameLabel() {
        if (ObjectUtils.isNull(vendorNameLabel)) {
            setVendorNameLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_VENDOR_NAME_LABEL ));
        }
        return vendorNameLabel;
    }

    public void setVendorNameLabel(String vendorNameLabel) {
        this.vendorNameLabel = vendorNameLabel;
    }

    public String getTaxIdTypeLabel() {
        if (ObjectUtils.isNull(taxIdTypeLabel)) {
            setTaxIdTypeLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_TAX_ID_TYPE_LABEL));
        }
        return taxIdTypeLabel;
    }

    public void setTaxIdTypeLabel(String taxIdTypeLabel) {
        this.taxIdTypeLabel = taxIdTypeLabel;
    }

    public String getVendorSubmitterEmailLabel() {
        if (ObjectUtils.isNull(vendorSubmitterEmailLabel)) {
            setVendorSubmitterEmailLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_SUBMITTER_EMAIL_ADDRESS_LABEL));
        }
        return vendorSubmitterEmailLabel;
    }

    public void setVendorSubmitterEmailLabel(String vendorSubmitterEmailLabel) {
        this.vendorSubmitterEmailLabel = vendorSubmitterEmailLabel;
    }

    public String getInitiatorNetidLabel() {
        if (ObjectUtils.isNull(initiatorNetidLabel)) {
            setInitiatorNetidLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_INITIATOR_NETID_LABEL));
        }
        return initiatorNetidLabel;
    }

    public void setInitiatorNetidLabel(String initiatorNetidLabel) {
        this.initiatorNetidLabel = initiatorNetidLabel;
    }

    public String getErrorsLabel() {
        if (ObjectUtils.isNull(errorsLabel)) {
            setErrorsLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_ERRORS_LABEL));
        }
        return errorsLabel;
    }

    public void setErrorsLabel(String errorsLabel) {
        this.errorsLabel = errorsLabel;
    }

     public PaymentWorksDataTransformationService getPaymentWorksDataTransformationService() {
        return paymentWorksDataTransformationService;
    }

    public void setPaymentWorksDataTransformationService(PaymentWorksDataTransformationService paymentWorksDataTransformationService) {
        this.paymentWorksDataTransformationService = paymentWorksDataTransformationService;
    }
    
}
