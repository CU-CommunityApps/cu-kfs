package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import java.sql.Date;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CemiAwardLegacyAccountSubAccountDataBo extends PersistableBusinessObjectBase {
    
    // account attributes
    private String proposalNumber;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String projectDirectorId;
    private boolean awdAccountActive;
    private boolean finalBilledIndicator;
    private Date currentLastBilledDate;
    private Date previousLastBilledDate;
    private String accountSubFundGroupCode;
    private Integer accountCgAccountResponsibilityId;
    private String accountTypeCode;
    private String accountCgCfdaNumber;
    private boolean accountAccountClosedIndicator;
    private String accountPurchaseOrderNumber;
    
    // sub-account attributes
    private String subAccountNumber;
    private String subAccountName;
    private boolean subAccountActive;
    
    public CemiAwardLegacyAccountSubAccountDataBo() {
    }
    
    public CemiAwardLegacyAccountSubAccountDataBo(String proposalNumber, String chartOfAccountsCode,
            String accountNumber, String projectDirectorId, boolean awdAccountActive, boolean finalBilledIndicator,
            Date currentLastBilledDate, Date previousLastBilledDate, String accountSubFundGroupCode,
            Integer accountCgAccountResponsibilityId, String accountTypeCode, String accountCgCfdaNumber,
            boolean accountAccountClosedIndicator, String accountPurchaseOrderNumber, String subAccountNumber,
            String subAccountName, boolean subAccountActive) {
        this.proposalNumber = proposalNumber;
        this.chartOfAccountsCode = chartOfAccountsCode; 
        this.accountNumber = accountNumber;
        this.projectDirectorId = projectDirectorId;
        this.awdAccountActive = awdAccountActive;
        this.finalBilledIndicator = finalBilledIndicator;
        this.currentLastBilledDate = currentLastBilledDate;
        this.previousLastBilledDate = previousLastBilledDate;
        this.accountCgAccountResponsibilityId = accountCgAccountResponsibilityId;
        this.accountTypeCode = accountTypeCode;
        this.accountCgCfdaNumber = accountCgCfdaNumber;
        this.accountAccountClosedIndicator = accountAccountClosedIndicator;
        this.accountPurchaseOrderNumber = accountPurchaseOrderNumber;
        this.subAccountNumber = subAccountNumber;
        this.subAccountName = subAccountName;
        this.subAccountActive = subAccountActive;
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
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

    public String getProjectDirectorId() {
        return projectDirectorId;
    }

    public void setProjectDirectorId(String projectDirectorId) {
        this.projectDirectorId = projectDirectorId;
    }

    public boolean isAwdAccountActive() {
        return awdAccountActive;
    }

    public void setAwdAccountActive(boolean awdAccountActive) {
        this.awdAccountActive = awdAccountActive;
    }

    public boolean isFinalBilledIndicator() {
        return finalBilledIndicator;
    }

    public void setFinalBilledIndicator(boolean finalBilledIndicator) {
        this.finalBilledIndicator = finalBilledIndicator;
    }

    public Date getCurrentLastBilledDate() {
        return currentLastBilledDate;
    }

    public void setCurrentLastBilledDate(Date currentLastBilledDate) {
        this.currentLastBilledDate = currentLastBilledDate;
    }

    public Date getPreviousLastBilledDate() {
        return previousLastBilledDate;
    }

    public void setPreviousLastBilledDate(Date previousLastBilledDate) {
        this.previousLastBilledDate = previousLastBilledDate;
    }

    public String getAccountSubFundGroupCode() {
        return accountSubFundGroupCode;
    }

    public void setAccountSubFundGroupCode(String accountSubFundGroupCode) {
        this.accountSubFundGroupCode = accountSubFundGroupCode;
    }

    public Integer getAccountCgAccountResponsibilityId() {
        return accountCgAccountResponsibilityId;
    }

    public void setAccountCgAccountResponsibilityId(Integer accountCgAccountResponsibilityId) {
        this.accountCgAccountResponsibilityId = accountCgAccountResponsibilityId;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getAccountCgCfdaNumber() {
        return accountCgCfdaNumber;
    }

    public void setAccountCgCfdaNumber(String accountCgCfdaNumber) {
        this.accountCgCfdaNumber = accountCgCfdaNumber;
    }

    public boolean isAccountAccountClosedIndicator() {
        return accountAccountClosedIndicator;
    }

    public void setAccountAccountClosedIndicator(boolean accountAccountClosedIndicator) {
        this.accountAccountClosedIndicator = accountAccountClosedIndicator;
    }

    public String getAccountPurchaseOrderNumber() {
        return accountPurchaseOrderNumber;
    }

    public void setAccountPurchaseOrderNumber(String accountPurchaseOrderNumber) {
        this.accountPurchaseOrderNumber = accountPurchaseOrderNumber;
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

    public boolean isSubAccountActive() {
        return subAccountActive;
    }

    public void setSubAccountActive(boolean subAccountActive) {
        this.subAccountActive = subAccountActive;
    }

}
