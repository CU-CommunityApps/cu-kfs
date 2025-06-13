package edu.cornell.kfs.module.purap.batch;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.service.ElectronicInvoiceHelperService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.service.CuElectronicInvoiceHelperService;

public class CuElectronicInvoiceRouteStep extends AbstractStep {
    
	private static final Logger LOG = LogManager.getLogger(CuElectronicInvoiceRouteStep.class);

    private ElectronicInvoiceHelperService electronicInvoiceHelperService;

    public boolean execute(String jobName, LocalDateTime jobRunDate) {
    	try {
    		Thread.sleep(60000);
    	} catch (Exception e) {}
        return ((CuElectronicInvoiceHelperService)electronicInvoiceHelperService).routeDocuments();
       
    }

    public void setElectronicInvoiceHelperService(ElectronicInvoiceHelperService electronicInvoiceHelperService) {
        this.electronicInvoiceHelperService = electronicInvoiceHelperService;
    }

}
