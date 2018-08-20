package edu.cornell.kfs.concur.batch;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;

public class ConcurRequestExtractCreatePdpFeedStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(ConcurRequestExtractCreatePdpFeedStep.class);
    protected ConcurRequestExtractCreatePdpFeedService concurRequestExtractCreatePdpFeedService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getConcurRequestExtractCreatePdpFeedService().createPdpFeedsFromRequestExtracts();
        return true;
    }

    public void setConcurRequestExtractCreatePdpFeedService(
            ConcurRequestExtractCreatePdpFeedService concurRequestExtractCreatePdpFeedService) {
        this.concurRequestExtractCreatePdpFeedService = concurRequestExtractCreatePdpFeedService;
    }

    public ConcurRequestExtractCreatePdpFeedService getConcurRequestExtractCreatePdpFeedService() {
        return concurRequestExtractCreatePdpFeedService;
    }
}
