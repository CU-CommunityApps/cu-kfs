package edu.cornell.kfs.vnd.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import edu.cornell.kfs.vnd.batch.service.VendorInactivateConvertBatchService;



/*
 * batch step for VendorBatchjob
 */
public class VendorInactivateConvertBatchStep  extends AbstractStep {

    private VendorInactivateConvertBatchService vendorInactivateConvertBatchService;
    private BatchInputFileType batchInputFileType;
    
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        return vendorInactivateConvertBatchService.processVendorUpdates();
    }


    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }	


	public VendorInactivateConvertBatchService getVendorInactivateConvertBatchService() {
		return vendorInactivateConvertBatchService;
	}


	public void setVendorInactivateConvertBatchService(
			VendorInactivateConvertBatchService vendorInactivateConvertBatchService) {
		this.vendorInactivateConvertBatchService = vendorInactivateConvertBatchService;
	}


}
