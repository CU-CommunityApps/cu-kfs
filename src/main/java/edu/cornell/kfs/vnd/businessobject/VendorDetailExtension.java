package edu.cornell.kfs.vnd.businessobject;


import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class VendorDetailExtension extends PersistableBusinessObjectExtensionBase {
	private static final long serialVersionUID = 2L;

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private boolean einvoiceVendorIndicator;

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

    public boolean isEinvoiceVendorIndicator() {
        return einvoiceVendorIndicator;
    }

    public void setEinvoiceVendorIndicator(boolean eInvoiceVendorIndicator) {
        this.einvoiceVendorIndicator = eInvoiceVendorIndicator;
    }

}