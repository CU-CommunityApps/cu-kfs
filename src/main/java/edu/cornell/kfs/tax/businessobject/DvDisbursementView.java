package edu.cornell.kfs.tax.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.core.api.util.type.KualiInteger;

/**
 * Convenience lightweight BO to allow for ORM tools to map to the DvDisbursementView
 */
public class DvDisbursementView extends TransientBusinessObjectBase {

    private String custPaymentDocNbr;
    private KualiInteger disbursementNbr;
    private String paymentStatusCode;
    private String disbursementTypeCode;

    public String getCustPaymentDocNbr() {
        return custPaymentDocNbr;
    }

    public void setCustPaymentDocNbr(String custPaymentDocNbr) {
        this.custPaymentDocNbr = custPaymentDocNbr;
    }

    public KualiInteger getDisbursementNbr() {
        return disbursementNbr;
    }

    public void setDisbursementNbr(KualiInteger disbursementNbr) {
        this.disbursementNbr = disbursementNbr;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    public void setPaymentStatusCode(String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public String getDisbursementTypeCode() {
        return disbursementTypeCode;
    }

    public void setDisbursementTypeCode(String disbursementTypeCode) {
        this.disbursementTypeCode = disbursementTypeCode;
    }

}
