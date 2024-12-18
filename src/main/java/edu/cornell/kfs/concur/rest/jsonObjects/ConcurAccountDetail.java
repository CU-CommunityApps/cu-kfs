package edu.cornell.kfs.concur.rest.jsonObjects;

import org.kuali.kfs.coa.businessobject.Account;

public class ConcurAccountDetail {
    
    private boolean active;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subFundGroupCode;
    private String higherEdFunctionCode;
    
    public ConcurAccountDetail(Account account) {
        this.active = account.isActive();
        this.chartOfAccountsCode = account.getChartOfAccountsCode();
        this.accountNumber = account.getAccountNumber();
        this.subFundGroupCode = account.getSubFundGroupCode();
        this.higherEdFunctionCode = account.getFinancialHigherEdFunctionCd();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }

    public String getHigherEdFunctionCode() {
        return higherEdFunctionCode;
    }

    public void setHigherEdFunctionCode(String higherEdFunctionCode) {
        this.higherEdFunctionCode = higherEdFunctionCode;
    }
    
    
}
