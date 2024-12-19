package edu.cornell.kfs.concur;

public class ConcurConstants {
    public static final String AUTHORIZATION_PROPERTY = "Authorization";
    public static final String CONSUMER_KEY_PROPERTY = "X-ConsumerKey";
    public static final String BASIC_AUTHENTICATION_SCHEME = "Basic";
    public static final String OAUTH_AUTHENTICATION_SCHEME = "OAuth";
    public static final String BEARER_AUTHENTICATION_SCHEME = "Bearer";

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
    public static final String TOKEN_URL_PARAM = "token";

    public static final String CONCUR_WEB_SERVICE_GROUP_CODE = "CNCR";
    public static final String CONCUR_LOGIN_USERNAME = "concur.login.username";
    public static final String CONCUR_LOGIN_PASSWORD = "concur.login.password";
    public static final String CONCUR_ACCESS_TOKEN = "concur.access.token";
    public static final String CONCUR_REFRESH_TOKEN = "concur.refresh.token";
    public static final String CONCUR_CONSUMER_KEY = "concur.consumer.key";
    public static final String CONCUR_SECRET_KEY = "concur.secret.key";
    public static final String CONCUR_ACCESS_TOKEN_EXPIRATION_DATE = "concur.access.token.expiration.date";

    public static final String USERNAME_PASSWORD_SEPARATOR = ":";
    public static final String TLS_V1_2_PROTOCOL = "TLSv1.2";

    public static final String EXPENSE_REPORT_URI_INDICATOR = "/expense/expensereport/";
    public static final String TRAVEL_REQUEST_URI_INDICATOR = "/travelrequest/";

    public static final String EXPENSE_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE = "A_EXTV";
    public static final String REQUEST_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE = "Q_EXTV";

    public static final String APPROVE_ACTION = "Approve";
    public static final String SEND_BACK_TO_EMPLOYEE_ACTION = "Send Back To Employee";
    public static final String APPROVE_COMMENT = "Approved via Concur Connect";

    public static final String ERROR_MESSAGE_STARTER = "Please resubmit your Report.\n\nThe Report is being returned due to an error. Please make the necessary corrections";
    public static final String ERROR_MESSAGE_HEADER = ERROR_MESSAGE_STARTER + "\n\nError:\n";
    public static final String DETAIL_MESSAGE_HEADER = APPROVE_COMMENT + ".  Below are the account, sub fund code, and higher ed function codes used in this report:\n";
    
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
    public static final String PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID = "CBCP";
    public static final String PAYMENT_CODE_PSEUDO = "XXXX";
    public static final String CASH_ADVANCE_PAYMENT_CODE_NAME_CASH = "Cash";
    public static final String CASH_ADVANCE_PAYMENT_CODE_NAME_UNIVERSITY_BILLED_OR_PAID = "University Billed/University Paid";
    public static final String EXPENSE_TYPE_ATM_FEE = "Corporate Card Fees";
    public static final String PENDING_CLIENT = "Pending Client";
    public static final String EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "E";
    public static final String NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "Y";
    public static final String COMBINED_GROUP_INDICATOR = "Y";
    public static final String BANK_CODE = "DISB";
    public static final String FEED_FILE_ENTRY_HEADER_VERSION = "1.0";
    public static final String EMPLOYEE_STATUS_CODE = "EMPLOYEE";
    public static final String NON_EMPLOYEE_STATUS_CODE = "NON-EMPLOYEE";
    public static final String PDP_CONCUR_CASH_ADVANCE_OUTPUT_FILE_NAME_PREFIX = "pdp_cncr_advnc_";
    public static final String PDP_CONCUR_TRIP_REIMBURSEMENT_OUTPUT_FILE_NAME_PREFIX = "pdp_cncr_reimb_";

    public static final String COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX = "gl_collector_concur_";
    public static final String COLLECTOR_HEADER_RECORD_TYPE = "HD";
    public static final String COLLECTOR_TRAILER_RECORD_TYPE = "TL";
    public static final String COLLECTOR_FILE_DATE_FORMAT = "yyyy-MM-dd";

    public static final String FILE_EXTENSION_DELIMITTER = ".";

    public static final String USER_PAYMENT_TYPE = "User";
    public static final String UNIVERSITY_PAYMENT_TYPE = "University";
    public static final String CORPORATE_CARD_PAYMENT_TYPE = "Cornell Corp Card";
    
