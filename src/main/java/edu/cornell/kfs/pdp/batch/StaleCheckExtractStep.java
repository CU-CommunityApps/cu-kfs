package edu.cornell.kfs.pdp.batch;

import org.kuali.kfs.sys.batch.AbstractStep;
import edu.cornell.kfs.pdp.batch.service.StaleCheckExtractService;

import java.util.Date;

public class StaleCheckExtractStep extends AbstractStep {

    private StaleCheckExtractService staleCheckExtractService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return staleCheckExtractService.processStaleCheckBatchFiles();
    }

    public void setStaleCheckExtractService(StaleCheckExtractService staleCheckExtractService) {
        this.staleCheckExtractService = staleCheckExtractService;
    }

}
