package edu.cornell.kfs.pmw.batch;

import java.util.Date;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchService;


public class PaymentWorksNewVendorCreateKfsAchStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorCreateKfsAchStep.class);
    
    protected PaymentWorksNewVendorPayeeAchService paymentWorksNewVendorPayeeAchService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getPaymentWorksNewVendorPayeeAchService().processKfsPayeeAchAccountsForApprovedAndDisapprovedPmwNewVendors();
        return true;
    }

    public PaymentWorksNewVendorPayeeAchService getPaymentWorksNewVendorPayeeAchService() {
        return paymentWorksNewVendorPayeeAchService;
    }

    public void setPaymentWorksNewVendorPayeeAchService(PaymentWorksNewVendorPayeeAchService paymentWorksNewVendorPayeeAchService) {
        this.paymentWorksNewVendorPayeeAchService = paymentWorksNewVendorPayeeAchService;
    }

}
