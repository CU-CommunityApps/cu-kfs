package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;

public interface PaymentWorksUploadSuppliersReportService extends PaymentWorksReportService {

    void generateAndEmailProcessingReport(PaymentWorksUploadSuppliersBatchReportData reportData);

}
