package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.function.Consumer;

import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksUploadSuppliersReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksUploadSuppliersReportServiceImpl extends PaymentWorksReportServiceImpl implements PaymentWorksUploadSuppliersReportService {

    @Override
    public void generateAndEmailProcessingReport(PaymentWorksUploadSuppliersBatchReportData reportData) {
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
        return getPropertyAndInitializeIfNecessary(unprocessedSubTitle, super::setUnprocessedSubTitle,
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_UNPROCESSED_VENDORS_SUB_TITLE);
    }

    private String getPropertyAndInitializeIfNecessary(String currentValue, Consumer<String> propertySetter, String parameterName) {
        if (ObjectUtils.isNull(currentValue)) {
            String parameterValue = getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(parameterName);
            propertySetter.accept(parameterValue);
        }
        return currentValue;
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
