package  edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiSponsorExtractService;

public class CreateCemiSponsorExtractStep extends AbstractStep {
    
    private CemiSponsorExtractService cemiSponsorExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        
        /* Phase1: Obtain the dataset */

        // Method required for every extraction.
        // Logs the values of the system parameters affecting this run (outbound folder copy, sensitive data masking,
        // and any extract specific parameters such as date ranges). The default implementation is provided by
        // abstract base class CemiDataExtractServiceBase. Override getExtractSpecificParameterNamesToLog in the
        // service implementation to include any additional parameters used by the data extraction.
        cemiSponsorExtractService.logSystemParameterValues();

        // Method required for every extraction.
        // Intended to perform any action to ensure job runs successfully and produces the data extract file.
        //      At a minimum, on every execution of the batch job after the first one, this method should clear the
        //      table populated by routine captureInScopeBusinessObjectKeysToProcessingTable (called below) from the
        //      previous execution of the job.
        cemiSponsorExtractService.resetState();
        
        // Not defined for this pattern example
        // One-to-many optional methods may be needed which sort, filter, or obtain dependent 
        // information then used by routine populateListOfInScopeBusinessObjects below.
        
        // Method required for every data extraction.
        // Intended processing of this method is to
        //      (1) obtain the keys for the business objects meeting required in scope data mapping criteria
        //      (2) place those identified legacy business object keys into a table so the associated business objects
        //          can be obtained by downstream processing
        cemiSponsorExtractService.captureInScopeBusinessObjectKeysToProcessingTable(); 
        
        
        
        /* Phase 2: Loop through result set to create data rows that are saved to database table */
        
        // Method required for every data extraction.
        // The items from the in scope legacy data set are processed by a business object factory to 
        //      (1) Format each identified attribute into a string as defined by the mapping template specification
        //      (2) Save the generated data rows to database tables.
        cemiSponsorExtractService.generateIntermediateExtractData(jobRunDate);
        
        
        
        /* Phase 3: Create single multi-tabbed file */
        
        // Create the data conversion spreadsheet file from the contents of the database tables just populated.
        cemiSponsorExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiSponsorExtractService getCemiSponsorExtractService() {
        return cemiSponsorExtractService;
    }

    public void setCemiSponsorExtractService(CemiSponsorExtractService cemiSponsorExtractService) {
        this.cemiSponsorExtractService = cemiSponsorExtractService;
    }

}
