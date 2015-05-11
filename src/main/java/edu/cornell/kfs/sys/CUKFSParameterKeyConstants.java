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

    public static class FpParameterConstants {
    	public static final String FP_ALLOWED_BUDGET_BALANCE_TYPES = "ALLOWED_BUDGET_BALANCE_TYPES";
    	public static final String FP_ALLOW_MULTIPLE_SUBFUNDS = "ALLOW_MULTIPLE_SUB_FUNDS";
    	public static final String FP_VALIDATE_CS_SUB_ACCOUNT_OR_ICR_ATTRIBUTES = "VALIDATE_CS_SUB_ACCOUNT_OR_ICR_ATTRIBUTES";
    }
    public static class YearEndAutoDisapprovalConstants {
		public static final String YEAR_END_AUTO_DISAPPROVE_START_DATE = "YEAR_END_AUTO_DISAPPROVE_START_DATE";
    }
    
    public static class GeneralLedgerSysParmeterKeys {
        public static final String EXPENSE_OBJECT_TYPE_CODE_PARAM = "EXPENSE_OBJECT_TYPE_CODE";
        public static final String INCOME_OBJECT_TYPE_CODE_PARAM = "INCOME_OBJECT_TYPE_CODE";
        public static final String CASH_BUDGET_RECORD_LEVEL_PARM = "CASH_BUDGET_RECORD_LEVEL";
        public static final String FUND_BALANCE_OBJECT_CODE_PARAM = "FUND_BALANCE_OBJECT_CODE";
        public static final String CURRENT_ASSET_OBJECT_CODE_PARAM = "CURRENT_ASSET_OBJECT_TYPE_CODE";
        public static final String CURRENT_LIABILITY_OBJECT_CODE_PARAM = "CURRENT_LIABILITY_OBJECT_TYPE_CODE";
        public static final String ENCUMBRANCE_BALANCE_TYPE_PARAM = "ENCUMBRANCE_BALANCE_TYPE";
        public static final String EXCLUDE_CB_PERIOD_PARAM = "EXCLUDE_CB_PERIOD";
        }
    
    public static class LdParameterConstants {
        // KFSPTS-1627
        public static final String VALIDATE_TRANSFER_ACCOUNT_TYPES_IND = "VALIDATE_TRANSFER_ACCOUNT_TYPES_IND";
        public static final String INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT = "INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT";
    }
}
