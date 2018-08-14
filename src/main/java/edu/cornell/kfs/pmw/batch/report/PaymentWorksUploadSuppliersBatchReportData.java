package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksUploadSuppliersBatchReportData extends PaymentWorksEmailableReportData {

    private PaymentWorksBatchReportSummaryItem recordsProcessedByPaymentWorksSummary;
    private List<String> globalMessages;
    private boolean updatedKfsAndPaymentWorksSuccessfully;

    public PaymentWorksUploadSuppliersBatchReportData() {
        super();
        this.recordsProcessedByPaymentWorksSummary = new PaymentWorksBatchReportSummaryItem();
        this.globalMessages = new ArrayList<>();
        this.updatedKfsAndPaymentWorksSuccessfully = false;
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
            List<String> globalMessages,
            boolean updatedKfsAndPaymentWorksSuccessfully) {
        super(recordsFoundToProcessSummary, recordsThatCouldNotBeProcessedSummary,
                recordsProcessedSummary, recordsWithProcessingErrorsSummary,
                recordsGeneratingExceptionSummary, recordsProcessed, recordsWithProcessingErrors);
        this.recordsProcessedByPaymentWorksSummary = recordsProcessedByPaymentWorksSummary;
        this.globalMessages = globalMessages;
        this.updatedKfsAndPaymentWorksSuccessfully = updatedKfsAndPaymentWorksSuccessfully;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsProcessedByPaymentWorksSummary() {
        return recordsProcessedByPaymentWorksSummary;
    }

    public void setRecordsProcessedByPaymentWorksSummary(PaymentWorksBatchReportSummaryItem recordsProcessedByPaymentWorksSummary) {
        this.recordsProcessedByPaymentWorksSummary = recordsProcessedByPaymentWorksSummary;
    }

    public List<String> getGlobalMessages() {
        return globalMessages;
    }

    public void setGlobalMessages(List<String> globalMessages) {
        this.globalMessages = globalMessages;
    }

    public boolean isUpdatedKfsAndPaymentWorksSuccessfully() {
        return updatedKfsAndPaymentWorksSuccessfully;
    }

    public void setUpdatedKfsAndPaymentWorksSuccessfully(boolean updatedKfsAndPaymentWorksSuccessfully) {
        this.updatedKfsAndPaymentWorksSuccessfully = updatedKfsAndPaymentWorksSuccessfully;
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
