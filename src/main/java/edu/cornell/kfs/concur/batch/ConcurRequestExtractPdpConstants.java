package edu.cornell.kfs.concur.batch;

public class ConcurRequestExtractPdpConstants {

    public static class ValidationConstants {
        public static final String CASH_ADVANCE_INDICATOR = "CASH ADVANCE";
        public static final String EMPLOYEE_INDICATOR = "EMPLOYEE";
        public static final String NON_EMPLOYEE_INDICATOR = "NON-EMPLOYEE";
    }

    public static class ValidationErrorMessages {
        public static final String NOT_CASH_ADVANCE_DATA_LINE = "Request Extract Detail Line is not a Cash Advance. ";
        public static final String CASH_ADVANCE_USED_IN_EXPENSE_REPORT = "Cash Advance has been used in expense report.";
        public static final String REQUEST_ID_INVALID = "Request ID was detected as being NULL or blank.";
        public static final String EMPLOYEE_ID_NULL_OR_BLANK = "Employee ID was detected as being NULL or blank.";
        public static final String EMPLOYEE_ID_NOT_FOUND_IN_KFS = "Person for provided Employee ID could not be found in KFS.";
        public static final String PAYEE_ID_TYPE_INVALID = "Payee ID type was not specified as EMPLOYEE or NON-EMPOYEE.";
        public static final String REQUEST_AMOUNT_INVALID = "Requested cash advance amount was detected as being NULL or blank.";
        public static final String DUPLICATE_CASH_ADVANCE_DETECTED = "Duplicate cash advance request detected.";
    }
    
    public static final String WHITESPACE = "    ";
}
