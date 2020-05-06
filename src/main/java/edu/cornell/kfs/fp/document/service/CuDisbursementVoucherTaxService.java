package edu.cornell.kfs.fp.document.service;

public interface CuDisbursementVoucherTaxService {
	
	boolean isForeignVendorAndTaxReviewRequired(String payeeTypeCode,
			String paymentReasonCode, Integer vendorHeaderId);
	
	boolean isForeignVendorAndTaxReviewNotRequired(String payeeTypeCode,
			String paymentReasonCode, Integer vendorHeaderId);
	
	boolean isForeignVendor(String payeeTypeCode, Integer vendorHeaderId);

}
