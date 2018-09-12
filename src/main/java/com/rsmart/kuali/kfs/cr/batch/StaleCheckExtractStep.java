package com.rsmart.kuali.kfs.cr.batch;

import org.kuali.kfs.sys.batch.AbstractStep;
import com.rsmart.kuali.kfs.cr.batch.service.StaleCheckExtractService;

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
