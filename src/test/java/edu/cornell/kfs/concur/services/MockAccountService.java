package edu.cornell.kfs.concur.services;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.kim.impl.identity.Person;

import java.util.Set;

public class MockAccountService implements AccountService {

    @Override
    public Account getByPrimaryId(String chartOfAccountsCode, String accountNumber) {
        Account account = null;
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_ACCT_NBR.equalsIgnoreCase(accountNumber)) {
            account = createAccount(chartOfAccountsCode, accountNumber);
            account.setActive(true);
        }
        
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR.equalsIgnoreCase(accountNumber)) {
            account = createAccount(chartOfAccountsCode, accountNumber);
            account.setActive(false);
        }
        
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.CLOSED_ACCT_NBR.equalsIgnoreCase(accountNumber)) {
            account = createAccount(chartOfAccountsCode, accountNumber);
            account.setClosed(true);
        }
        return account;
    }
    
    private Account createAccount(String chartOfAccountsCode, String accountNumber) {
        Account account = new Account();
        account.setChartOfAccountsCode(chartOfAccountsCode);
        account.setAccountNumber(accountNumber);
        return account;
    }

    @Override
    public Account getByPrimaryIdWithCaching(String chartOfAccountsCode, String accountNumber) {
        return null;
    }

    @Override
    public AccountDelegate getPrimaryDelegationByExample(AccountDelegate delegateExample, String totalDollarAmount) {
        return null;
    }

    @Override
    public List getSecondaryDelegationsByExample(AccountDelegate delegateExample, String totalDollarAmount) {
        return null;
    }

    @Override
    public List getAccountsThatUserIsResponsibleFor(Person kualiUser) {
        return null;
    }

    @Override
    public boolean hasResponsibilityOnAccount(Person kualiUser, Account account) {
        return false;
    }

    @Override
    public Iterator getAllAccounts() {
        return null;
    }

    @Override
    public Iterator<Account> getActiveAccountsForFiscalOfficer(String principalId) {
        return null;
    }

    @Override
    public Iterator<Account> getExpiredAccountsForFiscalOfficer(String principalId) {
        return null;
    }

    @Override
    public Iterator<Account> getActiveAccountsForAccountSupervisor(String principalId) {
        return null;
    }

    @Override
    public Iterator<Account> getExpiredAccountsForAccountSupervisor(String principalId) {
        return null;
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormFiscalOfficer(String principalId) {
        return false;
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormAccountSupervisor(String principalId) {
        return false;
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormAccountManager(String principalId) {
        return false;
    }

    @Override
    public Collection<Account> getAccountsForAccountNumber(String accountNumber) {
        return null;
    }

    @Override
    public String getDefaultLaborBenefitRateCategoryCodeForAccountType(String accountTypeCode) {
        return null;
    }

    @Override
    public Boolean isFridgeBenefitCalculationEnable() {
        return null;
    }

    @Override
    public Account getUniqueAccountForAccountNumber(String accountNumber) {
        return null;
    }

    @Override
    public boolean accountsCanCrossCharts() {
        return false;
    }

    @Override
    public void populateAccountingLineChartIfNeeded(AccountingLine line) {

    }

    @Override
    public Account getUnexpiredContinuationAccountOrNull(Account account) {
        return null;
    }
    
    @Override
    public void updateRoleAssignmentsForAccountChange(String docIdToIgnore, Set<String> accountNumbers) {
        
    }
    
    public void updateRoleAssignmentsForAccountChange(
            String docIdToIgnore,
            Set<String> accountNumbers,
            Set<String> documentTypes) {
        
    }

}
