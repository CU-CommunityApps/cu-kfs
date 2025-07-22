package edu.cornell.kfs.pmw.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksUploadSuppliersService;

public class PaymentWorksUploadSuppliersStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(PaymentWorksUploadSuppliersStep.class);

    private PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    private PaymentWorksUploadSuppliersService paymentWorksUploadSuppliersService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        try {
            if (paymentWorksBatchUtilityService.isPaymentWorksIntegrationProcessingEnabled()) {
                paymentWorksUploadSuppliersService.uploadPreparedVendorsToPaymentWorks();
            } else {
                LOG.info("execute: KFS System Parameter '" + PaymentWorksParameterConstants.PMW_INTEGRATION_IS_ACTIVE_IND
                        + "' is NOT active. The value of this KFS System parameter must be changed to turn on the batch jobs integration to PaymentWorks.");
            }
            return true;
        } catch (RuntimeException e) {
            LOG.error("execute, had an error processing payment works", e);
            return false;
        }
        
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksUploadSuppliersService(PaymentWorksUploadSuppliersService paymentWorksUploadSuppliersService) {
        this.paymentWorksUploadSuppliersService = paymentWorksUploadSuppliersService;
    }

}
