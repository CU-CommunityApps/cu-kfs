package edu.cornell.kfs.vnd.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class VendorForEmployeeSearch extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private String vendorTaxTypeCode;

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

    public String getVendorTaxTypeCode() {
        return vendorTaxTypeCode;
    }

    public void setVendorTaxTypeCode(String vendorTaxTypeCode) {
        this.vendorTaxTypeCode = vendorTaxTypeCode;
    }

}
