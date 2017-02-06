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

    public static final String EXPENSE_AWAIRING_EXTERNAL_VALIDATION_STATUS_CODE = "A_EXTV";
    public static final String REQUEST_AWAIRING_EXTERNAL_VALIDATION_STATUS_CODE = "Q_EXTV";

    public static final String APPROVE_ACTION = "Approve";
    public static final String SEND_BACK_TO_EMPLOYEE_ACTION = "Send Back To Employee";
    public static final String APPROVE_COMMENT = "Approved via Concur Connect";
    public static final String EXPENSE_WORKFLOW_UPDATE_NAMESPACE = "http://www.concursolutions.com/api/expense/expensereport/2011/03";
    public static final String REQUEST_WORKFLOW_UPDATE_NAMESPACE = "http://www.concursolutions.com/api/travelrequest/2012/06";

    public static final String ERROR_MESSAGE_HEADER = "Please resubmit your Report.\n\nThe Report is being returned due to an error. Please make the necessary corrections.\n\nError:\n";
}
