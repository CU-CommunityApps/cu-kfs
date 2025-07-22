package edu.cornell.kfs.sys.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;

public class PurgeTablesStep extends AbstractStep {
    
    private TablesPurgeService tablesPurgeService;
    
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        getTablesPurgeService().purgeRecords(jobRunDate.toLocalDate());
        return true;
    }

    public TablesPurgeService getTablesPurgeService() {
        return tablesPurgeService;
    }

    public void setTablesPurgeService(TablesPurgeService tablesPurgeService) {
        this.tablesPurgeService = tablesPurgeService;
    }

}
