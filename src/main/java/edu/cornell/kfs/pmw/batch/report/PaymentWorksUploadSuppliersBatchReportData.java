package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksUploadSuppliersBatchReportData extends PaymentWorksEmailableReportData {

    private PaymentWorksBatchReportSummaryItem recordsProcessedByPaymentWorksSummary;
    private List<String> sharedErrorMessages;

    public PaymentWorksUploadSuppliersBatchReportData() {
        super();
        this.recordsProcessedByPaymentWorksSummary = new PaymentWorksBatchReportSummaryItem();
        this.sharedErrorMessages = new ArrayList<>();
    }

    public PaymentWorksUploadSuppliersBatchReportData(
            PaymentWorksBatchReportSummaryItem recordsFoundToProcessSummary,
            PaymentWorksBatchReportSummaryItem recordsThatCouldNotBeProcessedSummary,
            PaymentWorksBatchReportSummaryItem recordsProcessedSummary,
            PaymentWorksBatchReportSummaryItem recordsWithProcessingErrorsSummary,
            PaymentWorksBatchReportSummaryItem recordsGeneratingExceptionSummary,
            PaymentWorksBatchReportSummaryItem recordsProcessedByPaymentWorksSummary,
            List<PaymentWorksBatchReportVendorItem> recordsProcessed,
            List<PaymentWorksBatchReportVendorItem> recordsWithProcessingErrors,
            List<String> sharedErrorMessages) {
        super(recordsFoundToProcessSummary, recordsThatCouldNotBeProcessedSummary,
                recordsProcessedSummary, recordsWithProcessingErrorsSummary,
                recordsGeneratingExceptionSummary, recordsProcessed, recordsWithProcessingErrors);
        this.recordsProcessedByPaymentWorksSummary = recordsProcessedByPaymentWorksSummary;
        this.sharedErrorMessages = sharedErrorMessages;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsProcessedByPaymentWorksSummary() {
        return recordsProcessedByPaymentWorksSummary;
    }

    public void setRecordsProcessedByPaymentWorksSummary(PaymentWorksBatchReportSummaryItem recordsProcessedByPaymentWorksSummary) {
        this.recordsProcessedByPaymentWorksSummary = recordsProcessedByPaymentWorksSummary;
    }

    public List<String> getSharedErrorMessages() {
        return sharedErrorMessages;
    }

    public void setSharedErrorMessages(List<String> sharedErrorMessages) {
        this.sharedErrorMessages = sharedErrorMessages;
    }

    @Override
    public void populateOutstandingSummaryItemsForReport() {
        super.populateSummaryItemsForReport(0, 0);
    }

    @Override
    public String retrieveReportName() {
        return PaymentWorksConstants.PaymentWorksBatchReportNames.NEW_VENDOR_REQUESTS_SUPPLIER_UPLOAD_REPORT_NAME;
    }

}
