package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksNewVendorRequestsBatchReportData implements PaymentWorksEmailableReportData {
    
    private PaymentWorksBatchReportSummaryItem pendingNewVendorsFoundInPmw;
    private PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsThatCouldNotBeProcessed;
    private PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsProcessed;
    private PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsWithProcessingErrors;
    private List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed;
    private List<PaymentWorksBatchReportVendorItem> pmwVendorsProcessed;
    private List<PaymentWorksBatchReportVendorItem> pmwVendorsWithErrorsWhenProcessingAttempted;
    
    public PaymentWorksNewVendorRequestsBatchReportData() {
        this.pendingNewVendorsFoundInPmw = new PaymentWorksBatchReportSummaryItem();
        this.pendingPaymentWorksVendorsThatCouldNotBeProcessed = new PaymentWorksBatchReportSummaryItem();
        this.pendingPaymentWorksVendorsProcessed = new PaymentWorksBatchReportSummaryItem();
        this.pendingPaymentWorksVendorsWithProcessingErrors = new PaymentWorksBatchReportSummaryItem();
        this.pmwVendorsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportRawDataItem>();
        this.pmwVendorsProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.pmwVendorsWithErrorsWhenProcessingAttempted = new ArrayList<PaymentWorksBatchReportVendorItem>();
    }
    
    public PaymentWorksNewVendorRequestsBatchReportData (PaymentWorksBatchReportSummaryItem pendingNewVendorsFoundInPmw,
           PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsThatCouldNotBeProcessed,
           PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsProcessed,
           PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsWithProcessingErrors,
           List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed,
           List<PaymentWorksBatchReportVendorItem> pmwVendorsProcessed,
           List<PaymentWorksBatchReportVendorItem> pmwVendorsWithErrorsWhenProcessingAttempted) {
        this.pendingNewVendorsFoundInPmw = pendingNewVendorsFoundInPmw;
        this.pendingPaymentWorksVendorsThatCouldNotBeProcessed = pendingPaymentWorksVendorsThatCouldNotBeProcessed;
        this.pendingPaymentWorksVendorsProcessed = pendingPaymentWorksVendorsProcessed;
        this.pendingPaymentWorksVendorsWithProcessingErrors = pendingPaymentWorksVendorsWithProcessingErrors;
        this.pmwVendorsThatCouldNotBeProcessed = pmwVendorsThatCouldNotBeProcessed;
        this.pmwVendorsProcessed = pmwVendorsProcessed;
        this.pmwVendorsWithErrorsWhenProcessingAttempted = pmwVendorsWithErrorsWhenProcessingAttempted;
    }

    public PaymentWorksBatchReportSummaryItem getPendingNewVendorsFoundInPmw() {
        return pendingNewVendorsFoundInPmw;
    }

    public void setPendingNewVendorsFoundInPmw(PaymentWorksBatchReportSummaryItem pendingNewVendorsFoundInPmw) {
        this.pendingNewVendorsFoundInPmw = pendingNewVendorsFoundInPmw;
    }

    public PaymentWorksBatchReportSummaryItem getPendingPaymentWorksVendorsThatCouldNotBeProcessed() {
        return pendingPaymentWorksVendorsThatCouldNotBeProcessed;
    }

    public void setPendingPaymentWorksVendorsThatCouldNotBeProcessed(
            PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsThatCouldNotBeProcessed) {
        this.pendingPaymentWorksVendorsThatCouldNotBeProcessed = pendingPaymentWorksVendorsThatCouldNotBeProcessed;
    }

    public PaymentWorksBatchReportSummaryItem getPendingPaymentWorksVendorsProcessed() {
        return pendingPaymentWorksVendorsProcessed;
    }

    public void setPendingPaymentWorksVendorsProcessed(
            PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsProcessed) {
        this.pendingPaymentWorksVendorsProcessed = pendingPaymentWorksVendorsProcessed;
    }

    public PaymentWorksBatchReportSummaryItem getPendingPaymentWorksVendorsWithProcessingErrors() {
        return pendingPaymentWorksVendorsWithProcessingErrors;
    }

    public void setPendingPaymentWorksVendorsWithProcessingErrors(
            PaymentWorksBatchReportSummaryItem pendingPaymentWorksVendorsWithProcessingErrors) {
        this.pendingPaymentWorksVendorsWithProcessingErrors = pendingPaymentWorksVendorsWithProcessingErrors;
    }

    public List<PaymentWorksBatchReportRawDataItem> getPmwVendorsThatCouldNotBeProcessed() {
        return pmwVendorsThatCouldNotBeProcessed;
    }

    public void setPmwVendorsThatCouldNotBeProcessed(List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed) {
        this.pmwVendorsThatCouldNotBeProcessed = pmwVendorsThatCouldNotBeProcessed;
    }

    public void addPmwVendorsThatCouldNotBeProcessed(PaymentWorksBatchReportRawDataItem pmwVendorThatCouldNotBeProcessed) {
        if (this.pmwVendorsThatCouldNotBeProcessed == null) {
            this.pmwVendorsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportRawDataItem>();
        }
        this.pmwVendorsThatCouldNotBeProcessed.add(pmwVendorThatCouldNotBeProcessed);
    }

    public List<PaymentWorksBatchReportVendorItem> getPmwVendorsProcessed() {
        return pmwVendorsProcessed;
    }

    public void setPmwVendorsProcessed(List<PaymentWorksBatchReportVendorItem> pmwVendorsProcessed) {
        this.pmwVendorsProcessed = pmwVendorsProcessed;
    }
    
    public void addPmwVendorProcessed(PaymentWorksBatchReportVendorItem pmwVendorProcessed) {
        if (this.pmwVendorsProcessed == null) {
            this.pmwVendorsProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.pmwVendorsProcessed.add(pmwVendorProcessed);
    }

    public List<PaymentWorksBatchReportVendorItem> getPmwVendorsWithErrorsWhenProcessingAttempted() {
        return pmwVendorsWithErrorsWhenProcessingAttempted;
    }

    public void setPmwVendorsWithErrorsWhenProcessingAttempted(List<PaymentWorksBatchReportVendorItem> pmwVendorsWithErrorsWhenProcessingAttempted) {
        this.pmwVendorsWithErrorsWhenProcessingAttempted = pmwVendorsWithErrorsWhenProcessingAttempted;
    }
    
    public void addPmwVendorsWithErrorsWhenProcessingAttempted(PaymentWorksBatchReportVendorItem pmwVendorWithErrorsWhenProcessingAttempted) {
        if (this.pmwVendorsWithErrorsWhenProcessingAttempted == null) {
            this.pmwVendorsWithErrorsWhenProcessingAttempted = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.pmwVendorsWithErrorsWhenProcessingAttempted.add(pmwVendorWithErrorsWhenProcessingAttempted);
    }
    
    @Override
    public String retrieveReportName() {
        return PaymentWorksConstants.PaymentWorksBatchReportNames.NEW_VENDOR_REQUESTS_REPORT_NAME;
    }
    
    @Override
    public void populateOutstandingSummaryItemsForReport() {
        pendingPaymentWorksVendorsThatCouldNotBeProcessed.setRecordCount(pmwVendorsThatCouldNotBeProcessed.size());
        pendingPaymentWorksVendorsProcessed.setRecordCount(pmwVendorsProcessed.size());
        pendingPaymentWorksVendorsWithProcessingErrors.setRecordCount(pmwVendorsWithErrorsWhenProcessingAttempted.size());
    }
    
    @Override
    public List<PaymentWorksBatchReportRawDataItem> retrieveUnprocessablePaymentWorksVendors() {
        return this.getPmwVendorsThatCouldNotBeProcessed();
    }
    
    @Override
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsProcessed() {
        return this.getPmwVendorsProcessed();
    }
    
    @Override
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithProcessingErrors() {
        return this.getPmwVendorsWithErrorsWhenProcessingAttempted();
    }

}
