package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherDocumentPreRules;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.businessobject.PaymentSourceWireTransferExtendedAttribute;

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

    @Override
    protected boolean hasWireTransferValues(final PaymentSourceWireTransfer wireTransfer) {
        boolean hasValues = super.hasWireTransferValues(wireTransfer);
        final PaymentSourceWireTransferExtendedAttribute wireExtension =
                (PaymentSourceWireTransferExtendedAttribute) wireTransfer.getExtension();
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankStreetAddress());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankProvince());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankSWIFTCode());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankIBAN());
        hasValues |= StringUtils.isNotBlank(wireExtension.getSortOrTransitCode());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankName());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankAddress());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankRoutingNumber());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankAccountNumber());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankSwiftCode());
        return hasValues;
    }

    @Override
    protected void clearWireTransferValues(final PaymentSourceWireTransfer wireTransfer) {
        super.clearWireTransferValues(wireTransfer);
        final PaymentSourceWireTransferExtendedAttribute wireExtension =
                (PaymentSourceWireTransferExtendedAttribute) wireTransfer.getExtension();
        wireExtension.setBankStreetAddress(null);
        wireExtension.setBankProvince(null);
        wireExtension.setBankSWIFTCode(null);
        wireExtension.setBankIBAN(null);
        wireExtension.setSortOrTransitCode(null);
        wireExtension.setCorrespondentBankName(null);
        wireExtension.setCorrespondentBankAddress(null);
        wireExtension.setCorrespondentBankRoutingNumber(null);
        wireExtension.setCorrespondentBankAccountNumber(null);
        wireExtension.setCorrespondentBankSwiftCode(null);
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
