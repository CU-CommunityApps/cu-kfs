package edu.cornell.kfs.paymentworks.businessobject;

import java.io.Serializable;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksFieldMapping extends PersistableBusinessObjectBase implements Serializable {

    private static final long serialVersionUID = -2187008550220892821L;
    private long paymentWorksFieldMappingId;
    private String paymentWorksFieldLabel;
    private String kfsFieldName;

    public long getPaymentWorksFieldMappingId() {
        return paymentWorksFieldMappingId;
    }

    public void setPaymentWorksFieldMappingId(long paymentWorksFieldMappingId) {
        this.paymentWorksFieldMappingId = paymentWorksFieldMappingId;
    }

    public String getPaymentWorksFieldLabel() {
        return paymentWorksFieldLabel;
    }

    public void setPaymentWorksFieldLabel(String paymentWorksFieldLabel) {
        this.paymentWorksFieldLabel = paymentWorksFieldLabel;
    }

    public String getKfsFieldName() {
        return kfsFieldName;
    }

    public void setKfsFieldName(String kfsFieldName) {
        this.kfsFieldName = kfsFieldName;
    }

}
