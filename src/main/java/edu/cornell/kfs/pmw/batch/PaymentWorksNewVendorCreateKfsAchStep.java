package edu.cornell.kfs.pmw.batch;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchService;


public class PaymentWorksNewVendorCreateKfsAchStep extends AbstractStep {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorCreateKfsAchStep.class);
    
    protected PaymentWorksNewVendorPayeeAchService paymentWorksNewVendorPayeeAchService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        if (getPaymentWorksBatchUtilityService().isPaymentWorksIntegrationProcessingEnabled()) {
            getPaymentWorksNewVendorPayeeAchService().processKfsPayeeAchAccountsForApprovedAndDisapprovedPmwNewVendors();
        } else {
            LOG.info("execute: KFS System Parameter '" + PaymentWorksParameterConstants.PMW_INTEGRATION_IS_ACTIVE_IND
                     + "' is NOT active. The value of this KFS System parameter must be changed to turn on the batch jobs integration to PaymentWorks.");
        }
        return true;
    }

    public PaymentWorksNewVendorPayeeAchService getPaymentWorksNewVendorPayeeAchService() {
        return paymentWorksNewVendorPayeeAchService;
    }

    public void setPaymentWorksNewVendorPayeeAchService(PaymentWorksNewVendorPayeeAchService paymentWorksNewVendorPayeeAchService) {
        this.paymentWorksNewVendorPayeeAchService = paymentWorksNewVendorPayeeAchService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

}
