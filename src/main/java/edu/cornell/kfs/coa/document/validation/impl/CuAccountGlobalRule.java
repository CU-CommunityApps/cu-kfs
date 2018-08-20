package edu.cornell.kfs.coa.document.validation.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.document.validation.impl.AccountGlobalRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuAccountGlobalRule extends AccountGlobalRule {
    
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        if (StringUtils.isNotBlank(newAccountGlobal.getAccountRestrictedStatusCode()) && StringUtils.isBlank(newAccountGlobal.getSubFundGroupCode())) {
            valid = isTheNewRestrictionCodeValidForEachAccount() && valid;
        }
        return valid;
    }
    
    protected boolean isTheNewRestrictionCodeValidForEachAccount() {
        boolean valid = true;
        int accountDetailIndex = 0;
        for (AccountGlobalDetail detail : newAccountGlobal.getAccountGlobalDetails()) {
            String accountSubFundGroupRestrictionCode = detail.getAccount().getSubFundGroup().getAccountRestrictedStatusCode();
            String accountGlobalRestrictionCode = newAccountGlobal.getAccountRestrictedStatusCode();
            if (StringUtils.isNotBlank(accountSubFundGroupRestrictionCode) && 
                    !StringUtils.equalsAnyIgnoreCase(accountSubFundGroupRestrictionCode, accountGlobalRestrictionCode)) {
                valid = false;
                //String fieldNameFormat = "accountGlobalDetails[{0}].accountNumber";
                String fieldNameFormat = "accountGlobalDetails[{0}]";
                putFieldError(MessageFormat.format(fieldNameFormat, String.valueOf(accountDetailIndex)), 
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_DETAILS_INVALID_RESTRICTION_CODE_CHANGE, detail.getAccountNumber());
            }
            accountDetailIndex++;
        }
        return valid;
    }
}
