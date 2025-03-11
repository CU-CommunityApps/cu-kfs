package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherDocumentPreRules;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.pdp.service.CuCheckStubService;

/**
 * Checks warnings and prompt conditions for dv document.
 */
public class CuDisbursementVoucherDocumentPreRules extends DisbursementVoucherDocumentPreRules {

    private CuCheckStubService cuCheckStubService;
    
    /**
     * Executes pre-rules for Disbursement Voucher Document
     *
     * @param document submitted document
     * @return true if pre-rules execute successfully
     * @see org.kuali.kfs.kns.rules.PromptBeforeValidationBase#doRules(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
    @Override
    public boolean doPrompts(final Document document) {
        boolean preRulesOK = super.doPrompts(document);
        
        preRulesOK &= getCuCheckStubService().performPreRulesValidationOfIso20022CheckStubLength(document, this);

        setIncomeClassNonReportableForForeignVendorWithNoTaxReviewRequired(document);

        return preRulesOK;
    }

	private void setIncomeClassNonReportableForForeignVendorWithNoTaxReviewRequired(final Document document) {
		final DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) document;
		final DisbursementVoucherPayeeDetail dvPayeeDetail = dvDoc.getDvPayeeDetail();

		final String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
		final String paymentReasonCode = dvPayeeDetail.getDisbVchrPaymentReasonCode();
		final Integer vendorHeaderId = dvPayeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger();

		if (getCuDisbursementVoucherTaxService().isForeignVendorAndTaxReviewNotRequired(payeeTypeCode,paymentReasonCode, vendorHeaderId)) {
			dvDoc.getDvNonresidentTax().setIncomeClassCode(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_NON_REPORTABLE);
		}
	}

	protected CuDisbursementVoucherTaxService getCuDisbursementVoucherTaxService(){
		return SpringContext.getBean(CuDisbursementVoucherTaxService.class);
	}

    public CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }
    
}
