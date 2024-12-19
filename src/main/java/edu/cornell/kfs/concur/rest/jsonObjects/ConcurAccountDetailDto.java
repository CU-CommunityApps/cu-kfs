package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.coa.businessobject.Account;

public class ConcurAccountDetailDto {
    
    private boolean active;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subFundGroupCode;
    private String higherEdFunctionCode;
    private String accountTypeCode;
    private String orgCode;
    
    public ConcurAccountDetailDto(Account account) {
        this.active = account.isActive();
        this.chartOfAccountsCode = account.getChartOfAccountsCode();
        this.accountNumber = account.getAccountNumber();
        this.subFundGroupCode = account.getSubFundGroupCode();
        this.higherEdFunctionCode = account.getFinancialHigherEdFunctionCd();
        this.accountTypeCode = account.getAccountTypeCode();
        this.orgCode = account.getOrganizationCode();
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
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
}
