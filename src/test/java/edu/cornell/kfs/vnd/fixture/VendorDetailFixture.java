package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.krad.service.BusinessObjectService;

public enum VendorDetailFixture {

	ANAK_INC("Anak Inc", true, 4506, 0),
	ADD_ASSOCIATES_INC("ADD Associates Inc", true, 4435, 0),
	NO_SUCH_VENDOR("NO SUCH VENDOR", false, -1, -1),
	VENDOR_TO_CREATE("Test Vendor", true, 1234567, 0, "", false, "999887676","", "", true, false, "P" ),
	UPDATE_ADD_ASSOCIATES_INC("ADD Associates Inc", true, 4435, 0, "", false, "999887676","", "", true, false, "P" );
	
	
	public final String vendorName;
	public final boolean vendorParentIndicator;
	public final boolean isForeign;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorDetailAssignedIdentifier;
	public final String vendorTypeCode;
	public final String taxNumber;
	public final String taxNumberType;
	public final String ownershipTypeCode;
	public final boolean isTaxable;
	public final boolean isEInvoice;
	public final String defaultPaymentMethodCode;
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier) 
	{
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
		this.vendorTypeCode = "";
		this.isForeign = false;
		this.taxNumber = "987654321";
		this.taxNumberType = "";
		this.ownershipTypeCode = "";
		this.isTaxable = true;
		this.isEInvoice = true;
		this.defaultPaymentMethodCode = "";
	}
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String vendorTypeCode, boolean isForeign, String taxNumber,
			String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice, String defaultPaymentMethodCode) {
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
		this.vendorTypeCode = vendorTypeCode;
		this.isForeign = isForeign;
		this.taxNumber = taxNumber;
		this.taxNumberType = taxNumberType;
		this.ownershipTypeCode = ownershipTypeCode;
		this.isTaxable = isTaxable;
		this.isEInvoice = isEInvoice;
		this.defaultPaymentMethodCode = defaultPaymentMethodCode;
	}
	
	public VendorDetail createVendorDetail() {
		VendorDetail vendorDetail = new VendorDetail();
		vendorDetail.setVendorName(this.vendorName);
		vendorDetail.setVendorParentIndicator(this.vendorParentIndicator);
		vendorDetail.setVendorHeaderGeneratedIdentifier(this.vendorHeaderGeneratedIdentifier);
		vendorDetail.setVendorDetailAssignedIdentifier(this.vendorDetailAssignedIdentifier);
		vendorDetail.setVendorHeader(VendorHeaderFixture.ONE.createVendorHeader());
		return vendorDetail;
	}

	public VendorDetail createVendorDetail(BusinessObjectService businessObjectService) {
		return (VendorDetail) businessObjectService.retrieve(this.createVendorDetail());
	}
	
	
}
