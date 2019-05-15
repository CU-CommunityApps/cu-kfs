package edu.cornell.kfs.sys.batch;

import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import cynergy.CynergyKimFeed;

public class RunKimFeedStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger(RunKimFeedStep.class);

    private static final String DELTA_RUN_TYPE = "delta";

    private Properties kimFeedProperties;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        try {
            LOG.info("execute: Starting run of KIM feed");
            CynergyKimFeed.runKimFeed(kimFeedProperties, DELTA_RUN_TYPE);
            LOG.info("execute: Successfully finished running KIM feed");
            return true;
        } catch (Exception e) {
            LOG.error("execute: Unexpected error encountered when running KIM feed", e);
            return false;
        }
    }

    public void setKimFeedProperties(Properties kimFeedProperties) {
        this.kimFeedProperties = kimFeedProperties;
    }

}
