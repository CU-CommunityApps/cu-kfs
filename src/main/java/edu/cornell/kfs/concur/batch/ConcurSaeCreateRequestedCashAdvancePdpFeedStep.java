package edu.cornell.kfs.concur.batch;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvancePdpFeedService;

public class ConcurSaeCreateRequestedCashAdvancePdpFeedStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger(ConcurSaeCreateRequestedCashAdvancePdpFeedStep.class);
    protected ConcurSaeCreateRequestedCashAdvancePdpFeedService concurSaeCreateRequestedCashAdvancePdpFeedService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getConcurSaeCreateRequestedCashAdvancePdpFeedService().createPdpFeedsFromSaeRequestedCashAdvances();
        return true;
    }

    public ConcurSaeCreateRequestedCashAdvancePdpFeedService getConcurSaeCreateRequestedCashAdvancePdpFeedService() {
        return concurSaeCreateRequestedCashAdvancePdpFeedService;
    }

    public void setConcurSaeCreateRequestedCashAdvancePdpFeedService(
            ConcurSaeCreateRequestedCashAdvancePdpFeedService concurSaeCreateRequestedCashAdvancePdpFeedService) {
        this.concurSaeCreateRequestedCashAdvancePdpFeedService = concurSaeCreateRequestedCashAdvancePdpFeedService;
    }

}
