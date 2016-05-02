package edu.cornell.kfs.vnd.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;


public class CuVendorHeaderExtension extends PersistableBusinessObjectExtensionBase {

    private static final long serialVersionUID = 1L;
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorLocale;

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public String getVendorLocale() {
        return vendorLocale;
    }

    public void setVendorLocale(String vendorLocale) {
        this.vendorLocale = vendorLocale;
    }

}