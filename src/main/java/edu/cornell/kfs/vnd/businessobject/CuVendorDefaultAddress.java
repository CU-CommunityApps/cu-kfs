package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorDefaultAddress;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorDefaultAddress extends VendorDefaultAddress {

    public boolean isEqualForRouting(Object toCompare) {
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorDefaultAddress)) {

            return false;
        }
        else {
            VendorDefaultAddress vda = (VendorDefaultAddress) toCompare;

            return new EqualsBuilder().append(this.getVendorDefaultAddressGeneratedIdentifier(), vda.getVendorDefaultAddressGeneratedIdentifier()).append(this.getVendorAddressGeneratedIdentifier(), vda.getVendorAddressGeneratedIdentifier()).append(this.getVendorCampusCode(), vda.getVendorCampusCode()).
                    // KFSPTS-2055
                    append(this.isActive(), vda.isActive()).isEquals();
        }
    }

}
