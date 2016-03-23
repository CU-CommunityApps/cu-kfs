package edu.cornell.kfs.coa.document.validation.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class SubAccountGlobalRule extends GlobalDocumentRuleBase {
	
	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = true;
		success &= checkSubAccountDetails();
		return success;
	}
	
	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = true;
		success &= checkSubAccountDetails();
		return success;
	}
	
	 /**
	 * Checks that at least one sub account is entered.
	 * 
	 * @return true if at least one sub account entered, false otherwise
	 */
	public boolean checkSubAccountDetails() {
		boolean success = true;

		SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
		List<SubAccountGlobalDetail> details = newSubAccountGlobal.getSubAccountGlobalDetails();
		// check if there are any accounts
		if (details.size() == 0) {
			putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + "subAccountGlobalDetails.accountNumber", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUS_ACCOUNT_NO_SUB_ACCOUNTS);
			success = false;
		}
		return success;
	 }
	 
	
	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument, java.lang.String, org.kuali.rice.krad.bo.PersistableBusinessObject)
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		SubAccountGlobalDetail detail = (SubAccountGlobalDetail) line;
		boolean success = true;

		success &= checkSubAccountDetail(detail);
		return success;
	}
	
    /**
     * Checks that sub account global detail fields are valid. Also checks that the chart, account, sub account are valid.
     * 
     * @param dtl
     * @return true if valid, false otherwise
     */
    public boolean checkSubAccountDetail(SubAccountGlobalDetail dtl) {
        boolean success = true;
        int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        getDictionaryValidationService().validateBusinessObject(dtl);
        if (StringUtils.isNotBlank(dtl.getAccountNumber()) && StringUtils.isNotBlank(dtl.getChartOfAccountsCode())) {
            dtl.refreshReferenceObject("account");
            if (ObjectUtils.isNull(dtl.getAccount())) {
                GlobalVariables.getMessageMap().putError("accountNumber", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_ACCOUNT, new String[] { dtl.getChartOfAccountsCode(), dtl.getAccountNumber() });
            }
            
            dtl.refreshReferenceObject("subAccount");
            if (ObjectUtils.isNull(dtl.getAccount())) {
                GlobalVariables.getMessageMap().putError("subAccountNumber", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_SUB_ACCOUNT, new String[] { dtl.getChartOfAccountsCode(), dtl.getAccountNumber(), dtl.getSubAccountNumber() });
            }
        }
        success &= GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;

        return success;
    }
    
}
