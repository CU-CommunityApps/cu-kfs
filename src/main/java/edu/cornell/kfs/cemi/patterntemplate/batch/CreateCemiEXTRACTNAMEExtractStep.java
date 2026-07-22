package  edu.cornell.kfs.cemi.patterntemplate.batch;

// Name the batch job step class such that it is unique following the pattern CreateCemi{EXTRACTNAME}ExtractStep
// where EXTRACTNAME is used consistently to represent the SAME data extract everywhere that term is used in
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

import edu.cornell.kfs.cemi.patterntemplate.batch.service.CemiEXTRACTNAMEExtractService;

public class CreateCemiEXTRACTNAMEExtractStep extends AbstractStep {
    
    private CemiEXTRACTNAMEExtractService cemiEXTRACTNAMEExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        
        /* Phase1: Obtain the dataset */
        
        // Method required for every extraction. 
        // Intended to perform any action to ensure job runs successfully and produces the data extract file.
        //      At a minimum, on every execution of the batch job after the first one, this method should clear the
        //      table populated by routine captureInScopeBusinessObjectKeysToProcessingTable (called below) from the
        //      previous execution of the job.
        cemiEXTRACTNAMEExtractService.resetState();
        
        // Not defined for this pattern example
        // One-to-many optional methods may be needed which sort, filter, or obtain dependent 
        // information then used by routine populateListOfInScopeBusinessObjects below.
        
        // Method required for every data extraction.
        // Intended processing of this method is to
        //      (1) obtain the keys for the business objects meeting required in scope data mapping criteria
        //      (2) place those identified legacy business object keys into a table so the associated business objects
        //          can be obtained by downstream processing
        cemiEXTRACTNAMEExtractService.captureInScopeBusinessObjectKeysToProcessingTable(); 
        
        
        
        /* Phase 2: Loop through result set to create data rows that are saved to database table */
        
        // Method required for every data extraction.
        // The items from the in scope legacy data set are processed by a business object factory to 
        //      (1) Format each identified attribute into a string as defined by the mapping template specification
        //      (2) Save the generated data rows to database tables.
        cemiEXTRACTNAMEExtractService.generateIntermediateExtractData(jobRunDate);
        
        
        
        /* Phase 3: Create single multi-tabbed file */
        
        // Create the data conversion spreadsheet file from the contents of the database tables just populated.
        cemiEXTRACTNAMEExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiEXTRACTNAMEExtractService getCemiEXTRACTNAMEExtractService() {
        return cemiEXTRACTNAMEExtractService;
    }

    public void setCemiEXTRACTNAMEExtractService(CemiEXTRACTNAMEExtractService cemiEXTRACTNAMEExtractService) {
        this.cemiEXTRACTNAMEExtractService = cemiEXTRACTNAMEExtractService;
    }

}
