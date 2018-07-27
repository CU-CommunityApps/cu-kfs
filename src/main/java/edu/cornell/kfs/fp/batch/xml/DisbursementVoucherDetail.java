package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DisbursementVoucherDetail", namespace = StringUtils.EMPTY)
public class DisbursementVoucherDetail {
    @XmlElement(name = "payment_information", namespace = StringUtils.EMPTY, required = false)
    protected PaymentInformation paymentInformation;
    
    public PaymentInformation getPaymentInformation() {
        return paymentInformation;
    }

    public void setPaymentInformation(PaymentInformation paymentInformation) {
        this.paymentInformation = paymentInformation;
    }

}
