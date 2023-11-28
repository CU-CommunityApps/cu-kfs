package edu.cornell.kfs.fp.document.service.impl;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherTaxServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;

public class CuDisbursementVoucherTaxServiceImpl extends DisbursementVoucherTaxServiceImpl implements CuDisbursementVoucherTaxService{
	
	protected VendorService vendorService;
	protected ParameterEvaluatorService parameterEvaluatorService;

    @Override
	public boolean isForeignVendorAndTaxReviewRequired(String payeeTypeCode,
			String paymentReasonCode, Integer vendorHeaderId) {

		return isForeignVendor(payeeTypeCode, vendorHeaderId) && !paymentReasonDoesNotRequireTaxReviewForForeignVendor(paymentReasonCode);
	}
	
    @Override
	public boolean isForeignVendorAndTaxReviewNotRequired(String payeeTypeCode,
			String paymentReasonCode, Integer vendorHeaderId) {

		return isForeignVendor(payeeTypeCode, vendorHeaderId) && paymentReasonDoesNotRequireTaxReviewForForeignVendor(paymentReasonCode);
	}
	
    @Override
    public boolean isForeignVendor(String payeeTypeCode, Integer vendorHeaderId) {
        return KFSConstants.PaymentPayeeTypes.VENDOR.equalsIgnoreCase(payeeTypeCode) && vendorService.isVendorForeign(vendorHeaderId);
    }
	
	protected boolean paymentReasonDoesNotRequireTaxReviewForForeignVendor(String paymentReasonCode){
		return parameterEvaluatorService.getParameterEvaluator(DisbursementVoucherDocument.class, CuDisbursementVoucherConstants.PAYMENT_REASONS_THAT_DO_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR, paymentReasonCode).evaluationSucceeds();
	}

	public VendorService getVendorService() {
		return vendorService;
	}

	public void setVendorService(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	public ParameterEvaluatorService getParameterEvaluatorService() {
		return parameterEvaluatorService;
	}

	public void setParameterEvaluatorService(
			ParameterEvaluatorService parameterEvaluatorService) {
		this.parameterEvaluatorService = parameterEvaluatorService;
	}
}
