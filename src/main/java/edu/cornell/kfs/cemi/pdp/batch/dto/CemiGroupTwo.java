package edu.cornell.kfs.cemi.pdp.batch.dto;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiGroupTwo {
    
    private static final Logger LOG = LogManager.getLogger();
    
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
    
    public CemiGroupTwo(PayeeACHAccount payeeAccountInfo, String employeeId, String bankNameViaBankLookup, boolean maskSensitiveData) {
        this.employeeId = employeeId;
        this.paymentElectionGroupRule_2 = CemiPaymentElectionConstants.EXPENSE_PAYMENTS;
        this.paymentElectionRule_2_1 = CemiPaymentElectionConstants.EXPENSE_PAYMENTS;
        this.electionCountry_2_1 = CemiPaymentElectionConstants.US;
        this.electionCurrency_2_1 = CemiPaymentElectionConstants.USD;
        this.paymentType_2_1 = CemiPaymentElectionConstants.DIRECT_DEPOSIT;
        this.accountCountry_2_1 = CemiPaymentElectionConstants.US; 
        this.accountCurrency_2_1 = CemiPaymentElectionConstants.USD;
        this.bankAccountNickname_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.bankAccountName_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.accountNumber_2_1 = determineBankAccountNumber(payeeAccountInfo, maskSensitiveData);
        this.accountType_2_1 = determineBankAccountType(payeeAccountInfo);
        this.bankName_2_1 = bankNameViaBankLookup;
        this.bankRoutingNumber_2_1 = determineBankRoutingNumber(payeeAccountInfo);
        this.iban_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.bic_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.branchName_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.branchIdNumber_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.checkDigit_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.distributionAmount_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.distributionPercentage_2_1 = CemiBaseConstants.EMPTY_STRING;
        this.distributionBalance_2_1 = CemiPaymentElectionConstants.TRUE;
    }
    
    private String determineBankAccountNumber(PayeeACHAccount payeeAccountInfo, boolean maskSensitiveData) {
        if (!maskSensitiveData) {
            return payeeAccountInfo.getBankAccountNumber();
        }
        return CemiPaymentElectionConstants.DUMMY_ACCOUNT_NUMBER;
    }

    private String determineBankAccountType(PayeeACHAccount payeeAccountInfo) {
        final String kfsAccountType = StringUtils.defaultString(payeeAccountInfo.getBankAccountTypeCode());
        final String cemiAccountType = CemiPaymentElectionConstants.KfsToWorkdayBankAccountTypeCodeConverter.get(kfsAccountType);
        if (StringUtils.isBlank(cemiAccountType)) {
            LOG.warn("determineBankAccountType, Payee Generated Account ID {} for Payee {} had a missing "
                    + "or unrecognized account type; defaulting to Checking account type",
                    payeeAccountInfo.getAchAccountGeneratedIdentifier(), payeeAccountInfo.getPayeeIdNumber());
            return CemiPaymentElectionConstants.WORKDAY_CHECKING_ACCOUNT_TYPE;
        }
        return cemiAccountType;
    }

    private String determineBankRoutingNumber(final PayeeACHAccount payeeAchAccount) {
        return payeeAchAccount.getBankRoutingNumber();
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
