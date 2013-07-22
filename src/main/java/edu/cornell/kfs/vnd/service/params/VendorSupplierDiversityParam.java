package edu.cornell.kfs.vnd.service.params;

import java.io.Serializable;
import java.util.Date;

public class VendorSupplierDiversityParam implements Serializable {
  
	private String vendorSupplierDiversityCode;
    private boolean active;
    private Date vendorSupplierDiversityExpirationDate;
	public String getVendorSupplierDiversityCode() {
		return vendorSupplierDiversityCode;
	}
	public void setVendorSupplierDiversityCode(String vendorSupplierDiversityCode) {
		this.vendorSupplierDiversityCode = vendorSupplierDiversityCode;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Date getVendorSupplierDiversityExpirationDate() {
		return vendorSupplierDiversityExpirationDate;
	}
	
	public void setVendorSupplierDiversityExpirationDate(
			Date vendorSupplierDiversityExpirationDate) {
		this.vendorSupplierDiversityExpirationDate = vendorSupplierDiversityExpirationDate;
	}

}
