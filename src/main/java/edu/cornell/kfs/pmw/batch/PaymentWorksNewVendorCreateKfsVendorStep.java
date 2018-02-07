package edu.cornell.kfs.pmw.batch;

import java.util.Date;
import org.kuali.kfs.sys.batch.AbstractStep;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsService;

public class PaymentWorksNewVendorCreateKfsVendorStep extends AbstractStep {
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorCreateKfsVendorStep.class);
    protected PaymentWorksNewVendorRequestsService paymentWorksNewVendorRequestsService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getPaymentWorksNewVendorRequestsService().createKfsVendorsFromPmwNewVendorRequests();
        return true;
    }

    public PaymentWorksNewVendorRequestsService getPaymentWorksNewVendorRequestsService() {
        return paymentWorksNewVendorRequestsService;
    }

    public void setPaymentWorksNewVendorRequestsService(PaymentWorksNewVendorRequestsService paymentWorksNewVendorRequestsService) {
        this.paymentWorksNewVendorRequestsService = paymentWorksNewVendorRequestsService;
    }
}
