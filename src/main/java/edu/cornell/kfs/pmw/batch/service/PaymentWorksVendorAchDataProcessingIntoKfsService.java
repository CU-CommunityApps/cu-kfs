package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;

public interface PaymentWorksVendorAchDataProcessingIntoKfsService {
    
    boolean createValidateAndRouteKfsPayeeAch(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData);
}
