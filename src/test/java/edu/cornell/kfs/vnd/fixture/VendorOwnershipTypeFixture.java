package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.OwnershipType;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public enum VendorOwnershipTypeFixture {
	ONE(true, PaymentWorksConstants.VendorOwnershipTypeCodes.C_CORPORATION, "CORPORATION", true);
	
	public final boolean active;
	public final String vendorOwnershipCode;
	public final String vendorOwnershipDescription;
	public final boolean vendorOwnershipCategoryAllowedIndicator;
	
	private VendorOwnershipTypeFixture(boolean active, String vendorOwnershipCode, String vendorOwnershipDescription, 
			boolean vendorOwnershipCategoryAllowedIndicator) {
			this.active = active;
			this.vendorOwnershipCategoryAllowedIndicator = vendorOwnershipCategoryAllowedIndicator;
			this.vendorOwnershipCode = vendorOwnershipCode;
			this.vendorOwnershipDescription = vendorOwnershipDescription;
	}
	
	public OwnershipType createVendorOwnershipType() {
		OwnershipType ownershipType = new OwnershipType();
		ownershipType.setActive(active);
		ownershipType.setVendorOwnershipCode(vendorOwnershipCode);
		ownershipType.setVendorOwnershipDescription(vendorOwnershipDescription);
		ownershipType.setVendorOwnershipCategoryAllowedIndicator(vendorOwnershipCategoryAllowedIndicator);
		
		return ownershipType;
	}
}
