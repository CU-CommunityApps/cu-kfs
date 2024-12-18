package edu.cornell.kfs.pdp;

public final class CUPdpParameterConstants {

    public static final String WARNING_INACTIVE_VENDOR_TO_EMAIL_ADDRESSES = "WARNING_INACTIVE_VENDOR_TO_EMAIL_ADDRESSES";

    public static final String ACH_PERSONAL_CHECKING_TRANSACTION_CODE = "ACH_PERSONAL_CHECKING_TRANSACTION_CODE";
    public static final String ACH_PERSONAL_SAVINGS_TRANSACTION_CODE = "ACH_PERSONAL_SAVINGS_TRANSACTION_CODE";
    public static final String ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE = "ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE";
    public static final String GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT = "GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT";
    public static final String NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT = "NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT";
    public static final String NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY = "NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY";
    public static final String UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT = "UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT";
    public static final String UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY = "UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY";
    public static final String PDP_ACH_INVALID_EMAIL_ERROR_REPORT_TO_ADDRESSES = "PDP_ACH_INVALID_EMAIL_ERROR_REPORT_TO_ADDRESSES";
    public static final String MAX_ACH_ACCT_EXTRACT_RETRY = "MAX_ACH_ACCT_EXTRACT_RETRY";

    public static final String CU_ISO20022_CHECK_FORMS_CODE = "CU_ISO20022_CHECK_FORMS_CODE";

    public static final class CuPayeeAddressService {
        public static final String CU_PAYEE_ADDRESS_SERVICE_COMPONENT = "CuPayeeAddressService";
        public static final String PAYER_NAME_PARAMETER = "PAYER_NAME";
        public static final String PAYER_ADDRESS_LINE1_PARAMETER = "PAYER_ADDRESS_LINE1";
        public static final String PAYER_ADDRESS_LINE2_PARAMETER = "PAYER_ADDRESS_LINE2";
        public static final String PAYER_CITY_PARAMETER = "PAYER_CITY";
        public static final String PAYER_STATE_PARAMETER = "PAYER_STATE";
        public static final String PAYER_ZIP_CODE_PARAMETER = "PAYER_ZIP_CODE";
    }

    private CUPdpParameterConstants() {
        throw new UnsupportedOperationException("Do not call");
    }

}
