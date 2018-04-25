package edu.cornell.kfs.pmw.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;

public class PaymentWorksRefreshAuthorizationTokenStep extends AbstractStep {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksRefreshAuthorizationTokenStep.class);

    private PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
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
