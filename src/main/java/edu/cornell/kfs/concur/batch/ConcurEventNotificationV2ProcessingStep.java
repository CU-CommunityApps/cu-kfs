package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.service.ConcurAccessTokenV2Service;

public class ConcurEventNotificationV2ProcessingStep extends AbstractStep {
    
    protected ConcurAccessTokenV2Service concurAccessTokenV2Service;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        String accessToken = concurAccessTokenV2Service.retrieveNewAccessBearerToken();
        return true;
    }

}
