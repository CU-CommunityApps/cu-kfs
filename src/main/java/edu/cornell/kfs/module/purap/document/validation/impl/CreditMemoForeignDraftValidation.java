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

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;

public class CreditMemoForeignDraftValidation extends GenericValidation {
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
        if (!KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equals(document.getPaymentMethodCode())) {
            return true;
        }

        MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);
        errors.addToErrorPath(KFSPropertyConstants.PAYMENT_SOURCE_WIRE_TRANSFER);

        /* currency type code required */
        if (StringUtils.isBlank(((CuVendorCreditMemoDocument)document).getWireTransfer().getForeignCurrencyTypeCode())) {
            errors.putError(KFSPropertyConstants.FD_CURRENCY_TYPE_CODE, KFSKeyConstants.ERROR_PAYMENT_SOURCE_CURRENCY_TYPE_CODE);
            isValid = false;
        }

        /* currency type name required */
        if (StringUtils.isBlank(((CuVendorCreditMemoDocument)document).getWireTransfer().getForeignCurrencyTypeName())) {
            errors.putError(KFSPropertyConstants.FD_CURRENCY_TYPE_NAME, KFSKeyConstants.ERROR_PAYMENT_SOURCE_CURRENCY_TYPE_NAME);
            isValid = false;
        }

        errors.removeFromErrorPath(KFSPropertyConstants.PAYMENT_SOURCE_WIRE_TRANSFER);
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT);

        return isValid;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

}
