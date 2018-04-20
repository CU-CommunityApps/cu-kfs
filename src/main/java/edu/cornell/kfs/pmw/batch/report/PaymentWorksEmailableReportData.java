package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;

public abstract class PaymentWorksEmailableReportData {
    
    protected PaymentWorksBatchReportSummaryItem recordsFoundToProcessSummary;
    protected PaymentWorksBatchReportSummaryItem recordsThatCouldNotBeProcessedSummary;
    protected PaymentWorksBatchReportSummaryItem recordsProcessedSummary;
    protected PaymentWorksBatchReportSummaryItem recordsWithProcessingErrorsSummary;
    protected PaymentWorksBatchReportSummaryItem recordsGeneratingExceptionSummary;
    protected List<PaymentWorksBatchReportVendorItem> recordsProcessed;
    protected List<PaymentWorksBatchReportVendorItem> recordsWithProcessingErrors;
    
    public PaymentWorksEmailableReportData() {
        this.recordsFoundToProcessSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsThatCouldNotBeProcessedSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsProcessedSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsWithProcessingErrorsSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsGeneratingExceptionSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.recordsWithProcessingErrors = new ArrayList<PaymentWorksBatchReportVendorItem>();
    }
    
    public PaymentWorksEmailableReportData(PaymentWorksBatchReportSummaryItem recordsFoundToProcessSummary,
            PaymentWorksBatchReportSummaryItem recordsThatCouldNotBeProcessedSummary,
            PaymentWorksBatchReportSummaryItem recordsProcessedSummary,
            PaymentWorksBatchReportSummaryItem recordsWithProcessingErrorsSummary,
            PaymentWorksBatchReportSummaryItem recordsGeneratingExceptionSummary,
            List<PaymentWorksBatchReportVendorItem> recordsProcessed,
            List<PaymentWorksBatchReportVendorItem> recordsWithProcessingErrors) {
        this.recordsFoundToProcessSummary = recordsFoundToProcessSummary;
        this.recordsThatCouldNotBeProcessedSummary = recordsThatCouldNotBeProcessedSummary;
        this.recordsProcessedSummary = recordsProcessedSummary;
        this.recordsWithProcessingErrorsSummary = recordsWithProcessingErrorsSummary;
        this.recordsGeneratingExceptionSummary = recordsGeneratingExceptionSummary;
        this.recordsProcessed = recordsProcessed;
        this.recordsWithProcessingErrors = recordsWithProcessingErrors;
    }
    
    public PaymentWorksBatchReportSummaryItem getRecordsFoundToProcessSummary() {
        return recordsFoundToProcessSummary;
    }

    public void setRecordsFoundToProcessSummary(PaymentWorksBatchReportSummaryItem recordsFoundToProcessSummary) {
        this.recordsFoundToProcessSummary = recordsFoundToProcessSummary;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsThatCouldNotBeProcessedSummary() {
        return recordsThatCouldNotBeProcessedSummary;
    }

    public void setRecordsThatCouldNotBeProcessedSummary(PaymentWorksBatchReportSummaryItem recordsThatCouldNotBeProcessedSummary) {
        this.recordsThatCouldNotBeProcessedSummary = recordsThatCouldNotBeProcessedSummary;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsProcessedSummary() {
        return recordsProcessedSummary;
    }

    public void setRecordsProcessedSummary(PaymentWorksBatchReportSummaryItem recordsProcessedSummary) {
        this.recordsProcessedSummary = recordsProcessedSummary;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsWithProcessingErrorsSummary() {
        return recordsWithProcessingErrorsSummary;
    }

    public void setRecordsWithProcessingErrorsSummary(PaymentWorksBatchReportSummaryItem recordsWithProcessingErrorsSummary) {
        this.recordsWithProcessingErrorsSummary = recordsWithProcessingErrorsSummary;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsGeneratingExceptionSummary() {
        return recordsGeneratingExceptionSummary;
    }

    public void setRecordsGeneratingExceptionSummary(PaymentWorksBatchReportSummaryItem recordsGeneratingExceptionSummary) {
        this.recordsGeneratingExceptionSummary = recordsGeneratingExceptionSummary;
    }
    
    public List<PaymentWorksBatchReportVendorItem> getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(List<PaymentWorksBatchReportVendorItem> recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public void addRecordProcessed(PaymentWorksBatchReportVendorItem recordProcessed) {
        if (this.recordsProcessed == null) {
            this.recordsProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.recordsProcessed.add(recordProcessed);
    }
    
    public List<PaymentWorksBatchReportVendorItem> getRecordsWithProcessingErrors() {
        return recordsWithProcessingErrors;
    }

    public void setRecordsWithProcessingErrors(List<PaymentWorksBatchReportVendorItem> recordsWithProcessingErrors) {
        this.recordsWithProcessingErrors = recordsWithProcessingErrors;
    }
    
    public void addRecordWithProcessingErrors(PaymentWorksBatchReportVendorItem recordWithProcessingErrors) {
        if (this.recordsWithProcessingErrors == null) {
            this.recordsWithProcessingErrors = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.recordsWithProcessingErrors.add(recordWithProcessingErrors);
    }
    
    public void populateSummaryItemsForReport(int recordsThatCouldNotBeProcessCount, int recordsGeneratingExceptionsCount) {
        getRecordsProcessedSummary().setRecordCount(getRecordsProcessed().size());
        getRecordsWithProcessingErrorsSummary().setRecordCount(getRecordsWithProcessingErrors().size());
        getRecordsThatCouldNotBeProcessedSummary().setRecordCount(recordsThatCouldNotBeProcessCount);
        getRecordsGeneratingExceptionSummary().setRecordCount(recordsGeneratingExceptionsCount);
    }

    public abstract void populateOutstandingSummaryItemsForReport();
    
    public abstract String retrieveReportName();

}
