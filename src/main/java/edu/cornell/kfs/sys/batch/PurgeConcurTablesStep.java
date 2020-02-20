package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurTablesPurgeService;

public class PurgeConcurTablesStep extends AbstractStep {
    
    ConcurTablesPurgeService concurTablesPurgeService;
    
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        
        getConcurTablesPurgeService().purgeRecords(jobRunDate);
        return true;
    }

    public ConcurTablesPurgeService getConcurTablesPurgeService() {
        return concurTablesPurgeService;
    }

    public void setConcurTablesPurgeService(ConcurTablesPurgeService concurTablesPurgeService) {
        this.concurTablesPurgeService = concurTablesPurgeService;
    }

}
