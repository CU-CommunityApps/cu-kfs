package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PODocumentsStrings;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.VendorConstants.VendorTypes;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;


public class CuPurchaseOrderProcessVendorValidation extends CuPurchasingProcessVendorValidation {
    private VendorService vendorService;
    private ParameterService parameterService;
    private PostalCodeValidationService postalCodeValidationService;
    private PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService;
    private FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService;

    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        boolean valid = super.validate(event);
        final PurchasingAccountsPayableDocument purapDocument = (PurchasingAccountsPayableDocument)event.getDocument();
        final PurchaseOrderDocument poDocument = (PurchaseOrderDocument) purapDocument;
        final MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(PurapConstants.VENDOR_ERRORS);

        // check to see if the vendor exists in the database, i.e. its ID is not null
        final Integer vendorHeaderID = poDocument.getVendorHeaderGeneratedIdentifier();
        if (ObjectUtils.isNull(vendorHeaderID)) {
            valid = false;
            errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_NONEXIST_VENDOR);
        }

        // vendor active validation...
        final VendorDetail vendorDetail = super.getVendorService().getVendorDetail(poDocument.getVendorHeaderGeneratedIdentifier(), poDocument.getVendorDetailAssignedIdentifier());
        if (ObjectUtils.isNull(vendorDetail)) {
            return valid;
        }

        // make sure that the vendor is active
        if (!vendorDetail.isActiveIndicator()) {
            valid &= false;
            errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_INACTIVE_VENDOR);
        }

        // validate vendor address
        super.getPostalCodeValidationService().validateAddress(poDocument.getVendorCountryCode(), poDocument.getVendorStateCode(), poDocument.getVendorPostalCode(), PurapPropertyConstants.VENDOR_STATE_CODE, PurapPropertyConstants.VENDOR_POSTAL_CODE);

        // Do checks for alternate payee vendor.
        final Integer alternateVendorHdrGeneratedId = poDocument.getAlternateVendorHeaderGeneratedIdentifier();
        final Integer alternateVendorHdrDetailAssignedId = poDocument.getAlternateVendorDetailAssignedIdentifier();

        final VendorDetail alternateVendor = super.getVendorService().getVendorDetail(alternateVendorHdrGeneratedId,alternateVendorHdrDetailAssignedId);

        if (alternateVendor != null) {
            if (alternateVendor.isVendorDebarred()) {
                errorMap.putError(PurapPropertyConstants.ALTERNATE_VENDOR_NAME,PurapKeyConstants.ERROR_PURCHASE_ORDER_ALTERNATE_VENDOR_DEBARRED);
                valid &= false;
            }
            if (StringUtils.equals(alternateVendor.getVendorHeader().getVendorTypeCode(), VendorTypes.DISBURSEMENT_VOUCHER)) {
                errorMap.putError(PurapPropertyConstants.ALTERNATE_VENDOR_NAME,PurapKeyConstants.ERROR_PURCHASE_ORDER_ALTERNATE_VENDOR_DV_TYPE);
                valid &= false;
            }
            if (!alternateVendor.isActiveIndicator()) {
                errorMap.putError(PurapPropertyConstants.ALTERNATE_VENDOR_NAME,PurapKeyConstants.ERROR_PURCHASE_ORDER_ALTERNATE_VENDOR_INACTIVE,PODocumentsStrings.ALTERNATE_PAYEE_VENDOR);
                valid &= false;
            }
        }


        //make sure that the vendor contract expiration date and not marked inactive.
        // KFSUPGRADE-266 remove kfsmi-8690/kfscntrb-929
//        if (StringUtils.isNotBlank(poDocument.getVendorContractName())) {
//            if (super.getVendorService().isVendorContractExpired(poDocument, poDocument.getVendorContractGeneratedIdentifier(), vendorDetail)) {
//                errorMap.putError(VendorPropertyConstants.VENDOR_CONTRACT_END_DATE, PurapKeyConstants.ERROR_EXPIRED_CONTRACT_END_DATE);
//                valid &= false;
//            }
//        }

        errorMap.clearErrorPath();
        return valid;
    }

	public VendorService getVendorService() {
		return vendorService;
	}

	public void setVendorService(final VendorService vendorService) {
		super.setVendorService(vendorService);
		this.vendorService = vendorService;
	}

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(final ParameterService parameterService) {
		super.setParameterService(parameterService);
		this.parameterService = parameterService;
	}

	public PostalCodeValidationService getPostalCodeValidationService() {
		return postalCodeValidationService;
	}

	public void setPostalCodeValidationService(
			final PostalCodeValidationService postalCodeValidationService) {
		super.setPostalCodeValidationService(postalCodeValidationService);
		this.postalCodeValidationService = postalCodeValidationService;
	}

	public PurchaseOrderTransmissionMethodDataRulesService getPurchaseOrderTransmissionMethodDataRulesService() {
		return purchaseOrderTransmissionMethodDataRulesService;
	}

	public void setPurchaseOrderTransmissionMethodDataRulesService(
			final PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService) {
		super.setPurchaseOrderTransmissionMethodDataRulesService(purchaseOrderTransmissionMethodDataRulesService);
		this.purchaseOrderTransmissionMethodDataRulesService = purchaseOrderTransmissionMethodDataRulesService;
	}

	public FinancialSystemWorkflowHelperService getFinancialSystemWorkflowHelperService() {
		return financialSystemWorkflowHelperService;
	}

	public void setFinancialSystemWorkflowHelperService(
			final FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService) {
		super.setFinancialSystemWorkflowHelperService(financialSystemWorkflowHelperService);
		this.financialSystemWorkflowHelperService = financialSystemWorkflowHelperService;
	}

}
