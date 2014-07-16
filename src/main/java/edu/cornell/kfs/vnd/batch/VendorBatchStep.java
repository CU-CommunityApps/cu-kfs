package edu.cornell.kfs.vnd.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import edu.cornell.kfs.vnd.batch.service.VendorBatchService;

/*
 * batch step for VendorBatchjob
 */
public class VendorBatchStep  extends AbstractStep {

    private VendorBatchService vendorBatchService;
    private BatchInputFileType batchInputFileType;
    
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return vendorBatchService.processVendors();
    }


    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }


	public VendorBatchService getVendorBatchService() {
		return vendorBatchService;
	}


	public void setVendorBatchService(VendorBatchService vendorBatchService) {
		this.vendorBatchService = vendorBatchService;
	}


}
