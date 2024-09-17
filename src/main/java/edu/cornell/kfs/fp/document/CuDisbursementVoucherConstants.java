package edu.cornell.kfs.fp.document;

public class CuDisbursementVoucherConstants {

    // Prefixes defined for the DV extract process to identify notes 
    public static String DV_EXTRACT_NOTE_PREFIX_PREPARER = "Info: ";
    public static String DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_NAME = "Send Check To:";
    public static String DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS1 = "SH1:";
    public static String DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS2 = "SH2:";
    public static String DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3 = "SH3:";
    public static String DV_EXTRACT_SUB_UNIT_CODE = "DV";
    public static String DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER = "::";
    
    public static int DV_EXTRACT_MAX_NOTE_LINE_SIZE = 72;
    public static String DV_EXTRACT_EDOC_NUMBER_PREFIX_IDENTIFIER = "Doc:";
    
    // payee types
    public static final String DV_PAYEE_TYPE_STUDENT = "S";
    public static final String DV_PAYEE_TYPE_ALUMNI = "A";
    
    public static final String DV_PAYEE_ID_TYP_VENDOR = "Vendor ID";
    public static final String DV_PAYEE_ID_TYP_ENTITY = "Entity ID";
    public static final String DV_PAYEE_ID_TYP_EMPL = "Employee ID";
    
    public static class PayeeAffiliations {
        public static final String STUDENT = "STDNT";
        public static final String ALUMNI = "ALUMNI";
        public static final String FACULTY = "FCLTY";
        public static final String STAFF = "STAFF";
    }
    // system parameter parameter constants
    public static final String ALLOWED_EMPLOYEE_STATUSES_FOR_PAYMENT = "ALLOWED_EMPLOYEE_STATUSES_FOR_PAYMENT";
    
    public static class PaymentReasonCodes {
        public static final String ROYALTIES = "R";
        public static final String RENTAL_PAYMENT = "T";
        public static final String TRAVEL_HONORARIUM = "X";
    }
    
    public static final String PAYMENT_REASONS_THAT_DO_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR = "PAYMENT_REASONS_THAT_DO_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR";

}
