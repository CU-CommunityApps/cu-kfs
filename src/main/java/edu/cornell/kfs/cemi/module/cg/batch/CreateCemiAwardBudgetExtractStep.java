package edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardBudgetExtractService;

public class CreateCemiAwardBudgetExtractStep extends AbstractStep {

    private CemiAwardBudgetExtractService cemiAwardBudgetExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        cemiAwardBudgetExtractService.logSystemParameterValues();
        cemiAwardBudgetExtractService.resetState();
        cemiAwardBudgetExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        cemiAwardBudgetExtractService.generateIntermediateExtractData(jobRunDate);
        cemiAwardBudgetExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiAwardBudgetExtractService getCemiAwardBudgetExtractService() {
        return cemiAwardBudgetExtractService;
    }

    public void setCemiAwardBudgetExtractService(CemiAwardBudgetExtractService cemiAwardBudgetExtractService) {
        this.cemiAwardBudgetExtractService = cemiAwardBudgetExtractService;
    }

}
