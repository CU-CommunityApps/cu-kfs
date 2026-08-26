package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBankAccountBo;

public class CemiSupplierBankAccountBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private Optional<PayeeACHAccount> vendorAccount;
    private String supplierId;
    private int accountIndex;
    private boolean maskSensitiveData;

    public CemiSupplierBankAccountBoFactory(final Optional<PayeeACHAccount> vendorAccount, final String supplierId,
            final int accountIndex, final boolean maskSensitiveData) {
        Validate.notNull(vendorAccount, "vendorAccount wrapper object cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(supplierId) || vendorAccount.isEmpty(),
                "supplierId cannot be blank if a non-empty vendorAccount wrapper was specified");
        Validate.isTrue(vendorAccount.isEmpty() == (accountIndex <= 0),
                "accountIndex must be a positive value if, and only if, a non-empty vendorAccount wrapper was specified");
        this.vendorAccount = vendorAccount;
        this.supplierId = supplierId;
        this.accountIndex = accountIndex;
        this.maskSensitiveData = maskSensitiveData;
    }

    public static CemiSupplierBankAccountBo createBankAccountBoFrom(final Optional<PayeeACHAccount> vendorAccount,
            final String supplierId, final int accountIndex, final boolean maskSensitiveData) {
        final CemiSupplierBankAccountBoFactory factory = new CemiSupplierBankAccountBoFactory(
                vendorAccount, supplierId, accountIndex, maskSensitiveData);
        return factory.createCemiSupplierBankAccountBo();
    }

    public CemiSupplierBankAccountBo createCemiSupplierBankAccountBo() {
        final CemiSupplierBankAccountBo bankAccountBo = new CemiSupplierBankAccountBo();

        final String bankName = determineBankName();
        final String bankAccountNumber = determineBankAccountNumber();
        final String bankAccountNickname = determineBankAccountNickname(bankName, bankAccountNumber);

        bankAccountBo.setSettlementBankAccountId(determineSettlementBankAccountId());
        bankAccountBo.setBankAccountNickname(bankAccountNickname);
        bankAccountBo.setBankAccountType(determineBankAccountType());
        bankAccountBo.setBankName(bankName);
        bankAccountBo.setRoutingTransitNumber(determineRoutingTransitNumber());
        bankAccountBo.setBranchId(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setBranchName(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setBankAccountNumber(bankAccountNumber);
        bankAccountBo.setAcceptsPaymentTypes1(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setAcceptsPaymentTypes2(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setAcceptsPaymentTypes3(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setPaymentTypes1(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setPaymentTypes2(CemiBaseConstants.EMPTY_STRING);
        bankAccountBo.setPaymentTypes3(CemiBaseConstants.EMPTY_STRING);

        return bankAccountBo;
    }

    private String determineBankName() {
        return vendorAccount.map(PayeeACHAccount::getBankRouting)
                .map(ACHBank::getBankName)
                .orElse(CemiBaseConstants.EMPTY_STRING);
    }

    private String determineBankAccountNumber() {
        return vendorAccount.map(PayeeACHAccount::getBankAccountNumber)
                .map(this::maskBankAccountNumberIfNecessary)
                .orElse(CemiBaseConstants.EMPTY_STRING);
    }

    private String maskBankAccountNumberIfNecessary(final String bankAccountNumber) {
        return maskSensitiveData ? CemiSupplierConstants.DUMMY_ACCOUNT_NUMBER : bankAccountNumber;
    }

    private String determineBankAccountNickname(final String bankName, final String bankAccountNumber) {
        if (vendorAccount.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final String lastFourDigitsOfBankAccountNumber = StringUtils.right(bankAccountNumber, 4);
        return StringUtils.join(bankName, KFSConstants.BLANK_SPACE, lastFourDigitsOfBankAccountNumber);
    }

    private String determineSettlementBankAccountId() {
        if (vendorAccount.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final String accountSystemGeneratedIdentifier = vendorAccount.get().getAchAccountGeneratedIdentifier().toString();
        return MessageFormat.format(CemiSupplierConstants.BANK_ACCOUNT_ID_FORMAT,
                supplierId, 
                accountSystemGeneratedIdentifier, 
                Integer.toString(accountIndex));
    }

    private String determineBankAccountType() {
        if (vendorAccount.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PayeeACHAccount payeeAccount = vendorAccount.get();
        final String kfsAccountType = StringUtils.defaultString(payeeAccount.getBankAccountTypeCode());
        final String cemiAccountType = CemiSupplierConstants.BANK_ACCOUNT_TYPES.get(kfsAccountType);
        if (StringUtils.isBlank(cemiAccountType)) {
            LOG.warn("determineBankAccountType, Payee ACH Account with ID {} for Vendor {} had a missing "
                    + "or unrecognized account type; defaulting to Checking account type",
                    payeeAccount.getAchAccountGeneratedIdentifier(), payeeAccount.getPayeeIdNumber());
            return CemiSupplierConstants.CHECKING_ACCOUNT_TYPE;
        }
        return cemiAccountType;
    }

    private String determineRoutingTransitNumber() {
        return vendorAccount.map(PayeeACHAccount::getBankRoutingNumber)
                .orElse(CemiBaseConstants.EMPTY_STRING);
    }

}
