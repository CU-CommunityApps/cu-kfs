package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorType;

public enum VendorHeaderFixture {
	
	ONE("PO", Boolean.TRUE);
	
	public final String vendorTypeCode;
	public final Boolean vendorForeignIndicator;
	public final String vendorTaxNumber;
	
	private VendorHeaderFixture(String vendorTypeCode, Boolean vendorForeignIndicator, String vendorTaxNumber) {
		this.vendorTypeCode = vendorTypeCode;
		this.vendorForeignIndicator = vendorForeignIndicator;
		this.vendorTaxNumber = vendorTaxNumber;
	}
	
	public VendorHeader createVendorHeader() {
		VendorHeader vendorHeader = new VendorHeader();
		vendorHeader.setVendorOwnershipCode(VendorOwnershipTypeFixture.ONE.vendorOwnershipCode);
		vendorHeader.setVendorForeignIndicator(this.vendorForeignIndicator);
		vendorHeader.setVendorTypeCode(vendorTypeCode);
		vendorHeader.setVendorTaxNumber(vendorTaxNumber);
		return vendorHeader;
	}

	public VendorType createVendorType() {
		VendorType vendorType = new VendorType();
		vendorType.setVendorAddressTypeRequiredCode("PURCHASE ORDER");
		return vendorType;
	}
}
