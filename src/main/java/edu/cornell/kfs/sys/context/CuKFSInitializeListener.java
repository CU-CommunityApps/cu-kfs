package edu.cornell.kfs.sys.context;

import javax.servlet.ServletContextEvent;

import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.KFSInitializeListener;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CuKFSInitializeListener extends KFSInitializeListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        super.contextInitialized(sce);
        if (shouldAllowLocalBatchExecution()) {
            if (isQuartzSchedulingEnabled()) {
                throw new IllegalStateException("CU-specific local batch execution and Quartz scheduling "
                        + "cannot both be enabled");
            }
            // Base code skips this initialize() call if Quartz is disabled, so we add it again in this block.
            SpringContext.getBean(SchedulerService.class).initialize();
        }
    }

    private boolean shouldAllowLocalBatchExecution() {
        return getBooleanProperty(CUKFSConstants.CU_ALLOW_LOCAL_BATCH_EXECUTION_KEY);
    }

    private boolean isQuartzSchedulingEnabled() {
        return getBooleanProperty(KFSPropertyConstants.USE_QUARTZ_SCHEDULING_KEY);
    }

    private boolean getBooleanProperty(String propertyName) {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(propertyName);
    }

}
