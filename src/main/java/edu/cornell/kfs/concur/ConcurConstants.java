package edu.cornell.kfs.concur;

public class ConcurConstants {
    public static final String AUTHORIZATION_PROPERTY = "Authorization";
    public static final String BASIC_AUTHENTICATION_SCHEME = "Basic";
    public static final String OAUTH_AUTHENTICATION_SCHEME = "OAuth";

    public static final int VALIDATION_RESULT_MESSAGE_MAX_LENGTH = 2000;

    public static class AccountingStringFieldNames {
        public static final String CHART = "Chart";
        public static final String ACCOUNT_NUMBER = "Account Number";
        public static final String OBJECT_CODE = "Object Code";
        public static final String SUB_ACCOUNT_NUMBER = "Sub Account Number";
        public static final String SUB_OBJECT_CODE = "Sub Object Code";
        public static final String PROJECT_CODE = "Project Code";
    }

    public static final String REFRESH_TOKEN_URL_PARAM = "refresh_token";
    public static final String CLIENT_ID_URL_PARAM = "client_id";
    public static final String CLIENT_SECRET_URL_PARAM = "client_secret";

    public static final String CONCUR_ACCESS_TOKEN = "concur.access.token";
    public static final String CONCUR_REFRESH_TOKEN = "concur.refresh.token";
    public static final String CONCUR_CONSUMER_KEY = "concur.consumer.key";
    public static final String CONCUR_SECRET_KEY = "concur.secret.key";
    public static final String CONCUR_ACCESS_TOKEN_EXPIRATION_DATE = "concur.access.token.expiration.date";

    public static final String EXPENSE_REPORT_URI_INDICATOR = "/expense/expensereport/";
    public static final String TRAVEL_REQUEST_URI_INDICATOR = "/travelrequest/";

    public static final String EXPENSE_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE = "A_EXTV";
    public static final String REQUEST_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE = "Q_EXTV";

    public static final String APPROVE_ACTION = "Approve";
    public static final String SEND_BACK_TO_EMPLOYEE_ACTION = "Send Back To Employee";
    public static final String APPROVE_COMMENT = "Approved via Concur Connect";

    public static final String ERROR_MESSAGE_HEADER = "Please resubmit your Report.\n\nThe Report is being returned due to an error. Please make the necessary corrections.\n\nError:\n";
    
    public static final boolean EVENT_NOTIFICATION_IN_PROCESS = true;
    public static final boolean EVENT_NOTIFICATION_NOT_IN_PROCESS = false;
    public static final boolean EVENT_NOTIFICATION_PROCESSED = true;
    public static final boolean EVENT_NOTIFICATION_NOT_PROCESSED = false;

    public static final String FORWARD_SLASH = "/";

    public static final String PDP_XML_NAMESPACE = "http://www.kuali.org/kfs/pdp/payment";
    
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final int SOURCE_DOCUMENT_NUMBER_FIELD_SIZE = 14; 
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String CREDIT = "CR";
    public static final String DEBIT = "DR";
    public static final String PAYMENT_CODE_CASH = "CASH";
    public static final String PAYMENT_CODE_PRE_PAID_OR_OTHER = "COPD";
    public static final String PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID = "CBCN";
    public static final String PAYMENT_CODE_PSEUDO = "XXXX";
    public static final String PENDING_CLIENT = "Pending Client";
    public static final String EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "E";
    public static final String NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "Y";
    public static final String COMBINED_GROUP_INDICATOR = "Y";
    public static final String BANK_CODE = "DISB";
    public static final String FEED_FILE_ENTRY_HEADER_VERSION = "1.0";
    public static final String EMPLOYEE_STATUS_CODE = "EMPLOYEE";
    public static final String NON_EMPLOYEE_STATUS_CODE = "NON-EMPLOYEE";
    public static final String PDP_CONCUR_OUTPUT_FILE_NAME_PREFIX = "pdp_concur_";

    public static class StandardAccountingExtractReport {
        public static final String UNKNOWN_SAE_FILENAME = "UNKNOWN_SAE_FILENAME";
        public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "No records with validation errors.";
        public static final String NO_RECORDS_MISSING_OBJECT_CODES_MESSAGE = "No transactions with \"Pending Client\" as the object code.";
        public static final String END_OF_REPORT_MESSAGE = "End of report.";
        public static final String PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_SUMMARY_REPORT_SUB_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_SUMMARY_REPORT_SUB_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_REPORT_CONCUR_FILE_NAME_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "SAE_REPORT_CONCUR_FILE_NAME_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String REIMBURSEMENTS_IN_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "REIMBURSEMENTS_IN_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String CASH_ADVANCE_RELATED_TO_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "CASH_ADVANCE_RELATED_TO_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String EXPENSES_PAID_ON_CORPORATE_CARD_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "EXPENSES_PAID_ON_CORPORATE_CARD_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String TRANSACTIONS_BYPASSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "TRANSACTIONS_BYPASSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String PDP_RECORDS_PROCESSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "PDP_RECORDS_PROCESSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_VALIDATION_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_VALIDATION_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_MISSING_OBJECT_CODES_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_MISSING_OBJECT_CODES_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
    }

}
