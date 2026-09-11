package  edu.cornell.kfs.cemi.module.purap.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderExtractService;

public class CreateCemiPurchaseOrderExtractStep extends AbstractStep {

    private CemiPurchaseOrderExtractService cemiPurchaseOrderExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        cemiPurchaseOrderExtractService.resetState();
        cemiPurchaseOrderExtractService.captureInScopeBusinessObjectKeysToProcessingTable(); 
        cemiPurchaseOrderExtractService.generateIntermediateExtractData(jobRunDate);
        cemiPurchaseOrderExtractService.generateDataConversionExtractFile(jobRunDate);
        return true;
    }

    public void setCemiPurchaseOrderExtractService(
            final CemiPurchaseOrderExtractService cemiPurchaseOrderExtractService) {
        this.cemiPurchaseOrderExtractService = cemiPurchaseOrderExtractService;
    }

}
