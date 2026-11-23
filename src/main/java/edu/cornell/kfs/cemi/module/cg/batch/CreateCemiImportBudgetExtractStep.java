package edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiImportBudgetExtractService;

public class CreateCemiImportBudgetExtractStep extends AbstractStep {

    private CemiImportBudgetExtractService cemiImportBudgetExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiImportBudgetExtractService.logSystemParameterValues(); //logs outbound folder and masking setting
        cemiImportBudgetExtractService.resetState(); //does nothing YET (no existing state to reset)
        cemiImportBudgetExtractService.captureInScopeBusinessObjectKeysToProcessingTable(); //will eventually get the keys needed to query KFS data
        //Phase 2: Loops through result set and saves data to the intermediate tables
        cemiImportBudgetExtractService.generateIntermediateExtractData(jobRunDate);
        //Phase 3: Create single multi-tabbed file.
        cemiImportBudgetExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiImportBudgetExtractService getCemiImportBudgetExtractService() {
        return cemiImportBudgetExtractService;
    }

    public void setCemiImportBudgetExtractService(CemiImportBudgetExtractService cemiImportBudgetExtractService) {
        this.cemiImportBudgetExtractService = cemiImportBudgetExtractService;
    }

}
