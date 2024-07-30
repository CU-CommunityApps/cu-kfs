package edu.cornell.kfs.pmw.businessobject;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public class PaymentWorksVendorGlobalDetail extends GlobalBusinessObjectDetailBase {

    private Integer id;
    private PaymentWorksVendor paymentWorksVendor;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PaymentWorksVendor getPaymentWorksVendor() {
        return paymentWorksVendor;
    }

    public void setPaymentWorksVendor(PaymentWorksVendor paymentWorksVendor) {
        this.paymentWorksVendor = paymentWorksVendor;
    }

}
