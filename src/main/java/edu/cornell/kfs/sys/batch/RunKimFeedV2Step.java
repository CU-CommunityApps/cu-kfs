package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.kim.batch.service.KimFeedService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class RunKimFeedV2Step extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private KimFeedService kimFeedService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        try {
            LOG.info("execute: Starting run of KFS KIM feed");
            kimFeedService.processPersonDataMarkedForDisabling();
            kimFeedService.processPersonDataChanges();
            if (shouldSkipDeltaFlagUpdates()) {
                LOG.info("execute: Skipping the step of marking the EDW data rows as read");
            } else {
                kimFeedService.markPersonDataChangesAsRead();
            }
            kimFeedService.flushPersonCache();
            LOG.info("execute: Successfully finished running KFS KIM feed");
            
            return true;
        } catch (Exception e) {
            LOG.error("execute: Unexpected error encountered when running KFS KIM feed", e);
            throw e;
        }
    }

    protected boolean shouldSkipDeltaFlagUpdates() {
        return parameterService.getParameterValueAsBoolean(
                KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.BATCH_COMPONENT,
                CUKFSParameterKeyConstants.KIM_FEED_SKIP_DELTA_FLAG_UPDATES);
    }

    public void setKimFeedService(KimFeedService kimFeedService) {
        this.kimFeedService = kimFeedService;
    }

}
