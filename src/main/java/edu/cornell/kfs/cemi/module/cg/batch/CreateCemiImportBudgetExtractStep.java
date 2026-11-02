package edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiImportBudgetExtractService;

public class CreateCemiImportBudgetExtractStep extends AbstractStep {

    private CemiImportBudgetExtractService cemiImportBudgetExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiImportBudgetExtractService.logSystemParameterValues();
        cemiImportBudgetExtractService.resetState();
        cemiImportBudgetExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        //Phase 2: Loop through result set to create all the csv files
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
