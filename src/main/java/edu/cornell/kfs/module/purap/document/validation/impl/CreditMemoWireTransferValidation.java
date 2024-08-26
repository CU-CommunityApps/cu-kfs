package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;
import edu.cornell.kfs.module.purap.businessobject.CreditMemoWireTransfer;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;

public class CreditMemoWireTransferValidation extends GenericValidation  {
	private static final Logger LOG = LogManager.getLogger();

    private AccountingDocument accountingDocumentForValidation;

    /**
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
    	if (LOG.isDebugEnabled()) {
            LOG.debug("validate start");
    	}
        boolean isValid = true;
        
        VendorCreditMemoDocument document = (VendorCreditMemoDocument) accountingDocumentForValidation;
        CreditMemoWireTransfer wireTransfer = ((CuVendorCreditMemoDocument)document).getCmWireTransfer();

        if (!KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(((CuVendorCreditMemoDocument)document).getPaymentMethodCode())) {
            return isValid;
        }

        MessageMap errors = GlobalVariables.getMessageMap(); 
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);
        errors.addToErrorPath(CUPurapPropertyConstants.CM_WIRE_TRANSFER);


        isValid &= isValid(wireTransfer.getCmBankName(), CUPurapConstants.LABEL_BANK_NAME, CUPurapPropertyConstants.CM_BANK_NAME);
        isValid &= isValid(wireTransfer.getCmBankCityName(), CUPurapConstants.LABEL_BANK_CITY, CUPurapPropertyConstants.CM_BANK_CITY_NAME);
        isValid &= isValid(wireTransfer.getCmBankCountryCode(), CUPurapConstants.LABEL_BANK_COUNTRY, CUPurapPropertyConstants.CM_BANK_COUNTRY_CODE);
        isValid &= isValid(wireTransfer.getCmCurrencyTypeName(), CUPurapConstants.LABEL_CURRENCY, CUPurapPropertyConstants.CM_CURRENCY_TYPE_NAME);
        isValid &= isValid(wireTransfer.getCmPayeeAccountNumber(), CUPurapConstants.LABEL_BANK_ACCT_NUMBER, CUPurapPropertyConstants.CM_PAYEE_ACCT_NUMBER);
        isValid &= isValid(wireTransfer.getCmPayeeAccountName(), CUPurapConstants.LABEL_BANK_ACCT_NAME, CUPurapPropertyConstants.CM_PAYEE_ACCT_NAME);

        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(wireTransfer.getCmBankCountryCode()) && StringUtils.isBlank(wireTransfer.getCmBankRoutingNumber())) {
            errors.putError(CUPurapPropertyConstants.CM_BANK_ROUTING_NUMBER, KFSKeyConstants.ERROR_PAYMENT_SOURCE_BANK_ROUTING_NUMBER);
            isValid = false;
        }

        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(wireTransfer.getCmBankCountryCode()) && StringUtils.isBlank(wireTransfer.getCmBankStateCode())) {
            errors.putError(CUPurapPropertyConstants.CM_BANK_STATE_CODE, KFSKeyConstants.ERROR_REQUIRED, "Bank State");
            isValid = false;
        }


        errors.removeFromErrorPath(CUPurapPropertyConstants.CM_WIRE_TRANSFER);
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT);
        
        return isValid;
    }

    private boolean isValid(String field, String label, String errorPropertyName) {

        // make sure it exists
        if (StringUtils.isBlank(field)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_REQUIRED, label);
            return false;
        }
        return true;
    }
    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

}
