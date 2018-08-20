package edu.cornell.kfs.pmw.batch;

import java.util.Date;

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
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        if (paymentWorksBatchUtilityService.isPaymentWorksIntegrationProcessingEnabled()) {
            paymentWorksUploadSuppliersService.uploadPreparedVendorsToPaymentWorks();
        } else {
            LOG.info("execute: KFS System Parameter '" + PaymentWorksParameterConstants.PMW_INTEGRATION_IS_ACTIVE_IND
                    + "' is NOT active. The value of this KFS System parameter must be changed to turn on the batch jobs integration to PaymentWorks.");
        }
        return true;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksUploadSuppliersService(PaymentWorksUploadSuppliersService paymentWorksUploadSuppliersService) {
        this.paymentWorksUploadSuppliersService = paymentWorksUploadSuppliersService;
    }

}
