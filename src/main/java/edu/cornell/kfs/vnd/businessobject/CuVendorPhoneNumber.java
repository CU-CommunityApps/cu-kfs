package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorPhoneNumber extends VendorPhoneNumber implements VendorRoutingComparable {


    public boolean isEqualForRouting(Object toCompare) {
        // KFSPTS-2055
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorPhoneNumber)) {

            return false;
        }
        else {
            VendorPhoneNumber vndPhNumber = (VendorPhoneNumber) toCompare;

            return new EqualsBuilder().append(
                    this.getVendorPhoneExtensionNumber(), vndPhNumber.getVendorPhoneExtensionNumber()).append(
                    this.getVendorPhoneGeneratedIdentifier(), vndPhNumber.getVendorPhoneGeneratedIdentifier()).append(
                    this.getVendorPhoneNumber(), vndPhNumber.getVendorPhoneNumber()).append(
                    this.getVendorPhoneTypeCode(), vndPhNumber.getVendorPhoneTypeCode()).append(
                    this.isActive(), vndPhNumber.isActive()).isEquals();
        }
    }

}
