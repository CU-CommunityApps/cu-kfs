package edu.cornell.kfs.fp;

public class CuFPParameterConstants {
   
    public static class CorporateBilledCorporatePaidDocument {
        public static final String CBCP_COMPONENT_NAME = "CorporateBilledCorporatePaidDocument";
        public static final String CBCP_ACCOUNTING_DEFAULT_IND_PARAMETER_NAME = "CBCP_ACCOUNTING_DEFAULT_IND";
        public static final String CBCP_HOLDER_DEFAULT_IND_PARAMETER_NAME = "CBCP_HOLDER_DEFAULT_IND";
        public static final String DEFAULT_ACCOUNT_PARAMETER_NAME = "DEFAULT_ACCOUNT";
        public static final String DEFAULT_AMOUNT_OWED_OBJECT_CODE_PARAMETER_NAME = "DEFAULT_AMOUNT_OWED_OBJECT_CODE";
        public static final String DEFAULT_CHART_PARAMETER_NAME = "DEFAULT_CHART";
        public static final String DEFAULT_LIABILITY_OBJECT_CODE_PARAMETER_NAME = "DEFAULT_LIABILITY_OBJECT_CODE";
        public static final String DOCUMENT_EXPLANATION_PARAMETER_NAME = "DOCUMENT_EXPLANATION";
        public static final String SINGLE_TRANSACTION_IND_PARAMETER_NAME = "SINGLE_TRANSACTION_IND";
        public static final String CBCP_REPORT_EMAIL_ADDRESS_PARAMETER_NAME = "CBCP_REPORT_EMAIL_ADDRESS";
    }
    
    public static class CreateAccountingDocumentService {
        public static final String CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME = "CreateAccountingDocumentService";
        public static final String CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS = "CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS";
        public static final String WARNING_EMAIL_ADDRESS = "WARNING_EMAIL_ADDRESS";
        public static final String DUPLICATE_FILE_CHECK_IND = "DUPLICATE_FILE_CHECK_IND";
        public static final String DUPLICATE_FILE_REPORT_EMAIL_ADDRESS = "DUPLICATE_FILE_REPORT_EMAIL_ADDRESS";
    }
    
    public static class ProcurementCardDocument {
        public static final String CARD_TRANSACTION_TYPES_TO_SKIP = "CARD_TRANSACTION_TYPES_TO_SKIP";
        public static final String CARD_TRANSACTIONS_SKIPPED_EMAIL_ADDRESS = "CARD_TRANSACTIONS_SKIPPED_EMAIL_ADDRESS";
        public static final String CARD_TRANSACTIONS_SKIPPED_EMAIL_BODY_TEMPLATE = "CARD_TRANSACTIONS_SKIPPED_EMAIL_BODY_TEMPLATE";
    }
    
    public static class DisbursementVoucherDocument {
        public static final String NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE = "NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE";
    }
    
    public static final String ERROR_TRANS_OBJECT_CODE_PARM_NM = "ERROR_TRANSACTION_OBJECT_CODE";

}
