package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

/**
 * A batch step implementation that does no processing. Intended as a temporary placeholder
 * for cases where a batch step has been removed, but we want the scheduler to continue
 * running the same list of steps while its setup and OpDocs are still in transition.
 */
public class NoOpStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, Ran a no-op step for " + jobName);
        return true;
    }
}
