package  edu.cornell.kfs.cemi.pdp.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionExtractService;

public class CreateCemiPaymentElectionExtractStep extends AbstractStep {

    private CemiPaymentElectionExtractService cemiPaymentElectionExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiPaymentElectionExtractService.resetState();
        cemiPaymentElectionExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        //Phase 2: Loop through result set to create data rows that are saved to database table
        cemiPaymentElectionExtractService.generateIntermediateExtractData(jobRunDate);
        //Phase 3: Create single multi-tabbed file.
        cemiPaymentElectionExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public CemiPaymentElectionExtractService getCemiPaymentElectionExtractService() {
        return cemiPaymentElectionExtractService;
    }

    public void setCemiPaymentElectionExtractService(CemiPaymentElectionExtractService cemiPaymentElectionExtractService) {
        this.cemiPaymentElectionExtractService = cemiPaymentElectionExtractService;
    }

}
