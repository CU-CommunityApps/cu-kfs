package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;

public class ConcurEventNotificationProcessingStep extends AbstractStep {
    
    protected ConcurEventNotificationProcessingService concurEventNotificationProcessingService;
    protected ConcurAccessTokenService concurAccessTokenService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
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

}
