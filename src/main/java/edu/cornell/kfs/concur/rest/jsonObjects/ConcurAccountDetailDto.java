package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;

public class ConcurAccountDetailDto {
    private static final SimpleDateFormat SDF = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
    
    private boolean active;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String accountName;
    private String orgCode;
    private String accountTypeCode;
    private String fundGroupCode;
    private String subFundGroupCode;
    private String subFundProgramCode;
    private String fiscalOfficerName;
    private String accountManagerName;
    private String accountSupervisorName;
    private boolean closed;
    private String accountCreateDate;
    private String accountExpirateDate;
    private String higherEdFunctionCode;
    
    
    
    public ConcurAccountDetailDto(Account account) {
        AccountExtendedAttribute accountExtendedAtribute = (AccountExtendedAttribute) account.getExtension();
        
        this.active = account.isActive();
        this.chartOfAccountsCode = account.getChartOfAccountsCode();
        this.accountNumber = account.getAccountNumber();
        this.accountName = account.getAccountName();
        this.orgCode = account.getOrganizationCode();
        this.accountTypeCode = account.getAccountTypeCode();
        this.fundGroupCode = account.getSubFundGroup().getFundGroupCode();
        this.subFundGroupCode = account.getSubFundGroupCode();
        this.subFundProgramCode = accountExtendedAtribute.getProgramCode();
        this.fiscalOfficerName = account.getAccountFiscalOfficerUser().getName();
        this.accountManagerName = account.getAccountManagerUser().getName();
        this.accountSupervisorName = account.getAccountSupervisoryUser().getName();
        this.closed = account.isClosed();
        this.accountCreateDate = formatDate(account.getAccountCreateDate());
        this.accountExpirateDate = formatDate(account.getAccountExpirationDate());
        this.higherEdFunctionCode = account.getFinancialHigherEdFunctionCd();
    }
    
    private String formatDate(Date date) {
        if (date != null) {
            return SDF.format(date);
        } else {
            return StringUtils.EMPTY;
        }
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getFundGroupCode() {
        return fundGroupCode;
    }

    public void setFundGroupCode(String fundGroupCode) {
        this.fundGroupCode = fundGroupCode;
    }

    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }

    public String getSubFundProgramCode() {
        return subFundProgramCode;
    }

    public void setSubFundProgramCode(String subFundProgramCode) {
        this.subFundProgramCode = subFundProgramCode;
    }

    public String getFiscalOfficerName() {
        return fiscalOfficerName;
    }

    public void setFiscalOfficerName(String fiscalOfficerName) {
        this.fiscalOfficerName = fiscalOfficerName;
    }

    public String getAccountManagerName() {
        return accountManagerName;
    }

    public void setAccountManagerName(String accountManagerName) {
        this.accountManagerName = accountManagerName;
    }

    public String getAccountSupervisorName() {
        return accountSupervisorName;
    }

    public void setAccountSupervisorName(String accountSupervisorName) {
        this.accountSupervisorName = accountSupervisorName;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getAccountCreateDate() {
        return accountCreateDate;
    }

    public void setAccountCreateDate(String accountCreateDate) {
        this.accountCreateDate = accountCreateDate;
    }

    public String getAccountExpirateDate() {
        return accountExpirateDate;
    }

    public void setAccountExpirateDate(String accountExpirateDate) {
        this.accountExpirateDate = accountExpirateDate;
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
