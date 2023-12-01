package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherEmployeeInformationValidation;
import org.kuali.kfs.sys.KFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherEmployeeInformationValidation extends DisbursementVoucherEmployeeInformationValidation {
	
    private static final Logger LOG = LogManager.getLogger();
    
    
    public boolean validate(final AttributedDocumentEvent event) {
        LOG.debug("validate start");
        boolean isValid = true;
        
        final CuDisbursementVoucherDocument document = (CuDisbursementVoucherDocument) getAccountingDocumentForValidation();
        final DisbursementVoucherPayeeDetail payeeDetail = document.getDvPayeeDetail();
        
        if (!payeeDetail.isEmployee()
                || payeeDetail.isVendor()
                || !(document.getDocumentHeader().getWorkflowDocument().isInitiated()
                || document.getDocumentHeader().getWorkflowDocument().isSaved())) {
            return true;
        }
        
        final String employeeId = payeeDetail.getDisbVchrPayeeIdNumber();
        Person employee = personService.getPersonByEmployeeId(employeeId);
        
        final MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        final boolean stateIsInitiated = workflowDocument.isInitiated() || workflowDocument.isSaved();

        if (ObjectUtils.isNull(employee)) {
            employee = personService.getPerson(employeeId);
        } else  {
            if (!KFSConstants.EMPLOYEE_ACTIVE_STATUS.equals(employee.getEmployeeStatusCode()) &&
                    !CUKFSConstants.EMPLOYEE_RETIRED_STATUS.equals(employee.getEmployeeStatusCode())) {
                // If employee is found, then check that employee is active or retired if the doc has not already been routed.
                if(stateIsInitiated) {
                    final String label = dataDictionaryService.getAttributeLabel(DisbursementVoucherPayeeDetail.class,
                            KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER);
                    errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_INACTIVE, label);
                    isValid = false;
                }
            }
        }
        
     // check existence of employee
        if (employee == null) { 
            // If employee is not found, report existence error
            final String label = dataDictionaryService.getAttributeLabel(
                    DisbursementVoucherPayeeDetail.class, KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER);
            errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_EXISTENCE, label);
            isValid = false;
        } 
        
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT); 

        return isValid;
    }

}
