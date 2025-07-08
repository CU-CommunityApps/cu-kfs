package edu.cornell.kfs.pdp.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;

/**
 * Batch step for reading Payee ACH Account additions or updates
 * from external files (such as .csv files from Workday). 
 */
public class PayeeACHAccountExtractStep extends AbstractStep {

    private PayeeACHAccountExtractService payeeACHAccountExtractService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        return payeeACHAccountExtractService.processACHBatchDetails();
    }

    public void setPayeeACHAccountExtractService(PayeeACHAccountExtractService payeeACHAccountExtractService) {
        this.payeeACHAccountExtractService = payeeACHAccountExtractService;
    }

}
