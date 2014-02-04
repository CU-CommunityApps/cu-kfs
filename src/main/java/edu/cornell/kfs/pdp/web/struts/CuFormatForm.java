package edu.cornell.kfs.pdp.web.struts;

import org.kuali.kfs.pdp.web.struts.FormatForm;

public class CuFormatForm extends FormatForm{
    protected String paymentDistribution;

    public String getPaymentDistribution() {
        return paymentDistribution;
    }

    public void setPaymentDistribution(String paymentDistribution) {
        this.paymentDistribution = paymentDistribution;
    }
}
