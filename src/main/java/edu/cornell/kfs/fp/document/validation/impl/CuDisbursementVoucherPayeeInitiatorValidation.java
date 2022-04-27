package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherPayeeInitiatorValidation;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherPayeeInitiatorValidation extends DisbursementVoucherPayeeInitiatorValidation {
    private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherPayeeInitiatorValidation.class);
    
    protected AccountingDocument accountingDocumentForValidation;
    
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");        
        boolean isValid = true;
        
        CuDisbursementVoucherDocument document = (CuDisbursementVoucherDocument) accountingDocumentForValidation;
        CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) document.getDvPayeeDetail();
        
        MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);

        String uuid = null;
        // If payee is a vendor, then look up SSN and look for SSN in the employee table
        if (payeeDetail.isVendor() && StringUtils.isNotBlank(payeeDetail.getDisbVchrVendorHeaderIdNumber())) {
            VendorDetail dvVendor = retrieveVendorDetail(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger(), payeeDetail.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            // if the vendor tax type is SSN, then check the tax number
            if (dvVendor != null && DisbursementVoucherConstants.TAX_TYPE_SSN.equals(dvVendor.getVendorHeader().getVendorTaxTypeCode())) {
                // check ssn against employee table
                Person user = retrieveEmployeeBySSN(dvVendor.getVendorHeader().getVendorTaxNumber());
                if (user != null) {
                    uuid = user.getPrincipalId();
                }
            }
        } else if (payeeDetail.isEmployee()) {
            Person employee = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeDetail.getDisbVchrEmployeeIdNumber());
            uuid = employee.getPrincipalId();
        } else if (payeeDetail.isStudent() || payeeDetail.isAlumni()) {
            uuid = payeeDetail.getDisbVchrPayeeIdNumber();
        }

        // If a uuid was found for payee, check it against the initiator uuid
        if (StringUtils.isNotBlank(uuid)) {
            Person initUser = getInitiator(document);
            if (uuid.equals(initUser.getPrincipalId())) {
                errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, FPKeyConstants.ERROR_PAYEE_INITIATOR);
                isValid = false;
            }
        }
        
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT);   
        
        return isValid;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

}
