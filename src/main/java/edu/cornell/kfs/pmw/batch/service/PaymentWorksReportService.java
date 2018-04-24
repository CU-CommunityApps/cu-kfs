package edu.cornell.kfs.pmw.batch.service;

import java.lang.String;
import java.util.List;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;

public interface PaymentWorksReportService {
    
    void sendEmailThatNoDataWasFoundToProcess(List<String> emailSubjectItems, List<String> emailBodyItems);
    
    PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorWithErrors, List<String> errorMessages);
    
    PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorWithErrors, String errorMessage);
    
    PaymentWorksBatchReportVendorItem createBatchReportVendorItem(PaymentWorksVendor pmwVendorProcessed);

}
