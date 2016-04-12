package edu.cornell.kfs.tax.service;

public interface PaymentReason1099BoxService {
	
	/**
	 * Determines if a given payment reason has any mapping set in the CUTaxConstants.PAYMENT_REASON_TO_TAX_BOX parameter
	 * @param paymentReasonCode
	 * @return
	 */
	boolean isPaymentReasonMappedTo1099Box(String paymentReasonCode);
	
	/**
	 * Returns the 1099 box mapped to the given payment reason code as prescribed in CUTaxConstants.PAYMENT_REASON_TO_TAX_BOX parameter
	 * @param paymentReasonCode
	 * @return
	 */
	String getPaymentReason1099Box(String paymentReasonCode);
	
	/**
	 * Determines if a given payment reason has been set to not map to any 1099 boxes in the CUTaxConstants.PAYMENT_REASON_TO_NO_TAX_BOX parameter
	 * @param paymentReasonCode
	 * @return
	 */
	boolean isPaymentReasonMappedToNo1099Box(String paymentReasonCode);

}
