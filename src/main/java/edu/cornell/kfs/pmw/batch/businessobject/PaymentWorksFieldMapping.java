package edu.cornell.kfs.pmw.batch.businessobject;

import java.io.Serializable;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksFieldMapping extends PersistableBusinessObjectBase implements Serializable {
    
    private static final long serialVersionUID = 5364858190367831904L;
    private long paymentWorksFieldMappingId;
    private String paymentWorksFieldId;
    private String paymentWorksFieldLabel;
    private String kfsPaymentWorksStagingTableColumn;

    public long getPaymentWorksFieldMappingId() {
        return paymentWorksFieldMappingId;
    }

    public void setPaymentWorksFieldMappingId(long paymentWorksFieldMappingId) {
        this.paymentWorksFieldMappingId = paymentWorksFieldMappingId;
    }

    public String getPaymentWorksFieldId() {
        return paymentWorksFieldId;
    }

    public void setPaymentWorksFieldId(String paymentWorksFieldId) {
        this.paymentWorksFieldId = paymentWorksFieldId;
    }

    public String getPaymentWorksFieldLabel() {
        return paymentWorksFieldLabel;
    }

    public void setPaymentWorksFieldLabel(String paymentWorksFieldLabel) {
        this.paymentWorksFieldLabel = paymentWorksFieldLabel;
    }

    public String getKfsPaymentWorksStagingTableColumn() {
        return kfsPaymentWorksStagingTableColumn;
    }

    public void setKfsPaymentWorksStagingTableColumn(String kfsPaymentWorksStagingTableColumn) {
        this.kfsPaymentWorksStagingTableColumn = kfsPaymentWorksStagingTableColumn;
    }

}
