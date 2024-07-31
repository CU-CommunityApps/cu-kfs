package edu.cornell.kfs.pmw.businessobject;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public class PaymentWorksVendorGlobalDetail extends GlobalBusinessObjectDetailBase {

    private Integer id;
    private PaymentWorksVendor pmwVendor;

    private transient Boolean pmwVendorDeleted;

    public boolean paymentWorksVendorWasDeletedOrPurged() {
        if (pmwVendorDeleted == null) {
            refreshNonUpdateableReferences();
        }
        return pmwVendorDeleted.booleanValue();
    }

    @Override
    public void refreshNonUpdateableReferences() {
        final PaymentWorksVendor tempPmwVendor = pmwVendor;
        super.refreshNonUpdateableReferences();
        if (ObjectUtils.isNull(pmwVendor) && ObjectUtils.isNotNull(tempPmwVendor)
                && id != null && id.equals(tempPmwVendor.getId())) {
            pmwVendor = tempPmwVendor;
            pmwVendorDeleted = Boolean.TRUE;
        } else {
            pmwVendorDeleted = Boolean.FALSE;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
        pmwVendorDeleted = null;
    }

    public PaymentWorksVendor getPmwVendor() {
        return pmwVendor;
    }

    public void setPmwVendor(final PaymentWorksVendor pmwVendor) {
        this.pmwVendor = pmwVendor;
        pmwVendorDeleted = null;
    }

}