    public static final String CONCUR_FAILED_EVENT_QUEUE_READONLY = "R";
    
    public static final String QUESTION_MARK_USER_EQUALS = "?user=";

    public static final String REQUEST_QUERY_CURRENT_DATE_INDICATOR = "CURRENT";
    public static final String REQUEST_QUERY_LAST_DATE_INDICATOR = "PREVIOUS";
    public static final String REQUEST_QUERY_START_DATE_FIELD = "startDate";
    public static final String REQUEST_QUERY_SORT_ORDER_DESC = "DESC";

    public static class StandardAccountingExtractReport {
        public static final String UNKNOWN_SAE_FILENAME = "UNKNOWN_SAE_FILENAME";
        public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "No records with validation errors.";
        public static final String NO_RECORDS_MISSING_OBJECT_CODES_MESSAGE = "No transactions with \"Pending Client\" as the object code.";
        public static final String NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT = "No validation errors to output.";
        public static final String NO_REMOVED_CHARACTERS_MESSAGE = "No tabs were replaced and no special characters or in-cell quotes were removed.";
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
        public static final String SAE_VALIDATION_SUB_REPORT_BYPASSED_XXXX_NOTE = "SAE_VALIDATION_SUB_REPORT_BYPASSED_XXXX_NOTE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_MISSING_OBJECT_CODES_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_MISSING_OBJECT_CODES_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String SAE_REMOVED_CHARACTERS_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "SAE_REMOVED_CHARACTERS_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
    }
    
    public static final String SAE_REQUESTED_CASH_ADVANCE_INDICATOR = "CASH";
    public static final String SAE_REQUESTED_CASH_ADVANCE_APPROVED_BY_CONCUR_ADMIN = "1";
    public static final String SAE_CASH_ADVANCE_BEING_APPLIED_TO_TRIP_REIMBURSEMENT = "2";

    public static final String REQUEST_EXTRACT_CASH_ADVANCE_INDICATOR = "CASH";
    public static final String SPACING_STRING_FOR_OUTPUT = "    ";

    public static class RequestExtractReport {
        public static final String UNKNOWN_REQUEST_EXTRACT_FILENAME = "UNKNOWN_REQUEST_EXTRACT_FILENAME";
        public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "No records with validation errors.";
        public static final String NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT = "No validation errors to output.";
        public static final String END_OF_REPORT_MESSAGE = "End of report.";
        public static final String PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String REQUEST_EXTRACT_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "REQUEST_EXTRACT_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String REQUEST_EXTRACT_SUMMARY_REPORT_SUB_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "REQUEST_EXTRACT_SUMMARY_REPORT_SUB_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String REQUEST_EXTRACT_REPORT_CONCUR_FILE_NAME_LABEL_NOT_SET_IN_CONFIGURATION_FILE = "REQUEST_EXTRACT_REPORT_CONCUR_FILE_NAME_LABEL_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String REQUEST_EXTRACT_VALIDATION_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE = "REQUEST_EXTRACT_VALIDATION_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE";
    }
    
    public static class SaeRequestedCashAdvancesExtractReport {
        public static final String UNKNOWN_SAE_FILENAME = "UNKNOWN_SAE_FILENAME";
        public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "No records with validation errors.";
        public static final String NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT = "No validation errors to output.";
        public static final String END_OF_REPORT_MESSAGE = "End of report.";
        public static final String PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE";
        public static final String PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE = "PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE";
    }
    
    public static final String SAE_REQUEST_ID_PREFIX = "SAE";

    public static class ConcurXmlNamespaces {
        public static final String NOTIFICATION = "http://www.concursolutions.com/api/notification/2012/06";
        public static final String TRAVEL_REQUEST = "http://www.concursolutions.com/api/travelrequest/2012/06";
    }
    
