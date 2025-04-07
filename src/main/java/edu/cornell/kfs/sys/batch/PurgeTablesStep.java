package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;

public class PurgeTablesStep extends AbstractStep {
    
    private DateTimeService dateTimeService;
    private TablesPurgeService tablesPurgeService;
    
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getTablesPurgeService().purgeRecords(getDateTimeService().getLocalDateTime(jobRunDate));
        return true;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public TablesPurgeService getTablesPurgeService() {
        return tablesPurgeService;
    }

    public void setTablesPurgeService(TablesPurgeService tablesPurgeService) {
        this.tablesPurgeService = tablesPurgeService;
    }

}
