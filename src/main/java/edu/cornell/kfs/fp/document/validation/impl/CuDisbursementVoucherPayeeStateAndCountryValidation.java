package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Custom validation class that checks the DV payee state and country codes.
 * They should not both be blank, since it would run the risk of creating
 * undeliverable checks, especially for foreign addresses.
 */
public class CuDisbursementVoucherPayeeStateAndCountryValidation extends GenericValidation {
    private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherPayeeStateAndCountryValidation.class);

    private AccountingDocument accountingDocumentForValidation;
    protected transient RecurringDisbursementVoucherForeignVendorValidation recurringDisbursementVoucherForeignVendorValidation;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate, entering");
        boolean isValid = true;
        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) accountingDocumentForValidation;
        DisbursementVoucherPayeeDetail payeeDetail = dvDocument.getDvPayeeDetail();
        
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        // Verify that state code and country code are not both blank.
        if (StringUtils.isBlank(payeeDetail.getDisbVchrPayeeStateCode()) && StringUtils.isBlank(payeeDetail.getDisbVchrPayeeCountryCode())) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(
                    KFSConstants.GENERAL_PAYMENT_TAB_ERRORS, CUKFSKeyConstants.ERROR_DOCUMENT_DV_BLANK_STATE_AND_COUNTRY);
            isValid = false;
        }
        
        if (dvDocument instanceof RecurringDisbursementVoucherDocument) {
            LOG.debug("validate, found a recurring DV");
            RecurringDisbursementVoucherDocument rcdvDocument = (RecurringDisbursementVoucherDocument) dvDocument;
            isValid &= recurringDisbursementVoucherForeignVendorValidation.validateRecurringDVForeignVendor(rcdvDocument);
        }
        
        GlobalVariables.getMessageMap().removeFromErrorPath(KFSPropertyConstants.DOCUMENT);
        
        return isValid;
    }



    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public void setRecurringDisbursementVoucherForeignVendorValidation(
            RecurringDisbursementVoucherForeignVendorValidation recurringDisbursementVoucherForeignVendorValidation) {
        this.recurringDisbursementVoucherForeignVendorValidation = recurringDisbursementVoucherForeignVendorValidation;
    }

}
