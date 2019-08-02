package edu.cornell.kfs.rass.batch.service;

import edu.cornell.kfs.rass.batch.RassBatchJobReport;

public interface RassReportService {
    
    void writeBatchJobReport(RassBatchJobReport rassBatchJobReport);
    
    void sendReportEmail();
    
}
