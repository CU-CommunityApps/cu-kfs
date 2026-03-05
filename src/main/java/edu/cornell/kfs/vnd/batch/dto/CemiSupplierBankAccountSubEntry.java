package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;

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

    private CemiSupplierBankAccountSubEntry() {
        this.vendorAccount = null;
        this.supplierId = CemiVendorConstants.EMPTY_STRING;
        this.bankName = CemiVendorConstants.EMPTY_STRING;
        this.bankRoutingNumber = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountNumber = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountName = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountId = CemiVendorConstants.EMPTY_STRING;
        this.bankAccountType = CemiVendorConstants.EMPTY_STRING;
    }

    public CemiSupplierBankAccountSubEntry(final PayeeACHAccount vendorAccount, final String supplierId,
            final int accountIndex) {
        this.vendorAccount = vendorAccount;
        this.supplierId = supplierId;
        this.bankName = vendorAccount.getBankRouting().getBankName();
        this.bankRoutingNumber = vendorAccount.getBankRoutingNumber();
        this.bankAccountNumber = determineBankAccountNumber(vendorAccount);
        this.bankAccountName = determineBankAccountName(bankName, bankAccountNumber);
        this.bankAccountId = determineBankAccountId(supplierId, bankAccountName, accountIndex);
        this.bankAccountType = determineBankAccountType(vendorAccount);
    }

    private static String determineBankAccountNumber(final PayeeACHAccount vendorAccount) {
        boolean cemiEnv = false; //TODO: determine if job is running in CEMI environment
        boolean maskCEMISensitiveValues = true; // TODO: determine if masking sensitive values for CEMI data conversion is on or off
        if (cemiEnv && !maskCEMISensitiveValues) {
            //return vendorAccount.getBankAccountNumber();
        }
        return CemiVendorConstants.DUMMY_ACCOUNT_NUMBER;
    }

    private static String determineBankAccountName(final String bankName, final String bankAccountNumber) {
        final String lastFourDigitsOfBankAccountNumber = StringUtils.right(bankAccountNumber, 4);
        return StringUtils.join(bankName, KFSConstants.BLANK_SPACE, lastFourDigitsOfBankAccountNumber);
    }

    private static String determineBankAccountId(final String supplierId, final String bankAccountName,
            final int accountIndex) {
        return MessageFormat.format(CemiVendorConstants.BANK_ACCOUNT_ID_FORMAT,
                supplierId, bankAccountName, Integer.toString(accountIndex));
    }

    private static String determineBankAccountType(final PayeeACHAccount vendorAccount) {
        final String kfsAccountType = StringUtils.defaultString(vendorAccount.getBankAccountTypeCode());
        final String cemiAccountType = CemiVendorConstants.BANK_ACCOUNT_TYPES.get(kfsAccountType);
        if (StringUtils.isBlank(cemiAccountType)) {
            LOG.warn("determineBankAccountType, Payee ACH Account with ID {} for Vendor {} had a missing "
                    + " or unrecognized account type; defaulting to Checking account type",
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

}
