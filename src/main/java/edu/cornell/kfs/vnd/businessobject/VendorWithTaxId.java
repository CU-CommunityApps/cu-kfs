package edu.cornell.kfs.vnd.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class VendorWithTaxId extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String vendorId;
    private String vendorTaxNumber;

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(final String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

}
