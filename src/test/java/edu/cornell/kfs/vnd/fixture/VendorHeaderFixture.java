package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorType;

public enum VendorHeaderFixture {
	
	ONE("PO", Boolean.TRUE);
	
	public final String vendorTypeCode;
	public final Boolean vendorForeignIndicator;
	
	private VendorHeaderFixture(String vendorTypeCode, Boolean vendorForeignIndicator) {
		this.vendorTypeCode = vendorTypeCode;
		this.vendorForeignIndicator = vendorForeignIndicator;
	}
	
	public VendorHeader createVendorHeader() {
		VendorHeader vendorHeader = new VendorHeader();
		vendorHeader.setVendorOwnershipCode(VendorOwnershipTypeFixture.ONE.vendorOwnershipCode);
//		vendorHeader.setVendorOwnership(VendorOwnershipTypeFixture.ONE.createVendorOwnershipType());
		vendorHeader.setVendorForeignIndicator(this.vendorForeignIndicator);
		vendorHeader.setVendorTypeCode(vendorTypeCode);
//		vendorHeader.setVendorType(this.createVendorType());
		return vendorHeader;
	}

	public VendorType createVendorType() {
		VendorType vendorType = new VendorType();
		vendorType.setVendorAddressTypeRequiredCode("PURCHASE ORDER");
		return vendorType;
	}
}
