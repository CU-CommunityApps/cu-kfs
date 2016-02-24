package edu.cornell.kfs.pdp.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;

/**
 * Batch step for reading Payee ACH Account additions or updates
 * from external files (such as .csv files from Workday). 
 */
public class PayeeACHAccountExtractStep extends AbstractStep {

    private PayeeACHAccountExtractService payeeACHAccountExtractService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return payeeACHAccountExtractService.processACHBatchDetails();
    }

    public void setPayeeACHAccountExtractService(PayeeACHAccountExtractService payeeACHAccountExtractService) {
        this.payeeACHAccountExtractService = payeeACHAccountExtractService;
    }

}
