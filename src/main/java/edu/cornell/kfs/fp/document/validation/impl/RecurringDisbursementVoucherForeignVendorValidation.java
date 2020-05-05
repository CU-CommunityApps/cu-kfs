package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class RecurringDisbursementVoucherForeignVendorValidation extends GenericValidation {
    private static final Logger LOG = LogManager.getLogger(RecurringDisbursementVoucherForeignVendorValidation.class);
    
    private AccountingDocument accountingDocumentForValidation;
    protected transient CuDisbursementVoucherTaxService cuDisbursementVoucherTaxService;
    
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        LOG.info("validate, entering");
        RecurringDisbursementVoucherDocument rcdvDocument = (RecurringDisbursementVoucherDocument) accountingDocumentForValidation;
        return validateRecurringDVForeignVendor(rcdvDocument);
    }

    protected boolean validateRecurringDVForeignVendor(RecurringDisbursementVoucherDocument rcdvDocument) {
        boolean isValid = true;
        DisbursementVoucherPayeeDetail payeeDetail = rcdvDocument.getDvPayeeDetail();
        String payeeTypeCode = payeeDetail.getDisbursementVoucherPayeeTypeCode();
        Integer vendorHeaderId = payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger();
        
        if (StringUtils.isNotBlank(payeeTypeCode) && vendorHeaderId != null) {
            if (cuDisbursementVoucherTaxService.isForeignVendor(payeeTypeCode, vendorHeaderId)) {
                LOG.info("validateRecurringDVForeignVendor, found a foreign vendor, which is not allowed on a recurring DV");
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KFSConstants.GENERAL_PAYMENT_TAB_ERRORS, 
                        CUKFSKeyConstants.ERROR_RCDV_NO_FOREIGN_VENDORS);
                isValid = false;
            }
        } else {
            LOG.info("validateRecurringDVForeignVendor, could not validate, no payee type code, or no vendor header id");
        }
        return isValid;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public void setCuDisbursementVoucherTaxService(CuDisbursementVoucherTaxService cuDisbursementVoucherTaxService) {
        this.cuDisbursementVoucherTaxService = cuDisbursementVoucherTaxService;
    }

}
