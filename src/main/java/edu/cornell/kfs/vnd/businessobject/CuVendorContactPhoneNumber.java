package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorContactPhoneNumber extends VendorContactPhoneNumber  implements VendorRoutingComparable  {

    public boolean isEqualForRouting(Object toCompare) {
        // KFSPTS-2055
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorContactPhoneNumber)) {

            return false;
        }
        else {
            VendorContactPhoneNumber vndPhNumber = (VendorContactPhoneNumber) toCompare;

            return new EqualsBuilder().append(
                    this.getVendorPhoneExtensionNumber(), vndPhNumber.getVendorPhoneExtensionNumber()).append(
                    this.getVendorContactPhoneGeneratedIdentifier(), vndPhNumber.getVendorContactPhoneGeneratedIdentifier()).append(
                    this.getVendorPhoneNumber(), vndPhNumber.getVendorPhoneNumber()).append(
                    this.getVendorPhoneTypeCode(), vndPhNumber.getVendorPhoneTypeCode()).append(
                    this.isActive(), vndPhNumber.isActive()).isEquals();
        }
    }


}
