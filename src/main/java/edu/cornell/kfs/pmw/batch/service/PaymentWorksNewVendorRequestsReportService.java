package edu.cornell.kfs.pmw.batch.service;

import java.util.List;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;

public interface PaymentWorksNewVendorRequestsReportService extends PaymentWorksReportService {

    PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorWithErrors, List<String> errorMessages);
    
    PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorProcessed);
    
    void generateAndEmailProcessingReport(PaymentWorksNewVendorRequestsBatchReportData reportData);
    
}
