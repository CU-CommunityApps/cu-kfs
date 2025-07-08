package edu.cornell.kfs.pmw.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;

public class PaymentWorksRefreshAuthorizationTokenStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(PaymentWorksRefreshAuthorizationTokenStep.class);

    private PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        try {
            paymentWorksWebServiceCallsService.refreshPaymentWorksAuthorizationToken();
            return true;
        } catch (RuntimeException e) {
            LOG.error("execute(): Could not refresh PaymentWorks token", e);
            return false;
        }
    }

    public void setPaymentWorksWebServiceCallsService(PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService) {
        this.paymentWorksWebServiceCallsService = paymentWorksWebServiceCallsService;
    }

}
