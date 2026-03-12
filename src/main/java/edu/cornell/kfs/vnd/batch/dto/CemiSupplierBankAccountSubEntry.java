package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierBankAccountSubEntry {

    private static final Logger LOG = LogManager.getLogger();

    public static final CemiSupplierBankAccountSubEntry EMPTY = new CemiSupplierBankAccountSubEntry();

    private final PayeeACHAccount vendorAccount;
    private final String supplierId;
    private final String bankName;
    private final String bankRoutingNumber;
    private final String bankAccountNumber;
    private final String bankAccountName;
    private final String bankAccountId;
    private final String bankAccountType;
    private final String branchId;
    private final String branchName;
    private final List<String> acceptedPaymentTypes;
    private final List<String> paymentTypes;

    private CemiSupplierBankAccountSubEntry() {
        this.vendorAccount = null;
        this.supplierId = CemiVendorConstants.EMPTY_STRING;
        this.bankName = CemiVendorConstants.EMPTY_STRING;
        this.bankRoutingNumber = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountNumber = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountName = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountId = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountType = CemiVendorConstants.EMPTY_STRING;
        this.branchId = CemiVendorConstants.EMPTY_STRING;
        this.branchName = CemiVendorConstants.EMPTY_STRING;
        this.acceptedPaymentTypes = Collections.nCopies(3, CemiVendorConstants.EMPTY_STRING);
        this.paymentTypes = Collections.nCopies(3, CemiVendorConstants.EMPTY_STRING);
    }

    public CemiSupplierBankAccountSubEntry(final PayeeACHAccount vendorAccount, final String supplierId,
            final int accountIndex, final boolean maskSensitiveData) {
        this.vendorAccount = vendorAccount;
        this.supplierId = supplierId;
        this.bankName = vendorAccount.getBankRouting().getBankName();
        this.bankRoutingNumber = vendorAccount.getBankRoutingNumber();
        this.bankAccountNumber = determineBankAccountNumber(vendorAccount, maskSensitiveData);
        this.bankAccountName = determineBankAccountName(bankName, bankAccountNumber);
        this.bankAccountId = determineBankAccountId(supplierId, (vendorAccount.getAchAccountGeneratedIdentifier()).toString(), accountIndex);
        this.bankAccountType = determineBankAccountType(vendorAccount);
        this.branchId = CemiVendorConstants.EMPTY_STRING;
        this.branchName = CemiVendorConstants.EMPTY_STRING;
        this.acceptedPaymentTypes = Collections.nCopies(3, CemiVendorConstants.EMPTY_STRING);
        this.paymentTypes = Collections.nCopies(3, CemiVendorConstants.EMPTY_STRING);
    }

    private static String determineBankAccountNumber(final PayeeACHAccount vendorAccount, final boolean maskSensitiveData) {
        if (!maskSensitiveData) {
            return vendorAccount.getBankAccountNumber();
        }
        return CemiVendorConstants.DUMMY_ACCOUNT_NUMBER;
    }

    private static String determineBankAccountName(final String bankName, final String bankAccountNumber) {
        final String lastFourDigitsOfBankAccountNumber = StringUtils.right(bankAccountNumber, 4);
        return StringUtils.join(bankName, KFSConstants.BLANK_SPACE, lastFourDigitsOfBankAccountNumber);
    }

    private static String determineBankAccountId(final String supplierId, final String accountSystemGeneratedIdentifier,
            final int accountIndex) {
        return MessageFormat.format(CemiVendorConstants.BANK_ACCOUNT_ID_FORMAT,
                supplierId, 
                accountSystemGeneratedIdentifier, 
                Integer.toString(accountIndex));
    }

    private static String determineBankAccountType(final PayeeACHAccount vendorAccount) {
        final String kfsAccountType = StringUtils.defaultString(vendorAccount.getBankAccountTypeCode());
        final String cemiAccountType = CemiVendorConstants.BANK_ACCOUNT_TYPES.get(kfsAccountType);
        if (StringUtils.isBlank(cemiAccountType)) {
            LOG.warn("determineBankAccountType, Payee ACH Account with ID {} for Vendor {} had a missing "
                    + "or unrecognized account type; defaulting to Checking account type",
                    vendorAccount.getAchAccountGeneratedIdentifier(), vendorAccount.getPayeeIdNumber());
            return CemiVendorConstants.CHECKING_ACCOUNT_TYPE;
        }
        return cemiAccountType;
    }

    public PayeeACHAccount getVendorAccount() {
        return vendorAccount;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public List<String> getAcceptedPaymentTypes() {
        return acceptedPaymentTypes;
    }

    public List<String> getPaymentTypes() {
        return paymentTypes;
    }

}
