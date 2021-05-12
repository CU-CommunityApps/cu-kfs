package edu.cornell.kfs.pdp.businessobject;

import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class PaymentDetailExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
    
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
