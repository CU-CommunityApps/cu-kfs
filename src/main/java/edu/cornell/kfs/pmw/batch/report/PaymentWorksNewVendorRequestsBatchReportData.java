package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksEmailableReportData;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksNewVendorRequestsBatchReportData extends PaymentWorksEmailableReportData {
    
    private List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed;
    
    public PaymentWorksNewVendorRequestsBatchReportData() {
        super();
        this.pmwVendorsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportRawDataItem>();
    }
    
    public PaymentWorksNewVendorRequestsBatchReportData (PaymentWorksBatchReportSummaryItem recordsFoundToProcessSummary,
           PaymentWorksBatchReportSummaryItem recordsThatCouldNotBeProcessedSummary,
           PaymentWorksBatchReportSummaryItem recordsProcessedSummary,
           PaymentWorksBatchReportSummaryItem recordsWithProcessingErrorsSummary,
           PaymentWorksBatchReportSummaryItem recordsGeneratingExceptionSummary,
           List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed,
           List<PaymentWorksBatchReportVendorItem> recordsProcessed,
           List<PaymentWorksBatchReportVendorItem> recordsWithProcessingErrors) {
        super(recordsFoundToProcessSummary, recordsThatCouldNotBeProcessedSummary, recordsProcessedSummary, 
              recordsWithProcessingErrorsSummary, recordsGeneratingExceptionSummary, recordsProcessed, recordsWithProcessingErrors);
        this.pmwVendorsThatCouldNotBeProcessed = pmwVendorsThatCouldNotBeProcessed;
    }

    public List<PaymentWorksBatchReportRawDataItem> getPmwVendorsThatCouldNotBeProcessed() {
        return pmwVendorsThatCouldNotBeProcessed;
    }

    public void setPmwVendorsThatCouldNotBeProcessed(List<PaymentWorksBatchReportRawDataItem> pmwVendorsThatCouldNotBeProcessed) {
        this.pmwVendorsThatCouldNotBeProcessed = pmwVendorsThatCouldNotBeProcessed;
    }

    public void addPmwVendorThatCouldNotBeProcessed(PaymentWorksBatchReportRawDataItem pmwVendorThatCouldNotBeProcessed) {
        if (this.pmwVendorsThatCouldNotBeProcessed == null) {
            this.pmwVendorsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportRawDataItem>();
        }
        this.pmwVendorsThatCouldNotBeProcessed.add(pmwVendorThatCouldNotBeProcessed);
    }

    @Override
    public String retrieveReportName() {
        return PaymentWorksConstants.PaymentWorksBatchReportNames.NEW_VENDOR_REQUESTS_REPORT_NAME;
    }
    
    @Override
    public void populateOutstandingSummaryItemsForReport() {
        super.populateSummaryItemsForReport(this.getPmwVendorsThatCouldNotBeProcessed().size(), 0);
        getRecordsThatCouldNotBeProcessedSummary().setRecordCount(getPmwVendorsThatCouldNotBeProcessed().size());
    }
    
    public List<PaymentWorksBatchReportRawDataItem> retrieveUnprocessablePaymentWorksVendors() {
        return this.getPmwVendorsThatCouldNotBeProcessed();
    }
    
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsProcessed() {
        return super.getRecordsProcessed();
    }
    
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithProcessingErrors() {
        return super.getRecordsWithProcessingErrors();
    }

}
