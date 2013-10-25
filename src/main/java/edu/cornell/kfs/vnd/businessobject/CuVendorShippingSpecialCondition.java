package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorShippingSpecialCondition;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorShippingSpecialCondition extends VendorShippingSpecialCondition {
    public boolean isEqualForRouting(Object toCompare) {
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorShippingSpecialCondition)) {
            return false;
        }
        else {
            VendorShippingSpecialCondition vssc = (VendorShippingSpecialCondition) toCompare;
            // KFSPTS-2055 : add active check
            return new EqualsBuilder().append(this.getVendorHeaderGeneratedIdentifier(), vssc.getVendorHeaderGeneratedIdentifier()).append(this.getVendorDetailAssignedIdentifier(), vssc.getVendorDetailAssignedIdentifier()).append(this.getVendorShippingSpecialConditionCode(), vssc.getVendorShippingSpecialConditionCode()).
                    append(this.isActive(), vssc.isActive()).isEquals();
        }
    }

}
