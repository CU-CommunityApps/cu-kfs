package edu.cornell.kfs.sys.batch;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.kim.KimFeed;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class RunKimFeedStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DELTA_RUN_TYPE = "delta";

    private Properties kimFeedBaseProperties;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        try {
            LOG.info("execute: Starting run of KIM feed");
            Properties kimFeedProperties = buildKimFeedProperties();
            KimFeed.runKimFeed(kimFeedProperties, DELTA_RUN_TYPE);
            LOG.info("execute: Successfully finished running KIM feed");
            return true;
        } catch (Exception e) {
            LOG.error("execute: Unexpected error encountered when running KIM feed", e);
            throw e;
        }
    }

    protected Properties buildKimFeedProperties() {
        Properties kimFeedProperties = new Properties(kimFeedBaseProperties);
        
        String deltasToLoad = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.BATCH_COMPONENT,
                CUKFSParameterKeyConstants.KIM_FEED_DELTAS_TO_LOAD);
        
        kimFeedProperties.setProperty(KimFeed.SKIP_DELTA_FLAG_UPDATES_PROP, Boolean.TRUE.toString());
        
        if (StringUtils.isBlank(deltasToLoad)) {
            throw new IllegalStateException(CUKFSParameterKeyConstants.KIM_FEED_DELTAS_TO_LOAD + " parameter cannot be blank");
        } else if (StringUtils.equals(CUKFSConstants.KimFeedConstants.ALL_UNPROCESSED_DELTAS_MODE, deltasToLoad)) {
            kimFeedProperties.setProperty(KimFeed.LOAD_LATEST_DELTA_ONLY_PROP, KFSConstants.Booleans.FALSE);
            kimFeedProperties.setProperty(KimFeed.LOAD_DELTA_WITH_DATE_PROP, KFSConstants.EMPTY_STRING);
        } else if (StringUtils.equals(CUKFSConstants.KimFeedConstants.LATEST_DATE_ONLY_MODE, deltasToLoad)) {
            kimFeedProperties.setProperty(KimFeed.LOAD_LATEST_DELTA_ONLY_PROP, KFSConstants.Booleans.TRUE);
            kimFeedProperties.setProperty(KimFeed.LOAD_DELTA_WITH_DATE_PROP, KFSConstants.EMPTY_STRING);
        } else {
            kimFeedProperties.setProperty(KimFeed.LOAD_LATEST_DELTA_ONLY_PROP, KFSConstants.Booleans.FALSE);
            kimFeedProperties.setProperty(KimFeed.LOAD_DELTA_WITH_DATE_PROP, deltasToLoad);
        }
        
        return kimFeedProperties;
    }

    public void setKimFeedBaseProperties(Properties kimFeedBaseProperties) {
        this.kimFeedBaseProperties = kimFeedBaseProperties;
    }

}
