package edu.cornell.kfs.pdp;

public class CUPdpTestConstants {

    public static final String DIRECT_DEPOSIT_TYPE = "PRAP";
    public static final String PERSONAL_CHECKING_CODE = "22PPD";
    public static final String PERSONAL_SAVINGS_CODE = "32PPD";
    public static final String GENERATED_PAAT_NOTE_TEXT = "Created from Workday ACH data extract.";
    public static final String NEW_ACCOUNT_EMAIL_SUBJECT = "New ACH Account in KFS.";
    public static final String NEW_ACCOUNT_EMAIL_BODY = "Payment for [payeeIdentifierTypeCode] of [payeeIdNumber]"
            + " will go to [bankAccountTypeCode] account at [bankRouting.bankName].";
    public static final String UPDATED_ACCOUNT_EMAIL_SUBJECT = "Update ACH Account in KFS.";
    public static final String UPDATED_ACCOUNT_EMAIL_BODY = "Update payment for [payeeIdentifierTypeCode] of [payeeIdNumber]"
            + " will go to [bankAccountTypeCode] account at [bankRouting.bankName] from now on.";
    public static final String ACH_EMAIL_FROM_ADDRESS = "test-from-address@someplace.edu";

}
