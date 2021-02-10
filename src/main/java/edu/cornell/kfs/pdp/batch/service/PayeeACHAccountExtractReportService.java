package edu.cornell.kfs.pdp.batch.service;

import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReportData;

public interface PayeeACHAccountExtractReportService {

    void writeBatchJobReports(PayeeACHAccountExtractReportData achReport);

}
