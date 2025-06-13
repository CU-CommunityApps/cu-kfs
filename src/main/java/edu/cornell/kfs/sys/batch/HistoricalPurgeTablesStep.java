package edu.cornell.kfs.sys.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.batch.service.HistoricalTablesPurgeService;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalPurgeTablesStep extends AbstractStep {
    
    private HistoricalTablesPurgeService historicalTablesPurgeService;
    
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        getHistoricalTablesPurgeService().purgeRecords(jobRunDate);
        return true;
    }

    public HistoricalTablesPurgeService getHistoricalTablesPurgeService() {
        return historicalTablesPurgeService;
    }

    public void setHistoricalTablesPurgeService(HistoricalTablesPurgeService historicalTablesPurgeService) {
        this.historicalTablesPurgeService = historicalTablesPurgeService;
    }

}
