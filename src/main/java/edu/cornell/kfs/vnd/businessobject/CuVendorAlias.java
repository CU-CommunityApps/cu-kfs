package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorAlias extends VendorAlias implements VendorRoutingComparable {


    public boolean isEqualForRouting(Object toCompare) {
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorAlias)) {

            return false;
        }
        else {
            VendorAlias vndAlias = (VendorAlias) toCompare;

            return new EqualsBuilder().append(
                    this.getVendorAliasName(), vndAlias.getVendorAliasName()).append(
                    this.isActive(), vndAlias.isActive()).isEquals();
        }
    }

}
