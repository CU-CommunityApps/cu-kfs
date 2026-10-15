package edu.cornell.kfs.cemi.pdp.batch.service.impl;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionFileGroupTwoTabRowBo;
import edu.cornell.kfs.cemi.pdp.batch.dto.CemiGroupTwo;

public class CemiPaymentElectionFileGroupTwoTabRowBoFactory {

    private PayeeACHAccount payeeAchAccount;
    private String employeeId;
    private String achBankName;
    private String jobRunDateString;
    private boolean maskSensitiveData = true;

    public CemiPaymentElectionFileGroupTwoTabRowBoFactory(final PayeeACHAccount payeeAchAccount,
            final String employeeId, final String achBankName, final String jobRunDateString,
            final boolean maskSensitiveData) {
        this.payeeAchAccount = payeeAchAccount;
        this.employeeId = employeeId;
        this.achBankName = achBankName;
        this.jobRunDateString = jobRunDateString;
        this.maskSensitiveData = maskSensitiveData;
    }

    public CemiPaymentElectionFileGroupTwoTabRowBo createCemiPaymentElectionFileGroupTwoTabRowBo() {
        Validate.validState(payeeAchAccount != null, "PayeeACHAccount cannot be null.");
        Validate.validState(employeeId != null, "employeeId cannot be null.");
        Validate.validState(achBankName != null, "achBankName cannot be null.");
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null.");

        final CemiGroupTwo groupTwo = new CemiGroupTwo(payeeAchAccount, employeeId, achBankName, maskSensitiveData);

        final CemiPaymentElectionFileGroupTwoTabRowBo groupTwoTabRow = new CemiPaymentElectionFileGroupTwoTabRowBo();

        // jobRunRowIndex and jobRunDateString are set by CemiOrmDataBuilderBase when storeSheetRow is invoked.
        groupTwoTabRow.setAchAccountGeneratedId(payeeAchAccount.getAchAccountGeneratedIdentifier());
        groupTwoTabRow.setEmployeeId(groupTwo.getEmployeeId());
        groupTwoTabRow.setPaymentElectionGroupRule_2(groupTwo.getPaymentElectionGroupRule_2());
        groupTwoTabRow.setPaymentElectionRule_2_1(groupTwo.getPaymentElectionRule_2_1());
        groupTwoTabRow.setElectionCountry_2_1(groupTwo.getElectionCountry_2_1());
        groupTwoTabRow.setElectionCurrency_2_1(groupTwo.getElectionCurrency_2_1());
        groupTwoTabRow.setPaymentType_2_1(groupTwo.getPaymentType_2_1());
        groupTwoTabRow.setAccountCountry_2_1(groupTwo.getAccountCountry_2_1());
        groupTwoTabRow.setAccountCurrency_2_1(groupTwo.getAccountCurrency_2_1());
        groupTwoTabRow.setBankAccountNickname_2_1(groupTwo.getBankAccountNickname_2_1());
        groupTwoTabRow.setBankAccountName_2_1(groupTwo.getBankAccountName_2_1());
        groupTwoTabRow.setAccountNumber_2_1(groupTwo.getAccountNumber_2_1());
        groupTwoTabRow.setAccountType_2_1(groupTwo.getAccountType_2_1());
        groupTwoTabRow.setBankName_2_1(groupTwo.getBankName_2_1());
        groupTwoTabRow.setBankRoutingNumber_2_1(groupTwo.getBankRoutingNumber_2_1());
        groupTwoTabRow.setIban_2_1(groupTwo.getIban_2_1());
        groupTwoTabRow.setBic_2_1(groupTwo.getBic_2_1());
        groupTwoTabRow.setBranchName_2_1(groupTwo.getBranchName_2_1());
        groupTwoTabRow.setBranchIdNumber_2_1(groupTwo.getBranchIdNumber_2_1());
        groupTwoTabRow.setCheckDigit_2_1(groupTwo.getCheckDigit_2_1());
        groupTwoTabRow.setDistributionAmount_2_1(groupTwo.getDistributionAmount_2_1());
        groupTwoTabRow.setDistributionPercentage_2_1(groupTwo.getDistributionPercentage_2_1());
        groupTwoTabRow.setDistributionBalance_2_1(groupTwo.getDistributionBalance_2_1());

        return groupTwoTabRow;
    }

}
