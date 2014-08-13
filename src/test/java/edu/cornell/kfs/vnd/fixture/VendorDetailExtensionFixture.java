package edu.cornell.kfs.vnd.fixture;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public enum VendorDetailExtensionFixture {
	
	EXTENSION(true, "P");
	
	public final boolean isEinvoice;
	public final String defaultB2BPaymentMethodCode;

	private VendorDetailExtensionFixture(boolean isEinvoice, String defaultB2BPaymentMethodCode) {
		this.isEinvoice = isEinvoice;
		this.defaultB2BPaymentMethodCode = defaultB2BPaymentMethodCode;
	}
	
	public VendorDetailExtension createVendorDetailExtension() {
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
		vendorDetailExtension.setEinvoiceVendorIndicator(isEinvoice);
		vendorDetailExtension.setDefaultB2BPaymentMethodCode(defaultB2BPaymentMethodCode);
		return vendorDetailExtension;
	}
}
