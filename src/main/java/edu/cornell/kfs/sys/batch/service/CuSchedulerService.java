package edu.cornell.kfs.sys.batch.service;

import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.sys.batch.service.SchedulerService;

public interface CuSchedulerService extends SchedulerService {

    void scheduleExceptionMessageJob(PersistedMessage message, String description);
    void scheduleDelayedAsyncCallJob(PersistedMessage message, String description);

}
