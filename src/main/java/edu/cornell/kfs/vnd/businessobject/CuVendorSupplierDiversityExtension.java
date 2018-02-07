package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

public class CuVendorSupplierDiversityExtension  extends PersistableBusinessObjectExtensionBase {

    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorSupplierDiversityCode;

    // CU enhancement
    private Date vendorSupplierDiversityExpirationDate;
    
    public CuVendorSupplierDiversityExtension() {
    }
    
    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(
            Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public String getVendorSupplierDiversityCode() {
        return vendorSupplierDiversityCode;
    }

    public void setVendorSupplierDiversityCode(String vendorSupplierDiversityCode) {
        this.vendorSupplierDiversityCode = vendorSupplierDiversityCode;
    }

    public Date getVendorSupplierDiversityExpirationDate() {
        return vendorSupplierDiversityExpirationDate;
    }

    public void setVendorSupplierDiversityExpirationDate(
            Date vendorSupplierDiversityExpirationDate) {
        this.vendorSupplierDiversityExpirationDate = vendorSupplierDiversityExpirationDate;
    }


}
