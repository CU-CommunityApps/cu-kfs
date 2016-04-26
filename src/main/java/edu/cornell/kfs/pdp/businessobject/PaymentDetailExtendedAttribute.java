package edu.cornell.kfs.pdp.businessobject;

import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

public class PaymentDetailExtendedAttribute extends PersistableBusinessObjectExtensionBase {
    
    private KualiInteger id;
    private Boolean crCancelledPayment;
    
    public PaymentDetailExtendedAttribute() {
        super();
    }
    
    public KualiInteger getId() {
        return id;
    }
    public void setId(KualiInteger id) {
        this.id = id;
    }

    public Boolean getCrCancelledPayment() {
        return crCancelledPayment;
    }

    public void setCrCancelledPayment(Boolean crCancelledPayment) {
        this.crCancelledPayment = crCancelledPayment;
    }

}
