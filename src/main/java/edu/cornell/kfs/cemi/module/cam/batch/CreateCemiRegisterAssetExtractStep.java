package edu.cornell.kfs.cemi.module.cam.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cam.batch.service.CemiRegisterAssetExtractService;

public class CreateCemiRegisterAssetExtractStep extends AbstractStep {

    private CemiRegisterAssetExtractService cemiRegisterAssetExtractService;
    
    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiRegisterAssetExtractService.resetState();
        cemiRegisterAssetExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        //Phase 2: Loop through result set to create all data in the extract tables
        cemiRegisterAssetExtractService.generateIntermediateExtractData(jobRunDate);
        //Phase 3: Create single multi-tabbed file.
        cemiRegisterAssetExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiRegisterAssetExtractService getCemiRegisterAssetExtractService() {
        return cemiRegisterAssetExtractService;
    }

    public void setCemiRegisterAssetExtractService(CemiRegisterAssetExtractService cemiRegisterAssetExtractService) {
        this.cemiRegisterAssetExtractService = cemiRegisterAssetExtractService;
    }

}
