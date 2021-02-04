package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.service.AwsSecretService;

public class ConcurEventNotificationProcessingStep extends AbstractStep {
    
    protected ConcurEventNotificationProcessingService concurEventNotificationProcessingService;
    protected ConcurAccessTokenService concurAccessTokenService;
    protected AwsSecretService awsSecretService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return awsSecretService.doWithAwsSecretsCachingEnabled(this::processConcurEventNotifications);
    }

    protected boolean processConcurEventNotifications() {
        concurAccessTokenService.refreshAccessToken();
        concurEventNotificationProcessingService.processConcurEventNotifications();
        return true;
    }

    public void setConcurEventNotificationProcessingService(ConcurEventNotificationProcessingService concurEventNotificationProcessingService) {
        this.concurEventNotificationProcessingService = concurEventNotificationProcessingService;       
    }

    public ConcurEventNotificationProcessingService getConcurEventNotificationProcessingService() {
        return concurEventNotificationProcessingService;
    }
    
    public ConcurAccessTokenService getConcurAccessTokenService() {
        return concurAccessTokenService;
    }

    public void setConcurAccessTokenService(ConcurAccessTokenService concurAccessTokenService) {
        this.concurAccessTokenService = concurAccessTokenService;
    }

    public void setAwsSecretService(AwsSecretService awsSecretService) {
        this.awsSecretService = awsSecretService;
    }

}
