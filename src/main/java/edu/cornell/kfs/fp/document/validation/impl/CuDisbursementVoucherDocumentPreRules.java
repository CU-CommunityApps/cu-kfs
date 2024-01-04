package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherDocumentPreRules;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Checks warnings and prompt conditions for dv document.
 */
public class CuDisbursementVoucherDocumentPreRules extends DisbursementVoucherDocumentPreRules {

    private CuCheckStubService cuCheckStubService;
    private ConfigurationService configurationService;

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

	/**
     * This method returns true if the state of all the tabs is valid, false otherwise.
     *
     * @param dvDocument submitted disbursement voucher document
     * @return Returns true if the state of all the tabs is valid, false otherwise.
     */
    @SuppressWarnings("deprecation")
    protected boolean checkWireTransferTabState(final DisbursementVoucherDocument dvDocument) {
        boolean tabStatesOK = true;

        final PaymentSourceWireTransfer dvWireTransfer = dvDocument.getWireTransfer();

        // if payment method is not WIRE and wire tab contains data, ask user to clear tab
        final String disbVchrPaymentMethodCode = dvDocument.getDisbVchrPaymentMethodCode();
        final boolean isWireTransfer = StringUtils.equals(
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE,
                disbVchrPaymentMethodCode);
        if (disbVchrPaymentMethodCode != null && !isWireTransfer && hasWireTransferValues(dvWireTransfer)) {
            String questionText = getConfigurationService().getPropertyValueAsString(
                    CUKFSKeyConstants.QUESTION_CLEAR_UNNEEDED_WIRE_TAB);
            
            final boolean clearTab = super.askOrAnalyzeYesNoQuestion(
                    KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID,
                    questionText);
            if (clearTab) {
                // NOTE: Can't replace with new instance because Foreign Draft uses same object
                clearWireTransferValues(dvWireTransfer);
            } else {
                // return to document if the user doesn't want to clear the Wire Transfer tab
                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
                tabStatesOK = false;
            }
        }

        return tabStatesOK;
    }

    public CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

    //Copied from subclass due to being private
    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }
    
}
