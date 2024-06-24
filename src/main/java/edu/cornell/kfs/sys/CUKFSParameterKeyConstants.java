package edu.cornell.kfs.sys;


public class CUKFSParameterKeyConstants {

    public static final String DEFAULT_CHART_CODE_METHOD = "DEFAULT_CHART_CODE_METHOD";
    public static final String DEFAULT_CHART_CODE = "DEFAULT_CHART_CODE";
    
    public static final String AGED_FYI_ACK_DOC_TYPES_INCLUDED = "AGED_FYI_ACK_DOC_TYPES_INCLUDED";
    public static final String AUTO_CANCEL_DOC_TYPES_PARAMETER = "AUTO_CANCEL_DOC_TYPES";   
    public static final String DAYS_TO_AUTO_CANCEL_PARAMETER = "DAYS_TO_AUTO_CANCEL";

    public static final String NON_REQUEUABLE_DOCUMENT_TYPES = "NON_REQUEUABLE_DOCUMENT_TYPES";
    public static final String REQUEUABLE_ROLES = "REQUEUABLE_ROLES";
    
    public static final String KFS_PDP = "KFS-PDP";
    public static final String ALL_COMPONENTS = "All";
    public static final String ACH_PAYMENT_COMBINING_IND = "ACH_PAYMENT_COMBINING_IND";
    public static final String BANK_PAYMENT_FILE_EMAIL_NOTIFICATION = "BANK_PAYMENT_FILE_EMAIL_NOTIFICATION";  
    public static final String PDP_CUSTOMER_MISSING_ADVICE_RETURN_EMAIL = "PDP_CUSTOMER_MISSING_ADVICE_RETURN_EMAIL";
    public static final String PDP_FORMAT_FAILURE_TO_EMAIL_ADDRESS = "PDP_FORMAT_FAILURE_TO_EMAIL_ADDRESS";

    public static final String CONFIDENTIAL_ATTACHMENT_FILENAME_PATTERNS = "CONFIDENTIAL_ATTACHMENT_FILENAME_PATTERNS";

    public static final String NON_EDITABLE_CREDENTIAL_VALUES = "NON_EDITABLE_CREDENTIAL_VALUES";

    public static final String KIM_FEED_SKIP_DELTA_FLAG_UPDATES = "KIM_FEED_SKIP_DELTA_FLAG_UPDATES";
    public static final String KIM_FEED_DELTAS_TO_LOAD = "KIM_FEED_DELTAS_TO_LOAD";

    public static class FpParameterConstants {
    	public static final String FP_ALLOWED_BUDGET_BALANCE_TYPES = "ALLOWED_BUDGET_BALANCE_TYPES";
    	public static final String FP_ALLOW_MULTIPLE_SUBFUNDS = "ALLOW_MULTIPLE_SUB_FUNDS";
    	public static final String FP_VALIDATE_CS_SUB_ACCOUNT_OR_ICR_ATTRIBUTES = "VALIDATE_CS_SUB_ACCOUNT_OR_ICR_ATTRIBUTES";
    	public static final String YEJV_CLOSING_CHARTS = "YEJV_CLOSING_CHARTS";
    }
    public static class YearEndAutoDisapprovalConstants {
		public static final String YEAR_END_AUTO_DISAPPROVE_START_DATE = "YEAR_END_AUTO_DISAPPROVE_START_DATE";
    }

    public static class GlParameterConstants {
        public static final String CURRENT_ASSET_OBJECT_TYPE_CODE = "CURRENT_ASSET_OBJECT_TYPE_CODE";
        public static final String CURRENT_LIABILITY_OBJECT_TYPE_CODE = "CURRENT_LIABILITY_OBJECT_TYPE_CODE";
        public static final String EXCLUDE_CB_PERIOD = "EXCLUDE_CB_PERIOD";
    }

    public static class LdParameterConstants {
        // KFSPTS-1627
        public static final String VALIDATE_TRANSFER_ACCOUNT_TYPES_IND = "VALIDATE_TRANSFER_ACCOUNT_TYPES_IND";
        public static final String INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT = "INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT";
    }
    
    public static class PurgeTablesParameterConstants {
        public static final String DEFAULT_NAME_SPACE_CODE = "KFS-SYS";
        public static final String DEFAULT_COMPONENT = "PurgeTablesStep";
        public static final String DEFAULT_PARAMETER_NAME = "DEFAULT_NUMBER_OF_DAYS_OLD";
    }
}
