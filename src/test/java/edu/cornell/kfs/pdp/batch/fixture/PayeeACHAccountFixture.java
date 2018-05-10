package edu.cornell.kfs.pdp.batch.fixture;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.rice.core.api.util.type.KualiInteger;

import edu.cornell.kfs.pdp.CUPdpTestConstants;

public enum PayeeACHAccountFixture {
    JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW(ACHPersonPayeeFixture.JOHN_DOE, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "44333222111"),
    JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW(ACHPersonPayeeFixture.JOHN_DOE, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "44333222111"),

    JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD(ACHPersonPayeeFixture.JANE_DOE, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "66555444300"),
    JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW(
            JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD, ACHPersonPayeeFixture.JANE_DOE, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "66555444333", true),
    JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW(ACHPersonPayeeFixture.JANE_DOE, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "66555444333"),
    JANE_DOE_SAVINGS_ACCOUNT_ENTITY_ALT_NEW(ACHPersonPayeeFixture.JANE_DOE, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "66555444300"),

    ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW(ACHPersonPayeeFixture.ROBERT_SMITH, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "12345554321"),
    ROBERT_SMITH_CHECKING_ACCOUNT_EMPLOYEE_ALT_NEW(ACHPersonPayeeFixture.ROBERT_SMITH, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "12345554321"),
    ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD(ACHPersonPayeeFixture.ROBERT_SMITH, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "12345554321", false),
    ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW(
            ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD, ACHPersonPayeeFixture.ROBERT_SMITH, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_SAVINGS_CODE, "12345554321", true),

    MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD(ACHPersonPayeeFixture.MARY_SMITH, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "99887766789", false),
    MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW(
            MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, ACHPersonPayeeFixture.MARY_SMITH, PayeeIdTypeCodes.EMPLOYEE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "99887766789", true),
    MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD(ACHPersonPayeeFixture.MARY_SMITH, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.FIRST_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "99887766789"),
    MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW(
            MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD, ACHPersonPayeeFixture.MARY_SMITH, PayeeIdTypeCodes.ENTITY, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE,
            ACHBankFixture.SECOND_BANK, CUPdpTestConstants.PERSONAL_CHECKING_CODE, "99887766789", true);

    public static final int BASE_ID = 300;

    public final PayeeACHAccountFixture oldAccountFixture;
    public final ACHPersonPayeeFixture payeeFixture;
    public final String payeeIdentifierTypeCode;
    public final String achTransactionType;
    public final ACHBankFixture bank;
    public final String bankAccountTypeCode;
    public final String bankAccountNumber;
    public final boolean active;

    private PayeeACHAccountFixture(ACHPersonPayeeFixture payeeFixture, String payeeIdentifierTypeCode, String achTransactionType,
            ACHBankFixture bank, String bankAccountTypeCode, String bankAccountNumber) {
        this(null, payeeFixture, payeeIdentifierTypeCode, achTransactionType, bank, bankAccountTypeCode, bankAccountNumber, true);
    }

    private PayeeACHAccountFixture(ACHPersonPayeeFixture payeeFixture, String payeeIdentifierTypeCode, String achTransactionType,
            ACHBankFixture bank, String bankAccountTypeCode, String bankAccountNumber, boolean active) {
        this(null, payeeFixture, payeeIdentifierTypeCode, achTransactionType, bank, bankAccountTypeCode, bankAccountNumber, active);
    }

    private PayeeACHAccountFixture(PayeeACHAccountFixture oldAccountFixture, ACHPersonPayeeFixture payeeFixture,
            String payeeIdentifierTypeCode, String achTransactionType, ACHBankFixture bank, String bankAccountTypeCode,
            String bankAccountNumber, boolean active) {
        this.oldAccountFixture = oldAccountFixture;
        this.payeeFixture = payeeFixture;
        this.payeeIdentifierTypeCode = payeeIdentifierTypeCode;
        this.achTransactionType = achTransactionType;
        this.bank = bank;
        this.bankAccountTypeCode = bankAccountTypeCode;
        this.bankAccountNumber = bankAccountNumber;
        this.active = active;
    }

    @SuppressWarnings("deprecation")
    public PayeeACHAccount toPayeeACHAccount() {
        PayeeACHAccount achAccount = new PayeeACHAccount();
        achAccount.setAchAccountGeneratedIdentifier(getOrdinalBasedAccountGeneratedId());
        achAccount.setPayeeIdentifierTypeCode(payeeIdentifierTypeCode);
        achAccount.setPayeeIdNumber(getPayeeIdNumber());
        achAccount.setAchTransactionType(achTransactionType);
        achAccount.setBankRoutingNumber(bank.bankRoutingNumber);
        achAccount.setBankRouting(bank.toACHBank());
        achAccount.setBankAccountTypeCode(bankAccountTypeCode);
        achAccount.setBankAccountNumber(bankAccountNumber);
        achAccount.setActive(active);
        return achAccount;
    }

    public KualiInteger getOrdinalBasedAccountGeneratedId() {
        int ordinalToUse = (oldAccountFixture != null) ? oldAccountFixture.ordinal() : ordinal();
        return new KualiInteger(BASE_ID + ordinalToUse);
    }

    public String getPayeeIdNumber() {
        if (StringUtils.equals(PayeeIdTypeCodes.EMPLOYEE, payeeIdentifierTypeCode)) {
            return payeeFixture.employeeId;
        } else if (StringUtils.equals(PayeeIdTypeCodes.ENTITY, payeeIdentifierTypeCode)) {
            return payeeFixture.entityId;
        } else {
            throw new IllegalStateException("Payee has unrecognized person-related identifier type: " + payeeIdentifierTypeCode);
        }
    }

}
