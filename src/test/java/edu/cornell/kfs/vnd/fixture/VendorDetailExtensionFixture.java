package edu.cornell.kfs.vnd.fixture;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.businessobject.options.EinvoiceIndicatorValuesFinder;

public enum VendorDetailExtensionFixture {
	
	EXTENSION(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.SFTP.code);
	
	public final String einvoice;

	private VendorDetailExtensionFixture(String einvoice) {
		this.einvoice = einvoice;
	}
	
	public VendorDetailExtension createVendorDetailExtension() {
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
		vendorDetailExtension.setEinvoiceVendorIndicator(einvoice);
		return vendorDetailExtension;
	}
}
