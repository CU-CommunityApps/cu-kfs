package edu.cornell.kfs.cemi.pdp.batch.businessobject;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.cemi.pdp.batch.dto.CemiGroupTwo;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiPaymentElectionGroupTwoBo extends PersistableBusinessObjectBase implements Serializable {
    
    private static final long serialVersionUID = 1915469127589491907L;
    
    private Long extractTableUniqueRowId;
    private String jobRunDateAsString;
    private KualiInteger achAccountGeneratedIdentifierUsedForDataRow; 
    
    private String employeeId;
    private String paymentElectionGroupRule_2;
    private String paymentElectionRule_2_1;
    private String electionCountry_2_1;
    private String electionCurrency_2_1;
    private String paymentType_2_1;
    private String accountCountry_2_1; 
    private String accountCurrency_2_1;
    private String bankAccountNickname_2_1;
    private String bankAccountName_2_1;
    private String accountNumber_2_1; 
    private String accountType_2_1;
    private String bankName_2_1;
    private String bankRoutingNumber_2_1;
    private String iban_2_1;
    private String bic_2_1;
    private String branchName_2_1;
    private String branchIdNumber_2_1;
    private String checkDigit_2_1;
    private String distributionAmount_2_1;
    private String distributionPercentage_2_1;
    private String distributionBalance_2_1;
    
    public CemiPaymentElectionGroupTwoBo(CemiGroupTwo groupTwo, String employeeId,
            KualiInteger achAccountGeneratedIdentifierForRow, LocalDateTime jobRunDate,
            CemiPaymentElectionGroupTwoBoSequence paymentElectionGroupTwoTabTableSequence) {
        
        // These values are to make the row that would appear in an extract spreadsheet seachable as well as
        // identifiable by the actual KFS data object keys used to create that row and date-time of the extract file.
        this.extractTableUniqueRowId = paymentElectionGroupTwoTabTableSequence.getLongValue();
        this.jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        this.achAccountGeneratedIdentifierUsedForDataRow = achAccountGeneratedIdentifierForRow;
        
        // These table data values should be the same as what would be in the extract file tabbed sheet columns.
        this.employeeId = employeeId;
        this.paymentElectionGroupRule_2 = groupTwo.getPaymentElectionGroupRule_2();
        this.paymentElectionRule_2_1 = groupTwo.getPaymentElectionRule_2_1();
        this.electionCountry_2_1 = groupTwo.getElectionCountry_2_1();
        this.electionCurrency_2_1 = groupTwo.getElectionCurrency_2_1();
        this.paymentType_2_1 = groupTwo.getPaymentType_2_1();
        this.accountCountry_2_1 = groupTwo.getAccountCountry_2_1(); 
        this.accountCurrency_2_1 = groupTwo.getAccountCurrency_2_1();
        this.bankAccountNickname_2_1 = groupTwo.getBankAccountNickname_2_1();
        this.bankAccountName_2_1 = groupTwo.getBankAccountName_2_1();
        this.accountNumber_2_1 = groupTwo.getAccountNumber_2_1();
        this.accountType_2_1 = groupTwo.getAccountType_2_1();
        this.bankName_2_1 = groupTwo.getBankName_2_1();
        this.bankRoutingNumber_2_1 = groupTwo.getBankRoutingNumber_2_1();
        this.iban_2_1 = groupTwo.getIban_2_1();
        this.bic_2_1 = groupTwo.getBic_2_1();
        this.branchName_2_1 = groupTwo.getBranchName_2_1();
        this.branchIdNumber_2_1 = groupTwo.getBranchIdNumber_2_1();
        this.checkDigit_2_1 = groupTwo.getCheckDigit_2_1();
        this.distributionAmount_2_1 = groupTwo.getDistributionBalance_2_1();
        this.distributionPercentage_2_1 = groupTwo.getDistributionPercentage_2_1();
        this.distributionBalance_2_1 = groupTwo.getDistributionBalance_2_1();
    }

    public Long getExtractTableUniqueRowId() {
        return extractTableUniqueRowId;
    }

    public void setExtractTableUniqueRowId(Long extractTableUniqueRowId) {
        this.extractTableUniqueRowId = extractTableUniqueRowId;
    }

    public String getJobRunDateAsString() {
        return jobRunDateAsString;
    }

    public void setJobRunDateAsString(String jobRunDateAsString) {
        this.jobRunDateAsString = jobRunDateAsString;
    }
    
    public KualiInteger getAchAccountGeneratedIdentifierUsedForDataRow() {
        return achAccountGeneratedIdentifierUsedForDataRow;
    }

    public void setAchAccountGeneratedIdentifierUsedForDataRow(KualiInteger achAccountGeneratedIdentifierUsedForDataRow) {
        this.achAccountGeneratedIdentifierUsedForDataRow = achAccountGeneratedIdentifierUsedForDataRow;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPaymentElectionGroupRule_2() {
        return paymentElectionGroupRule_2;
    }

    public void setPaymentElectionGroupRule_2(String paymentElectionGroupRule_2) {
        this.paymentElectionGroupRule_2 = paymentElectionGroupRule_2;
    }

    public String getPaymentElectionRule_2_1() {
        return paymentElectionRule_2_1;
    }

    public void setPaymentElectionRule_2_1(String paymentElectionRule_2_1) {
        this.paymentElectionRule_2_1 = paymentElectionRule_2_1;
    }

    public String getElectionCountry_2_1() {
        return electionCountry_2_1;
    }

    public void setElectionCountry_2_1(String electionCountry_2_1) {
        this.electionCountry_2_1 = electionCountry_2_1;
    }

    public String getElectionCurrency_2_1() {
        return electionCurrency_2_1;
    }

    public void setElectionCurrency_2_1(String electionCurrency_2_1) {
        this.electionCurrency_2_1 = electionCurrency_2_1;
    }

    public String getPaymentType_2_1() {
        return paymentType_2_1;
    }

    public void setPaymentType_2_1(String paymentType_2_1) {
        this.paymentType_2_1 = paymentType_2_1;
    }

    public String getAccountCountry_2_1() {
        return accountCountry_2_1;
    }

    public void setAccountCountry_2_1(String accountCountry_2_1) {
        this.accountCountry_2_1 = accountCountry_2_1;
    }

    public String getAccountCurrency_2_1() {
        return accountCurrency_2_1;
    }

    public void setAccountCurrency_2_1(String accountCurrency_2_1) {
        this.accountCurrency_2_1 = accountCurrency_2_1;
    }

    public String getBankAccountNickname_2_1() {
        return bankAccountNickname_2_1;
    }

    public void setBankAccountNickname_2_1(String bankAccountNickname_2_1) {
        this.bankAccountNickname_2_1 = bankAccountNickname_2_1;
    }

    public String getBankAccountName_2_1() {
        return bankAccountName_2_1;
    }

    public void setBankAccountName_2_1(String bankAccountName_2_1) {
        this.bankAccountName_2_1 = bankAccountName_2_1;
    }

    public String getAccountNumber_2_1() {
        return accountNumber_2_1;
    }

    public void setAccountNumber_2_1(String accountNumber_2_1) {
        this.accountNumber_2_1 = accountNumber_2_1;
    }

    public String getAccountType_2_1() {
        return accountType_2_1;
    }

    public void setAccountType_2_1(String accountType_2_1) {
        this.accountType_2_1 = accountType_2_1;
    }

    public String getBankName_2_1() {
        return bankName_2_1;
    }

    public void setBankName_2_1(String bankName_2_1) {
        this.bankName_2_1 = bankName_2_1;
    }

    public String getBankRoutingNumber_2_1() {
        return bankRoutingNumber_2_1;
    }

    public void setBankRoutingNumber_2_1(String bankRoutingNumber_2_1) {
        this.bankRoutingNumber_2_1 = bankRoutingNumber_2_1;
    }

    public String getIban_2_1() {
        return iban_2_1;
    }

    public void setIban_2_1(String iban_2_1) {
        this.iban_2_1 = iban_2_1;
    }

    public String getBic_2_1() {
        return bic_2_1;
    }

    public void setBic_2_1(String bic_2_1) {
        this.bic_2_1 = bic_2_1;
    }

    public String getBranchName_2_1() {
        return branchName_2_1;
    }

    public void setBranchName_2_1(String branchName_2_1) {
        this.branchName_2_1 = branchName_2_1;
    }

    public String getBranchIdNumber_2_1() {
        return branchIdNumber_2_1;
    }

    public void setBranchIdNumber_2_1(String branchIdNumber_2_1) {
        this.branchIdNumber_2_1 = branchIdNumber_2_1;
    }

    public String getCheckDigit_2_1() {
        return checkDigit_2_1;
    }

    public void setCheckDigit_2_1(String checkDigit_2_1) {
        this.checkDigit_2_1 = checkDigit_2_1;
    }

    public String getDistributionAmount_2_1() {
        return distributionAmount_2_1;
    }

    public void setDistributionAmount_2_1(String distributionAmount_2_1) {
        this.distributionAmount_2_1 = distributionAmount_2_1;
    }

    public String getDistributionPercentage_2_1() {
        return distributionPercentage_2_1;
    }

    public void setDistributionPercentage_2_1(String distributionPercentage_2_1) {
        this.distributionPercentage_2_1 = distributionPercentage_2_1;
    }

    public String getDistributionBalance_2_1() {
        return distributionBalance_2_1;
    }

    public void setDistributionBalance_2_1(String distributionBalance_2_1) {
        this.distributionBalance_2_1 = distributionBalance_2_1;
    }

}
