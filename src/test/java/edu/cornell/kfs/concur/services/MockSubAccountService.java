package edu.cornell.kfs.concur.services;

import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.service.SubAccountService;

public class MockSubAccountService implements SubAccountService {

    @Override
    public SubAccount getByPrimaryId(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        SubAccount subAccount = null;
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_ACCT_NBR.equalsIgnoreCase(accountNumber) && ConcurAccountValidationTestConstants.VALID_SUB_ACCT.equalsIgnoreCase(subAccountNumber)) {
            subAccount = createSubAccount(chartOfAccountsCode, accountNumber, subAccountNumber);
            subAccount.setActive(true);
        }
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_ACCT_NBR.equalsIgnoreCase(accountNumber) && ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT.equalsIgnoreCase(subAccountNumber)) {
            subAccount = createSubAccount(chartOfAccountsCode, accountNumber, subAccountNumber);
            subAccount.setActive(false);
        }
        return subAccount;
    }
    
    private SubAccount createSubAccount(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        SubAccount subAccount = new SubAccount();
        subAccount.setChartOfAccountsCode(chartOfAccountsCode);
        subAccount.setAccountNumber(accountNumber);
        subAccount.setSubAccountNumber(subAccountNumber);
        return subAccount;
    }

    @Override
    public SubAccount getByPrimaryIdWithCaching(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        return null;
    }

}
