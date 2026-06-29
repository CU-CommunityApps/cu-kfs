package  edu.cornell.kfs.cemi.patterntemplate.batch;

// Name the batch job step class such that it is unique following the pattern CreateCemiDATAEXTRACTNAMEExtractStep
// where DATAEXTRACTNAME is used consistently to represent the SAME data extract everywhere that term is used in
// the set of patterned template files.
//
//      Examples of using this pattern would be the following:
//          createCemiAwardScheduleExtractStep
//          createCemiOrderFromSupplierExtractStep
//          createCemiPaymentElectionExtractStep
//          createCemiRemitToSupplierExtractStep
//          createCemiSupplierExtractStep
//          

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

public class CreateCemiDATAEXTRACTNAMEExtractStep extends AbstractStep {
    
    private CemiDATAEXTRACTNAMEExtractService cemiDATAEXTRACTNAMEExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        
        
        
        // Phase1: Obtain the dataset
        
        // Method required for every extraction. 
        // Intended to perform any action to ensure job runs successfully and produces the data extract file.
        //      At a minimum, on every execution of the batch job after the first one, this method should clear the
        //      table populated by routine captureInScopeBusinessObjectKeysToProcessingTable (called below) from the
        //      previous execution of the job.
        cemiDATAEXTRACTNAMEExtractService.resetState(CemiEXTRACTNAMEConstants.CEMI_DATA_EXTRACT_NAME);
        
        // Not defined for this pattern example
        // One-to-many optional methods may be needed which sort, filter, or obtain dependent information
        // which then will be used by routine populateListOfInScopeBusinessObjects below.
        
        // Method required for every data extraction.
        // Intended processing of this method is to
        //      (1) obtain the keys for the business objects meeting required in scope data mapping criteria
        //      (2) place those identified keys into a table so the associated objects can be obtained by downstream processing
        cemiDATAEXTRACTNAMEExtractService.captureInScopeBusinessObjectKeysToProcessingTable(CemiEXTRACTNAMEConstants.CEMI_DATA_EXTRACT_NAME); 
        
        
        
        // Phase 2: Loop through result set to create all the csv files
        cemiDATAEXTRACTNAMEExtractService.generateIntermediateAwardScheduleExtractData(jobRunDate);
        
        
        
        // Phase 3: Create single multi-tabbed file.
        cemiDATAEXTRACTNAMEExtractService.generateAwardScheduleExtractFile(jobRunDate);
        return true;
    }

    public CemiDATAEXTRACTNAMEExtractService getCemiDATAEXTRACTNAMEExtractService() {
        return cemiDATAEXTRACTNAMEExtractService;
    }

    public void setCemiDATAEXTRACTNAMEExtractService(CemiDATAEXTRACTNAMEExtractService cemiDATAEXTRACTNAMEExtractService) {
        this.cemiDATAEXTRACTNAMEExtractService = cemiDATAEXTRACTNAMEExtractService;
    }
}
