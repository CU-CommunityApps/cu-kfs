package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.document.validation.impl.SubAccountRule;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;

public class CuSubAccountRule extends SubAccountRule {
	
    @Override
    protected boolean checkCgCostSharingRules() {
        boolean allFieldsSet = false;

        final A21SubAccount a21 = newSubAccount.getA21SubAccount();

        // check to see if all required fields are set
        if (StringUtils.isNotEmpty(a21.getCostShareChartOfAccountCode())
                && StringUtils.isNotEmpty(a21.getCostShareSourceAccountNumber())) {
            allFieldsSet = true;
        }

        // Cost Sharing COA Code and Cost Sharing Account Number are required
        boolean success = checkEmptyBOField("a21SubAccount.costShareChartOfAccountCode",
                a21.getCostShareChartOfAccountCode(), "Cost Share Chart of Accounts Code");
        success &= checkEmptyBOField("a21SubAccount.costShareSourceAccountNumber",
                a21.getCostShareSourceAccountNumber(), "Cost Share AccountNumber");
            
        // existence test on Cost Share Account
        if (allFieldsSet) {
            if (ObjectUtils.isNull(a21.getCostShareAccount())) {
                putFieldError("a21SubAccount.costShareSourceAccountNumber", KFSKeyConstants.ERROR_EXISTENCE,
                        getDisplayName("a21SubAccount.costShareSourceAccountNumber"));
                success = false;
            }
        }

        // existence test on Cost Share SubAccount
        if (allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceSubAccountNumber())) {
            if (ObjectUtils.isNull(a21.getCostShareSourceSubAccount())) {
                putFieldError("a21SubAccount.costShareSourceSubAccountNumber", KFSKeyConstants.ERROR_EXISTENCE,
                    getDisplayName("a21SubAccount.costShareSourceSubAccountNumber"));
                success = false;
            }
        }

        return success;
    }

    /**
     * This method tests if all fields in the ICR section are empty.
     *
     * @return true if the ICR values passed in are empty, otherwise false.
     */
    @Override
    protected boolean checkCgIcrIsEmpty() {
        boolean success = true;

        final A21SubAccount newA21SubAccount = newSubAccount.getA21SubAccount();
        if (ObjectUtils.isNotNull(newA21SubAccount)) {
            success = StringUtils.isEmpty(newA21SubAccount.getFinancialIcrSeriesIdentifier());

            success &= checkICRCollectionExist(false);
            success &= StringUtils.isEmpty(newA21SubAccount.getIndirectCostRecoveryTypeCode());
        }

        return success;
    }

}
