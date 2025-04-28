package edu.cornell.kfs.coa.batch.businessobject;

import java.sql.Date;

public class WorkdayOpenAccountDetailDTO {
    
    private String chart;
    private String accountNumber;
    private String accountName;
    private String subFundGroupWageIndicator;
    private String subFundGroupCode;
    private String higherEdFunctionCode;
    private Date accountEffectiveDate;
    private String accountClosedIndicator;
    private String accountTypeCode;
    private String subAccountNumber;
    private String subAccountName;
    private String subAccountActiveIndicator;
    private String objectCode;
    private String subObjectCode;
    private String subObjectName;
    private String accountCfdaNumber;
    
    public WorkdayOpenAccountDetailDTO() {
        super();
    }
    
    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
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

    public String getSubFundGroupWageIndicator() {
        return subFundGroupWageIndicator;
    }

    public void setSubFundGroupWageIndicator(String subFundGroupWageIndicator) {
        this.subFundGroupWageIndicator = subFundGroupWageIndicator;
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

    public Date getAccountEffectiveDate() {
        return accountEffectiveDate;
    }

    public void setAccountEffectiveDate(Date accountEffectiveDate) {
        this.accountEffectiveDate = accountEffectiveDate;
    }

    public String getAccountClosedIndicator() {
        return accountClosedIndicator;
    }

    public void setAccountClosedIndicator(String accountClosedIndicator) {
        this.accountClosedIndicator = accountClosedIndicator;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getSubAccountName() {
        return subAccountName;
    }

    public void setSubAccountName(String subAccountName) {
        this.subAccountName = subAccountName;
    }

    public String getSubAccountActiveIndicator() {
        return subAccountActiveIndicator;
    }

    public void setSubAccountActiveIndicator(String subAccountActiveIndicator) {
        this.subAccountActiveIndicator = subAccountActiveIndicator;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public String getSubObjectName() {
        return subObjectName;
    }

    public void setSubObjectName(String subObjectName) {
        this.subObjectName = subObjectName;
    }

    public String getAccountCfdaNumber() {
        return accountCfdaNumber;
    }

    public void setAccountCfdaNumber(String accountCfdaNumber) {
        this.accountCfdaNumber = accountCfdaNumber;
    }
}
