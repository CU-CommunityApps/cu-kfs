package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;

public class ConcurEventNotificationProcessingStep extends AbstractStep {
    
    protected ConcurEventNotificationProcessingService concurEventNotificationProcessingService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        concurEventNotificationProcessingService.processConcurEventNotifications();
        return true;
    }

    public void setConcurEventNotificationProcessingService(ConcurEventNotificationProcessingService concurEventNotificationProcessingService) {
        this.concurEventNotificationProcessingService = concurEventNotificationProcessingService;
    }

}
