package edu.cornell.kfs.cemi.pdp.batch.service.impl;

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
    private String achBankName;
    private String jobRunDateString;
    private boolean maskSensitiveData = true;

    public CemiPaymentElectionFileGroupTwoTabRowBoFactory(final PayeeACHAccount payeeAchAccount,
            final String achBankName, final String jobRunDateString, final boolean maskSensitiveData) {
        this.payeeAchAccount = payeeAchAccount;
        this.achBankName = achBankName;
        this.jobRunDateString = jobRunDateString;
        this.maskSensitiveData = maskSensitiveData;
    }

    public CemiPaymentElectionFileGroupTwoTabRowBo createCemiPaymentElectionFileGroupTwoTabRowBo() {
        Validate.validState(payeeAchAccount != null, "PayeeACHAccount cannot be null.");
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null.");

        final CemiPaymentElectionFileGroupTwoTabRowBo groupTwoTabDataRow = new CemiPaymentElectionFileGroupTwoTabRowBo();

        final String rowEmployeeId = determineEmployeeId();
        final String rowAccountNumber = determineBankAccountNumber();
        final String rowAccountType = determineBankAccountType();
        final String rowBankRoutingNumber = determineBankRoutingNumber();

        // Reference information related to business object being created that must be specified.
        // Both of these attributes are being set by abstract class CemiOrmDataBuilderBase when storeSheetRow is invoked.
        //      attribute jobRunRowIndex
        //      attribute jobRunDateString
        groupTwoTabDataRow.setAchAccountGeneratedIdentifierUsedForDataRow(payeeAchAccount.getAchAccountGeneratedIdentifier());

        //Format and assign data values for these attributes as defined by the Huron mapping template specification.
        groupTwoTabDataRow.setEmployeeId(rowEmployeeId);
        groupTwoTabDataRow.setPaymentElectionGroupRule_2(CemiPaymentElectionConstants.EXPENSE_PAYMENTS);
        groupTwoTabDataRow.setPaymentElectionRule_2_1(CemiPaymentElectionConstants.EXPENSE_PAYMENTS);
        groupTwoTabDataRow.setElectionCountry_2_1(CemiPaymentElectionConstants.US);
        groupTwoTabDataRow.setElectionCurrency_2_1(CemiPaymentElectionConstants.USD);
        groupTwoTabDataRow.setPaymentType_2_1(CemiPaymentElectionConstants.DIRECT_DEPOSIT);
        groupTwoTabDataRow.setAccountCountry_2_1(CemiPaymentElectionConstants.US);
        groupTwoTabDataRow.setAccountCurrency_2_1(CemiPaymentElectionConstants.USD);
        groupTwoTabDataRow.setBankAccountNickname_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setBankAccountName_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setAccountNumber_2_1(rowAccountNumber);
        groupTwoTabDataRow.setAccountType_2_1(rowAccountType);
        groupTwoTabDataRow.setBankName_2_1(achBankName);
        groupTwoTabDataRow.setBankRoutingNumber_2_1(rowBankRoutingNumber);
        groupTwoTabDataRow.setIban_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setBic_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setBranchName_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setBranchIdNumber_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setCheckDigit_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setDistributionAmount_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setDistributionPercentage_2_1(CemiBaseConstants.EMPTY_STRING);
        groupTwoTabDataRow.setDistributionBalance_2_1(CemiPaymentElectionConstants.TRUE);

        return groupTwoTabDataRow;
    }

    private String determineEmployeeId() {
        return payeeAchAccount.getPayeeIdNumber();
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
