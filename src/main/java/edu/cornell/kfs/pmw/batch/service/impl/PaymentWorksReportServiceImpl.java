package edu.cornell.kfs.pmw.batch.service.impl;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportEmailService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public abstract class PaymentWorksReportServiceImpl implements PaymentWorksReportService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksReportServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksReportEmailService paymentWorksReportEmailService;
    protected ReportWriterService reportWriterService;
    
    protected String toAddress = null;
    protected String fromAddress = null;
    protected String reportFileNamePrefix = null;
    protected String reportTitle = null;
    protected String summarySubTitle = null;
    protected String processedSubTitle = null;
    protected String processingErrorsSubTitle = null;
    protected String unprocessedSubTitle = null;
    protected String paymentWorksVendorIdLabel = null;
    protected String submittedDateLabel = null;
    protected String vendorTypeLabel = null;
    protected String vendorNameLabel = null;
    protected String taxIdTypeLabel = null;
    protected String vendorSubmitterEmailLabel = null;
    protected String initiatorNetidLabel = null;
    protected String errorsLabel = null;
    protected String kfsVendorNumberLabel = null;
    
    public void sendEmailThatNoDataWasFoundToProcess(List<String> emailSubjectItems, List<String> emailBodyItems) {
        LOG.info("sendEmailThatNoDataWasFoundToProcess: Preparing to send email that no data was found to process.");
        String body = constructStringFromListItemsDoubleSeparatedByDelimiter(emailBodyItems, System.lineSeparator());
        String subject = constructStringFromListItemsDoubleSeparatedByDelimiter(emailSubjectItems, KFSConstants.BLANK_SPACE);
        getPaymentWorksReportEmailService().sendEmail(getToAddress(), getFromAddress(), subject, body);
        LOG.info("sendEmailThatNoDataWasFoundToProcess: Email was sent that no data was found to process.");
    }
    
    private String constructStringFromListItemsDoubleSeparatedByDelimiter(List<String> itemsToUse, String delimitter) {
        StringBuilder sbText = new StringBuilder();
        for (String singleItem : itemsToUse) {
            sbText.append(singleItem).append(delimitter).append(delimitter);
        }
        return sbText.toString();
    }
    
    public PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorProcessed) {
        return createBatchReportVendorItem(pmwVendorProcessed, (List<String>) null);
    }

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
                                                     pmwVendorWithErrors.getKfsVendorHeaderGeneratedIdentifier(),
                                                     pmwVendorWithErrors.getKfsVendorDetailAssignedIdentifier(),
                                                     pmwVendorWithErrors.getKfsAchDocumentNumber(),
                                                     pmwVendorWithErrors.getBankAcctNameOnAccount(),
                                                     errorMessages);
    }
    
    public PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorWithErrors, String errorMessage) {
        List<String> errorMessages = new ArrayList<String>();
        errorMessages.add(errorMessage);
        return createBatchReportVendorItem(pmwVendorWithErrors, errorMessages);
    }

    protected void initializeReportTitleAndFileName(String fileNamePrefixToUse, String reportTitleToUse) {
        LOG.info("initializeReportTitleAndFileName: entered  fileNamePrefixToUse = " + fileNamePrefixToUse + "  reportTitleToUse = " + reportTitleToUse);
        LOG.info("initializeReportTitleAndFileName: this.reportFileNamePrefix = " + this.reportFileNamePrefix + "  this.reportTitle = " + this.reportTitle);
        if (ObjectUtils.isNull(this.reportWriterService)) {
            LOG.info("initializeReportTitleAndFileName: getReportWriterService seen as null");
        } else {
            LOG.info("initializeReportTitleAndFileName: getReportWriterService returned something");
        }
        this.reportWriterService.setFileNamePrefix(fileNamePrefixToUse);
        this.reportWriterService.setTitle(reportTitleToUse);
        this.reportWriterService.initialize();
        this.reportWriterService.writeNewLines(2);
    }
    
    
    protected void finalizeReport() {
        LOG.debug("finalizeReport, entered");
        this.reportWriterService.writeNewLines(3);
        this.reportWriterService.writeFormattedMessageLine(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.END_OF_REPORT_MESSAGE));
        this.reportWriterService.destroy();
    }
    
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
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

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setReportFileNamePrefix(String reportFileNamePrefix) {
        this.reportFileNamePrefix = reportFileNamePrefix;
    }
    
    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }
    
    public void setSummarySubTitle(String summarySubTitle) {
        this.summarySubTitle = summarySubTitle;
    }
    
    public void setProcessedSubTitle(String processedSubTitle) {
        this.processedSubTitle = processedSubTitle;
    }
    
    public void setProcessingErrorsSubTitle(String processingErrorsSubTitle) {
        this.processingErrorsSubTitle = processingErrorsSubTitle;
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
    
    public String getKfsVendorNumberLabel() {
        if (ObjectUtils.isNull(kfsVendorNumberLabel)) {
            setKfsVendorNumberLabel(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_KFS_VENDOR_NUMBER_LABEL));
        }
        return kfsVendorNumberLabel;
    }

    public void setKfsVendorNumberLabel(String kfsVendorNumberLabel) {
        this.kfsVendorNumberLabel = kfsVendorNumberLabel;
    }
    
    public abstract String getToAddress();
    
    public abstract String getFromAddress();
    
    public abstract String getReportFileNamePrefix();
    
    public abstract String getReportTitle();
    
    public abstract String getSummarySubTitle();
    
    public abstract String getProcessedSubTitle();
    
    public abstract String getProcessingErrorsSubTitle();
    
    public abstract String getUnprocessedSubTitle();
    
    public abstract ReportWriterService getReportWriterService();

    public abstract void setReportWriterService(ReportWriterService reportWriterService);

}
