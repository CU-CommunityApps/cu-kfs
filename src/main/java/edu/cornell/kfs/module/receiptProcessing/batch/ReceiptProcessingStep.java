/**
 * @author cab379
 */
package edu.cornell.kfs.module.receiptProcessing.batch;



import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import edu.cornell.kfs.module.receiptProcessing.service.ReceiptProcessingService;

public class ReceiptProcessingStep extends AbstractStep {

    private ReceiptProcessingService receiptProcessingService;
    private BatchInputFileType batchInputFileType;
    
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        return receiptProcessingService.loadFiles();
    }

    public ReceiptProcessingService getReceiptProcessingService() {
        return receiptProcessingService;
    }

    public void setReceiptProcessingService(ReceiptProcessingService receiptProcessingService) {
        this.receiptProcessingService =   receiptProcessingService;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

}
