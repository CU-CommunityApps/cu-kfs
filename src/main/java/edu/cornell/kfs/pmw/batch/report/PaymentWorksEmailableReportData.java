package edu.cornell.kfs.pmw.batch.report;

import java.util.List;

public interface PaymentWorksEmailableReportData {
    List<PaymentWorksBatchReportRawDataItem> retrieveUnprocessablePaymentWorksVendors();
    List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsProcessed();
    List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithProcessingErrors();
    void populateOutstandingSummaryItemsForReport();
    String retrieveReportName();
}
