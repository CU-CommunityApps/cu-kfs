package edu.cornell.kfs.vnd.businessobject;


/**
 *  non-persistable to hold vendor supplier diversity converted from input data file
 **/
public class VendorBatchSupplierDiversity {
	private String vendorSupplierDiversityCode;
    private String active;
    private String vendorSupplierDiversityExpirationDate;
    
    public VendorBatchSupplierDiversity(String[] supplierDiversity) {
    	vendorSupplierDiversityCode = supplierDiversity[0];
    	active = supplierDiversity[1];
    	vendorSupplierDiversityExpirationDate = supplierDiversity[2];
    }
    

	public String getVendorSupplierDiversityCode() {
		return vendorSupplierDiversityCode;
	}
	public void setVendorSupplierDiversityCode(String vendorSupplierDiversityCode) {
		this.vendorSupplierDiversityCode = vendorSupplierDiversityCode;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public String getVendorSupplierDiversityExpirationDate() {
		return vendorSupplierDiversityExpirationDate;
	}
	public void setVendorSupplierDiversityExpirationDate(
			String vendorSupplierDiversityExpirationDate) {
		this.vendorSupplierDiversityExpirationDate = vendorSupplierDiversityExpirationDate;
	}

}
