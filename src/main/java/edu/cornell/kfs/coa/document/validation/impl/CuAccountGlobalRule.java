package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.document.validation.impl.AccountGlobalRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuAccountGlobalRule extends AccountGlobalRule {
    protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAccountGlobalRule.class);
    
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        if (StringUtils.isNotBlank(newAccountGlobal.getAccountRestrictedStatusCode()) && StringUtils.isBlank(newAccountGlobal.getSubFundGroupCode())) {
            LOG.info("processCustomRouteDocumentBusinessRules, there is a restriction status code, but no sub fund group code, so we need to check the accounts for valid sub fund gorup code");
            valid = isTheNewRestrictionCodeValidForEachAccount() && valid;
        } else {
            LOG.info("processCustomRouteDocumentBusinessRules, no need to verify the accounts have the right sub fund group for restriction code");
        }
        return valid;
    }
    
    protected boolean isTheNewRestrictionCodeValidForEachAccount() {
        boolean valid = true;
        for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
            String accountSubFundGroupRestrictionCode = detail.getAccount().getSubFundGroup().getAccountRestrictedStatusCode();
            String accountGlobalRestrictionCode = newAccountGlobal.getAccountRestrictedStatusCode();
            if (!StringUtils.equalsAnyIgnoreCase(accountSubFundGroupRestrictionCode, accountGlobalRestrictionCode)) {
                valid = false;
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(MAINTAINABLE_ERROR_PREFIX + KFSConstants.MAINTENANCE_ADD_PREFIX + "accountGlobalDetails.accountNumber", 
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_DETAILS_INVALID_RESTRICTION_CODE_CHANGE, 
                        detail.getAccountNumber(), detail.getAccount().getSubFundGroupCode(), accountGlobalRestrictionCode);
            }
        }
        return valid;
    }
}
