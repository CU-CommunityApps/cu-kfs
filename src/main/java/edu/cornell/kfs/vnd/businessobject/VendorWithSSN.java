package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

public class VendorWithSSN extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private String vendorTaxNumber;

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getVendorId() {
        return StringUtils.join(vendorHeaderGeneratedIdentifier, KFSConstants.DASH, vendorDetailAssignedIdentifier);
    }

}
