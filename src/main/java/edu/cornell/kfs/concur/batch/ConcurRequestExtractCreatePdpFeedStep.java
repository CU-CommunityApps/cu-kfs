package edu.cornell.kfs.concur.batch;

import java.util.Date;
import org.kuali.kfs.sys.batch.AbstractStep;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;

public class ConcurRequestExtractCreatePdpFeedStep extends AbstractStep {
	
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpFeedStep.class);
    protected ConcurRequestExtractCreatePdpFeedService concurRequestExtractCreatePdpFeedService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        boolean jobCompletedSuccessfully = true;
		
        concurRequestExtractCreatePdpFeedService.createPdpFeedsFromRequestExtracts();
        return jobCompletedSuccessfully;
    }
	
    public void setConcurRequestExtractCreatePdpFeedService(ConcurRequestExtractCreatePdpFeedService concurRequestExtractCreatePdpFeedService) {
        this.concurRequestExtractCreatePdpFeedService = concurRequestExtractCreatePdpFeedService;
    }

    public ConcurRequestExtractCreatePdpFeedService getConcurRequestExtractCreatePdpFeedService() {
        return concurRequestExtractCreatePdpFeedService;
    }
}
