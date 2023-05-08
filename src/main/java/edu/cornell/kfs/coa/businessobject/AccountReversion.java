package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

public class AccountReversion extends Reversion implements MutableInactivatable, FiscalYearBasedBusinessObject {

    private String accountNumber;
    private Account account;
    
    private List<Account> accounts; // This is only used by the "global" document
    private List<AccountReversionDetail> accountReversionDetails;
    
    private String accountReversionViewer;
    
    /**
     * Default constructor.
     */
    public AccountReversion() {
        accounts = new ArrayList<Account>();
        accountReversionDetails = new ArrayList<AccountReversionDetail>();
    }

    public List<AccountReversionDetail> getAccountReversionDetails() {
        return accountReversionDetails;
    }

    public void addAccountReversionDetail(AccountReversionDetail ard) {
        accountReversionDetails.add(ard);
    }

    public void setAccountReversionDetails(List<AccountReversionDetail> accountReversionDetails) {
        this.accountReversionDetails = accountReversionDetails;
    }

    public ReversionCategoryInfo getReversionDetail(String categoryCode) {
        for (AccountReversionDetail element : accountReversionDetails) {
            if (element.getAccountReversionCategoryCode().equals(categoryCode)) {
                if (!element.isActive()) {
                    return null; // don't send back inactive details
                } else {
                    return element;
                }
            }
        }
        return null;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    protected LinkedHashMap toStringMapper() {
        
        LinkedHashMap m = super.toStringMapper();
        
        m.put("accountNumber", this.accountNumber);
        return m;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String getSourceAttribute() {
        
        return accountNumber;
    }

    public String getAccountReversionViewer() {
        return accountReversionViewer;
    }

    public void setAccountReversionViewer(String accountReversionViewer) {
        this.accountReversionViewer = accountReversionViewer;
    }
}