    public static final class ConcurOAuth2 {
        public static final class WebServiceCredentialKeys {
            public static final String GROUP_CODE = "CNCROAUTH2";
            public static final String CLIENT_ID = "concur.client_id";
            public static final String SECRET_ID = "concur.secret_id";
            public static final String USER_NAME = "concur.user_name";
            public static final String REQUEST_TOKEN = "concur.request_token";
            public static final String REFRESH_TOKEN = "concur.refresh_token";
        }
        public static final class FormFieldKeys {
            public static final String CLIENT_ID = "client_id";
            public static final String CLIENT_SECRET = "client_secret";
            public static final String REFRESH_TOKEN = "refresh_token";
            public static final String USER_NAME = "username";
            public static final String GRANT_TYPE = "grant_type";
            public static final String CREDTYPE = "credtype";
            public static final String PASSWORD = "password";
        }
        public static final String GRANT_TYPE_REFRESH_TOKEN_VALUE = "refresh_token";
        public static final String GRANT_TYPE_PASSWORD_VALUE = "password";
        public static final String CRED_TYPE_AUTHTOKEN_VALUE = "authtoken";
        public static final String REQUEST_HEADER_CONTENT_TYPE_KEY_NAME = "Content-Type";
    }
    
    public enum ConcurEventNotificationType {
        ExpenseReport("Expense Report", "Expense Report ID", "Expense Report Name", "Expense Report Status", true),
        TravelRequest("Travel Request", "Request ID", "Request Name", "Request Status", false);
        
        public final String eventType;
        public final String reportNumberDescription;
        public final String reportNameDescription;
        public final String reportStatusDescription;
        public final boolean displayTravelerEmail;

        private ConcurEventNotificationType(String eventType, String reportNumberDescription, String reportNameDescription,
                                            String reportStatusDescription, boolean displayTravelerEmail) {
            this.eventType = eventType;
            this.reportNumberDescription = reportNumberDescription;
            this.reportNameDescription = reportNameDescription;
            this.reportStatusDescription = reportStatusDescription;
            this.displayTravelerEmail = displayTravelerEmail;
        }
    }
    
    public enum ConcurEventNotificationStatus {
        validAccounts("valid", true, "Valid Accounts"),
        invalidAccounts("invalid", false, "Invalid Accounts"),
        processingError("processing error", false, "Processing Errors");
        
        public final String status;
        public final boolean valid;
        public final String statusForReport;
        
        private ConcurEventNotificationStatus(String status, boolean valid, String statusForReport) {
            this.status = status;
            this.valid = valid;
            this.statusForReport = statusForReport;
        }
    }

    public static final class ConcurWorkflowActions {
        public static final String APPROVE = "approve";
        public static final String SEND_BACK = "sendBack";
    }

    public static final class ConcurApiParameters {
        public static final String VIEW = "view";
        public static final String START = "start";
        public static final String LIMIT = "limit";
        public static final String MODIFIED_AFTER = "modifiedAfter";
        public static final String MODIFIED_BEFORE = "modifiedBefore";
        public static final String SORT_FIELD = "sortField";
        public static final String SORT_ORDER = "sortOrder";
    }

    public static final class ConcurApiOperations {
        public static final String FIRST = "first";
        public static final String PREV = "prev";
        public static final String NEXT = "next";
        public static final String LAST = "last";
    }

    public static final class RequestV4Views {
        public static final String SUBMITTED = "SUBMITTED";
    }

    public static final class RequestV4StatusCodes {
        public static final String NOT_SUBMITTED = "NOT_SUBMITTED";
        public static final String SUBMITTED = "SUBMITTED";
        public static final String APPROVED = "APPROVED";
        public static final String CANCELED = "CANCELED";
        public static final String SENTBACK = "SENTBACK";
    }

    public enum RequestV4Status {
        NOT_SUBMITTED(RequestV4StatusCodes.NOT_SUBMITTED, "Not Submitted"),
        SUBMITTED_AND_PENDING_APPROVAL(RequestV4StatusCodes.SUBMITTED, "Submitted & Pending Approval"),
        PENDING_COST_OBJECT_APPROVAL(RequestV4StatusCodes.SUBMITTED, "Pending Cost Object Approval"),
        PENDING_EXTERNAL_VALIDATION(RequestV4StatusCodes.SUBMITTED, "Pending External Validation"),
        APPROVED(RequestV4StatusCodes.APPROVED, "Approved"),
        CANCELED(RequestV4StatusCodes.CANCELED, "Canceled"),
        SENTBACK(RequestV4StatusCodes.SENTBACK, "Sent Back");
        
        public final String code;
        public final String name;
        
        private RequestV4Status(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

}
