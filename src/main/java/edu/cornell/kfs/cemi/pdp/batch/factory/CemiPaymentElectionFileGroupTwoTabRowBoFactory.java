package edu.cornell.kfs.cemi.pdp.batch.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionConstants;
import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionFileGroupTwoTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiPaymentElectionFileGroupTwoTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private PayeeACHAccount payeeAchAccount;
    private String employeeId;
    private String achBankName;
    private boolean maskSensitiveData = true;

    public CemiPaymentElectionFileGroupTwoTabRowBoFactory(final PayeeACHAccount payeeAchAccount,
            final String employeeId, final String achBankName, final boolean maskSensitiveData) {
        this.payeeAchAccount = payeeAchAccount;
        this.employeeId = employeeId;
        this.achBankName = achBankName;
        this.maskSensitiveData = maskSensitiveData;
    }

    public CemiPaymentElectionFileGroupTwoTabRowBo createCemiPaymentElectionFileGroupTwoTabRowBo() {
        Validate.validState(payeeAchAccount != null, "PayeeACHAccount cannot be null.");
        Validate.validState(employeeId != null, "employeeId cannot be null.");
        Validate.validState(achBankName != null, "achBankName cannot be null.");

        final CemiPaymentElectionFileGroupTwoTabRowBo groupTwoTabRow = new CemiPaymentElectionFileGroupTwoTabRowBo();

        groupTwoTabRow.setAchAccountGeneratedId(payeeAchAccount.getAchAccountGeneratedIdentifier());
        groupTwoTabRow.setEmployeeId(employeeId);
        groupTwoTabRow.setPaymentElectionGroupRule_2(CemiPaymentElectionConstants.EXPENSE_PAYMENTS);
        groupTwoTabRow.setPaymentElectionRule_2_1(CemiPaymentElectionConstants.EXPENSE_PAYMENTS);
        groupTwoTabRow.setElectionCountry_2_1(CemiPaymentElectionConstants.US);
        groupTwoTabRow.setElectionCurrency_2_1(CemiPaymentElectionConstants.USD);
        groupTwoTabRow.setPaymentType_2_1(CemiPaymentElectionConstants.DIRECT_DEPOSIT);
        groupTwoTabRow.setAccountCountry_2_1(CemiPaymentElectionConstants.US);
        groupTwoTabRow.setAccountCurrency_2_1(CemiPaymentElectionConstants.USD);
        groupTwoTabRow.setBankAccountNickname_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setBankAccountName_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setAccountNumber_2_1(determineBankAccountNumber());
        groupTwoTabRow.setAccountType_2_1(determineBankAccountType());
        groupTwoTabRow.setBankName_2_1(achBankName);
        groupTwoTabRow.setBankRoutingNumber_2_1(determineBankRoutingNumber());
        groupTwoTabRow.setIban_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setBic_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setBranchName_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setBranchIdNumber_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setCheckDigit_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setDistributionAmount_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setDistributionPercentage_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabRow.setDistributionBalance_2_1(CemiPaymentElectionConstants.TRUE);

        return groupTwoTabRow;
    }

    private String determineBankAccountNumber() {
        if (!maskSensitiveData) {
            return payeeAchAccount.getBankAccountNumber();
        }
        return CemiPaymentElectionConstants.DUMMY_ACCOUNT_NUMBER;
    }

    private String determineBankAccountType() {
        final String kfsAccountType = StringUtils.defaultString(payeeAchAccount.getBankAccountTypeCode());
        final String cemiAccountType = CemiPaymentElectionConstants.KfsToWorkdayBankAccountTypeCodeConverter.get(kfsAccountType);
        if (StringUtils.isBlank(cemiAccountType)) {
            LOG.warn("determineBankAccountType, Payee Generated Account ID {} for Payee {} had a missing "
                    + "or unrecognized account type; defaulting to Checking account type",
                    payeeAchAccount.getAchAccountGeneratedIdentifier(), payeeAchAccount.getPayeeIdNumber());
            return CemiPaymentElectionConstants.WORKDAY_CHECKING_ACCOUNT_TYPE;
        }
        return cemiAccountType;
    }

    private String determineBankRoutingNumber() {
        return payeeAchAccount.getBankRoutingNumber();
    }

}
