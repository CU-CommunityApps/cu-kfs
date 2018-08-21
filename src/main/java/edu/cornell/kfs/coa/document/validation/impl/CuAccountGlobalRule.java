package edu.cornell.kfs.coa.document.validation.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.document.validation.impl.AccountGlobalRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

@SuppressWarnings("deprecation")
public class CuAccountGlobalRule extends AccountGlobalRule {
    
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        if (doesAccountGlobalHaveAccountRestrictionCodeAndNoSubFundGroupCode()) {
            valid = isTheNewRestrictionCodeValidForEachAccount() && valid;
        }
        return valid;
    }

    protected boolean doesAccountGlobalHaveAccountRestrictionCodeAndNoSubFundGroupCode() {
        return StringUtils.isNotBlank(newAccountGlobal.getAccountRestrictedStatusCode()) && StringUtils.isBlank(newAccountGlobal.getSubFundGroupCode());
    }
    
    protected boolean isTheNewRestrictionCodeValidForEachAccount() {
        boolean valid = true;
        int accountDetailIndex = 0;
        for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
            String subFundDefaultRestrictionCode = detail.getAccount().getSubFundGroup().getAccountRestrictedStatusCode();
            String accountGlobalRestrictionCode = newAccountGlobal.getAccountRestrictedStatusCode();
            if (ifSubFundDefaultRestrictionCodeExistsDoesItDifferFromAccountGlobalRestrictionCode(subFundDefaultRestrictionCode, accountGlobalRestrictionCode)) {
                valid = false;
                String accountSectionFieldName = MessageFormat.format(CUKFSPropertyConstants.ACCOUNT_GLOBAL_ACCOUNT_SECTION_FIELD_NAME_FORMAT, String.valueOf(accountDetailIndex));
                putFieldError(accountSectionFieldName, CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_DETAILS_INVALID_RESTRICTION_CODE_CHANGE, detail.getAccountNumber());
            }
            accountDetailIndex++;
        }
        return valid;
    }

    protected boolean ifSubFundDefaultRestrictionCodeExistsDoesItDifferFromAccountGlobalRestrictionCode(
            String subFundDefaultRestrictionCode, String accountGlobalRestrictionCode) {
        return StringUtils.isNotBlank(subFundDefaultRestrictionCode) && 
                !StringUtils.equalsIgnoreCase(subFundDefaultRestrictionCode, accountGlobalRestrictionCode);
    }
}
