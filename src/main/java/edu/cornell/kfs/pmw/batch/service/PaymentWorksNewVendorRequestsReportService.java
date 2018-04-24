package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;

public interface PaymentWorksNewVendorRequestsReportService extends PaymentWorksReportService {

    void generateAndEmailProcessingReport(PaymentWorksNewVendorRequestsBatchReportData reportData);

}
