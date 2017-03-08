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

    public static final String DEFAULT_TRAVEL_REQUEST_OBJECT_CODE_PARAMETER_NAME = "DEFAULT_TRAVEL_REQUEST_OBJECT_CODE";

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
    
    public class ConcurPdpConstants {
        public static final String CREDIT = "CR";
        public static final String DEBIT = "DR";
    }
    
    public class StandardAccountingExtractPdpConstants {
        public static final String PAYMENT_CODE_CASH = "CASH";
        public static final String COMBINDED_GROUP_INDICATOR = "Y";
        public static final String BANK_CODE = "DISB";
        /**
         * @todo, figure this origin code out
         */
        //public static final String FS_ORIGIN_CODE = "Z6";
        public static final String FS_ORIGIN_CODE = "Z1";
        public static final String DOCUMENT_TYPE = "APTR";
        public static final String EMPLOYEE_STATUS_CODE = "EMPLOYEE";
        public static final String EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "E";
        public static final String NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE = "Y"; 
        public static final String DEFAULT_CHART_CODE = "IT";
        public static final String DEFAULT_UNIT = "CRNL";
        public static final String DEFAULT_SUB_UNIT = "CNCR";
    }
}
