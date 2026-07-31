package  edu.cornell.kfs.cemi.vnd.batch;

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

import edu.cornell.kfs.cemi.vnd.batch.service.CemiEntityContactExtractService;

public class CreateCemiEntityContactExtractStep extends AbstractStep {
    
    private CemiEntityContactExtractService cemiEntityContactExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        cemiEntityContactExtractService.resetState();
        cemiEntityContactExtractService.captureInScopeBusinessObjectKeysToProcessingTable(); 
        cemiEntityContactExtractService.generateIntermediateExtractData(jobRunDate);
        cemiEntityContactExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public void setCemiEntityContactExtractService(CemiEntityContactExtractService cemiEntityContactExtractService) {
        this.cemiEntityContactExtractService = cemiEntityContactExtractService;
    }

}
