package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.document.validation.impl.SubAccountRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuSubAccountRule extends SubAccountRule {

    @Override
    protected boolean checkCgCostSharingRules() {

        boolean success = true;
        boolean allFieldsSet = false;

        A21SubAccount a21 = newSubAccount.getA21SubAccount();

        // check to see if all required fields are set
        if (StringUtils.isNotEmpty(a21.getCostShareChartOfAccountCode()) && StringUtils.isNotEmpty(a21.getCostShareSourceAccountNumber())) {
            allFieldsSet = true;
        }

        // Cost Sharing COA Code and Cost Sharing Account Number are required
        success &= checkEmptyBOField("a21SubAccount.costShareChartOfAccountCode", a21.getCostShareChartOfAccountCode(), "Cost Share Chart of Accounts Code");
        success &= checkEmptyBOField("a21SubAccount.costShareSourceAccountNumber", a21.getCostShareSourceAccountNumber(), "Cost Share AccountNumber");

        // existence test on Cost Share Account
        if (allFieldsSet) {
            if (ObjectUtils.isNull(a21.getCostShareAccount())) {
                putFieldError("a21SubAccount.costShareSourceAccountNumber", KFSKeyConstants.ERROR_EXISTENCE, getDisplayName("a21SubAccount.costShareSourceAccountNumber"));
                success &= false;
            }
        }

        // existence test on Cost Share SubAccount
        if (allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceSubAccountNumber())) {
            if (ObjectUtils.isNull(a21.getCostShareSourceSubAccount())) {
                putFieldError("a21SubAccount.costShareSourceSubAccountNumber", KFSKeyConstants.ERROR_EXISTENCE, getDisplayName("a21SubAccount.costShareSourceSubAccountNumber"));
                success &= false;
            }
        }
        
        return success;
    }

}
