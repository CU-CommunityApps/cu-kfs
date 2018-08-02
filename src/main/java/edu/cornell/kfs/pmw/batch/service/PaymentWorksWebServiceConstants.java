package edu.cornell.kfs.pmw.batch.service;

public class PaymentWorksWebServiceConstants {
    
    public static final String NEW_VENDOR_REQUESTS = "new-vendor-requests/";
    public static final String NEW_VENDOR_REQUEST_DETAILS_PREFIX = "new-vendor-requests";
    public static final String NEW_VENDOR_REQUEST_DETAILS_SUFFIX = "details/";
    public static final String NEW_VENDOR_REQUEST_UPDATE_STATUS = "new-vendor-requests/bulk/";

    public static final String SUPPLIERS_LOAD = "suppliers/load/";

    public static final String STATUS = "status";
    public static final String QUESTION_MARK = "?";
    public static final String FORWARD_SLASH = "/";
    
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String AUTHORIZATION_TOKEN_VALUE_STARTER = "Token ";

    public static final String EMPTY_JSON_WRAPPER = "{}";

    public static final class PaymentWorksCredentialKeys {
        public static final String PAYMENTWORKS_API_URL = "paymentworks.api.url";
        public static final String PAYMENTWORKS_USER_ID = "paymentworks.user.id";
        public static final String PAYMENTWORKS_AUTHORIZATION_TOKEN = "paymentworks.authorization.token";
    }

    public static final class PaymentWorksCommonJsonConstants {
        public static final String STATUS_FIELD = "status";
        public static final String STATUS_OK = "ok";
    }

    public static final class PaymentWorksTokenRefreshConstants {
        public static final String REFRESH_TOKEN_URL_FORMAT = "%susers/%s/refresh_auth_token/";
        public static final String AUTH_TOKEN_FIELD = "auth_token";
        public static final String DETAIL_FIELD = "detail";
    }

    public static final class PaymentWorksSupplierUploadConstants {
        public static final String DUMMY_SUPPLIERS_FILENAME = "suppliers.csv";
        public static final String SUPPLIERS_FIELD = "suppliers";
        public static final String NUM_RCVD_SUPPLIERS_FIELD = "num_rcvd_suppliers";
        public static final String ERROR_FIELD = "error";
    }

}
