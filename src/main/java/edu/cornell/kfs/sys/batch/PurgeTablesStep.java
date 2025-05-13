package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;

public class PurgeTablesStep extends AbstractStep {
    
    private TablesPurgeService tablesPurgeService;
    
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getTablesPurgeService().purgeRecords(getDateTimeService().getLocalDate(jobRunDate));
        return true;
    }

    public TablesPurgeService getTablesPurgeService() {
        return tablesPurgeService;
    }

    public void setTablesPurgeService(TablesPurgeService tablesPurgeService) {
        this.tablesPurgeService = tablesPurgeService;
    }

}
