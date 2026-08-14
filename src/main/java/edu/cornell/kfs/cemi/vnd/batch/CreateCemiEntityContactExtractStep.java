package  edu.cornell.kfs.cemi.vnd.batch;

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
