package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.service.ConcurReportsService;

public class ConcurEventNotificationProcessingStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    protected ConcurEventNotificationProcessingService concurEventNotificationProcessingService;
    protected ConcurReportsService concurReportsService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        try {
            concurReportsService.initializeTemporaryAccessToken();
            concurEventNotificationProcessingService.processConcurEventNotifications();
        } catch (RuntimeException e) {
            LOG.error("execute, An unexpected error occurred during event notification processing", e);
            throw e;
        } finally {
            concurReportsService.clearTemporaryAccessToken();
        }
        return true;
    }

    public void setConcurEventNotificationProcessingService(ConcurEventNotificationProcessingService concurEventNotificationProcessingService) {
        this.concurEventNotificationProcessingService = concurEventNotificationProcessingService;       
    }

    public ConcurEventNotificationProcessingService getConcurEventNotificationProcessingService() {
        return concurEventNotificationProcessingService;
    }

    public ConcurReportsService getConcurReportsService() {
        return concurReportsService;
    }

    public void setConcurReportsService(ConcurReportsService concurReportsService) {
        this.concurReportsService = concurReportsService;
    }

}
