package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 */
public class DisbursementVoucherInitiatorPrimaryDepartmentValidation extends GenericValidation {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherInitiatorPrimaryDepartmentValidation.class);
    private AccountingDocument accountingDocumentForValidation;

    /**
     * Validates that the initiator has a primary department id set up.
     * 
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean isValid = true;
        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) accountingDocumentForValidation;
        String initiatorPrincipalId = dvDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();

        ChartOrgHolder chartOrg = SpringContext.getBean(org.kuali.kfs.sys.service.FinancialSystemUserService.class).getPrimaryOrganization(initiatorPrincipalId, KFSConstants.ParameterNamespaces.FINANCIAL);

        if (ObjectUtils.isNull(chartOrg) || ObjectUtils.isNull(chartOrg.getOrganization())) {

            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_DV_INITIATOR_INVALID_PRIMARY_DEPARTMENT);

            isValid = false;
        }
        return isValid;
    }

    /**
     * Gets the accountingDocumentForValidation.
     * 
     * @return accountingDocumentForValidation
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;

    }

    /**
     * Sets the accountingDocumentForValidation.
     * 
     * @param accountingDocumentForValidation
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;

    }

}
