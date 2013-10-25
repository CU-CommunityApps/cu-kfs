package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.vnd.businessobject.VendorCustomerNumber;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorCustomerNumber extends VendorCustomerNumber implements VendorRoutingComparable  {


    public boolean isEqualForRouting(Object toCompare) {
        // KFSPTS-2055
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorCustomerNumber)) {

            return false;
        }
        else {
            VendorCustomerNumber vndCustNumber = (VendorCustomerNumber) toCompare;

            return new EqualsBuilder().append(
                    this.getVendorCustomerNumber(), vndCustNumber.getVendorCustomerNumber()).append(
                    this.getVendorCustomerNumberGeneratedIdentifier(), vndCustNumber.getVendorCustomerNumberGeneratedIdentifier()).append(
                    this.getVendorOrganizationCode(), vndCustNumber.getVendorOrganizationCode()).append(
                    this.getChartOfAccountsCode(), vndCustNumber.getChartOfAccountsCode()).append(
                    this.isActive(), vndCustNumber.isActive()).isEquals();
        }
    }
    

}
