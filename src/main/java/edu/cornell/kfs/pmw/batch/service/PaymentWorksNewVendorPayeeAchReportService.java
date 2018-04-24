package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;

public interface PaymentWorksNewVendorPayeeAchReportService extends PaymentWorksReportService {
    
    void generateAndEmailProcessingReport(PaymentWorksNewVendorPayeeAchBatchReportData reportData);
    
}
