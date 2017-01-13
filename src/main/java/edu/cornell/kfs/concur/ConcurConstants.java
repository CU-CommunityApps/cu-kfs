package edu.cornell.kfs.concur;

public class ConcurConstants {
    public static final String AUTHORIZATION_PROPERTY = "Authorization";
    public static final String BASIC_AUTHENTICATION_SCHEME = "Basic";
    public static final String OAUTH_AUTHENTICATION_SCHEME = "OAuth";

    public static final int VALIDATION_RESULT_MESSAGE_MAX_LENGTH = 2000;
    
    public static class AccountingStringValidationErrorMessages {
        public static final String ERROR_CHART_OF_ACCTS_REQUIRED = "Chart is a required field.";
        public static final String ERROR_ACCT_NBR_REQUIRED = "Account Number is a required field.";
        public static final String ERROR_OBJ_CD_REQUIRED = "Object Code is a required field.";
        public static final String ERROR_ACCT_DOES_NOT_EXIST = "The Account does not exist in KFS.";
        public static final String ERROR_OBJ_CD_DOES_NOT_EXIST = "The Object Code does not exits in KFS.";
        public static final String ERROR_SUB_ACCT_DOES_NOT_EXIST = "The Sub Account does not exits in KFS.";
        public static final String ERROR_SUB_OBJ_CD_DOES_NOT_EXIST = "The Sub Object Code does not exits in KFS.";
        public static final String ERROR_PRJ_CD_DOES_NOT_EXIST = "The Project Code does not exits in KFS.";
        public static final String ERROR_ACCT_CLOSED = "The Account is closed.";
        public static final String ERROR_ACCT_INACTIVE = "The Account is inactive.";
        public static final String ERROR_OBJ_CD_INACTIVE = "The Object Code is inactive.";
        public static final String ERROR_SUB_ACCT_INACTIVE = "The Sub Account is inactive.";
        public static final String ERROR_SUB_OBJ_CD_INACTIVE = "The Sub Object Code is inactive.";
        public static final String ERROR_PRJ_CD_INACTIVE = "The Project Code is inactive.";
    }
    public static final String REFRESH_TOKEN_URL_PARAM = "refresh_token";
    public static final String CLIENT_ID_URL_PARAM = "client_id";
    public static final String CLIENT_SECRET_URL_PARAM = "client_secret";

    public static final String CONCUR_ACCESS_TOKEN = "concur.access.token";
    public static final String CONCUR_REFRESH_TOKEN = "concur.refresh.token";
    public static final String CONCUR_CONSUMER_KEY = "concur.consumer.key";
    public static final String CONCUR_SECRET_KEY = "concur.secret.key";
    public static final String CONCUR_ACCESS_TOKEN_EXPIRATION_DATE = "concur.access.token.expiration.date";

}
