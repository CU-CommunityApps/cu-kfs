package  edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardExtractService;

public class CreateCemiAwardExtractStep extends AbstractStep {
    
    private CemiAwardExtractService cemiAwardExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        
        // Phase 1: Obtain the dataset
        cemiAwardExtractService.resetState();
        cemiAwardExtractService.initializeExtractDateSettings();
        cemiAwardExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        
//        /* Phase N: Gather all the raw data elements into tables that will be used to create the data extract. */
//        cemiAwardExtractService.gatherAndPopulateRawDataTables(jobRunDate);
        
        // Phase 2: Loop through in scope dataset transforming the attribute values and saving to database tables.
        cemiAwardExtractService.generateIntermediateExtractData(jobRunDate);
        
        // Phase 3: Create single multi-tabbed file
        cemiAwardExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiAwardExtractService getCemiAwardExtractService() {
        return cemiAwardExtractService;
    }

    public void setCemiAwardExtractService(CemiAwardExtractService cemiAwardExtractService) {
        this.cemiAwardExtractService = cemiAwardExtractService;
    }
    
}
