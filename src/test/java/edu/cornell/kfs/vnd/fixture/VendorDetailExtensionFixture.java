package edu.cornell.kfs.vnd.fixture;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.businessobject.options.EinvoiceIndicatorValuesFinder;

public enum VendorDetailExtensionFixture {
	
	EXTENSION(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.SFTP.code, "P");
	
	public final String einvoice;
	public final String defaultB2BPaymentMethodCode;

	private VendorDetailExtensionFixture(String einvoice, String defaultB2BPaymentMethodCode) {
		this.einvoice = einvoice;
		this.defaultB2BPaymentMethodCode = defaultB2BPaymentMethodCode;
	}
	
	public VendorDetailExtension createVendorDetailExtension() {
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
		vendorDetailExtension.setEinvoiceVendorIndicator(einvoice);
		vendorDetailExtension.setDefaultB2BPaymentMethodCode(defaultB2BPaymentMethodCode);
		return vendorDetailExtension;
	}
}
