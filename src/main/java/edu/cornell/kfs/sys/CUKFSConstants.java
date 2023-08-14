package edu.cornell.kfs.sys;

import java.util.Collections;
import java.util.Set;

public class CUKFSConstants {
        
    public static final String COMMODITY_CODE_FILE_TYPE_INDENTIFIER = "commodityCodeInputFileType";
    public static final String ACCOUNT_MAINTENANCE_DOCUMENT_TYPE_DD_KEY = "AccountMaintenanceDocument";
    public static final String EMPLOYEE_RETIRED_STATUS = "R";
    public static final String KUALI_FREQUENCY_LOOKUPABLE_IMPL = "frequencyCodeLookupable";
    //cannot use SOURCE_ACCOUNTING_LINE_ERROR_PATTERN due to doubled error displayed in checking already added source accounting line
    public static final String NEW_SOURCE_LINE_ERRORS = "newSourceLine*";    
    public static final String ACCOUNT_RESTRICTED_STATUS_CODE = "R";
    public static final String CASH_REVERSION_OBJECT_CD = "CASH_REVERSION_OBJECT_CD";
    public static final String RULE_CODE_CA = "CA";
    public static final String DELIMITER = ".";
    public static final String US_LOCALE_STRING = "en_US";
    public static final String DATE_FORMAT_yyyyMMdd = "yyyyMMdd";
    public static final String DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd";
    public static final String DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am = "MM/dd/yyyy hh:mm:ss a";
    public static final String DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATE_FORMAT_dd_MMM_yy = "dd-MMM-yy";
    public static final String DATE_FORMAT_dd_MMM_yyyy = "dd-MMM-yyyy";
    public static final String DATE_FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_yyyy_MM = "yyyy/MM";
    public static final String DATE_FORMAT_yyyyMMdd_HHmmss = "yyyyMMdd_HHmmss";
    public static final String DATE_FORMAT_yyyyMMdd_HHmmssSSS = "yyyyMMdd_HHmmssSSS";
    public static final String DATE_FORMAT_MMddyyyy_hhmmss = "MMddyyyy_hhmmss";
    public static final String DATE_FORMAT_yyyyMMdd_HH_mm_ss_S = "yyyyMMdd-HH-mm-ss-S";
    public static final String DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_ZONE_UTC = "UTC";
    
    public static final String DECIMAL_FORMAT_0N_NN = "#0.00";
    
    public static class SysKimApiConstants {
        public static final String CONTRACTS_AND_GRANTS_PROCESSOR = "Contracts & Grants Processor";
        public static final String CREATE_DONE_FILE_PERMISSION_TEMPLATE_NAME = "Create Done File";
        public static final String DOWNLOAD_BATCH_FILE_PERMISSION_TEMPLATE_NAME = "Download Batch File";
    }       
    public static final class TaxRegionConstants {
        public static final String CREATE_TAX_REGION_FROM_LOOKUP_PARM = "createTaxRegionFromLookup";
    }
    public static final class FinancialDocumentTypeCodes {
        public static final String ACCOUNT = "ACCT";
        public static final String ACCOUNT_GLOBAL = "GACC";
    }        
    public static class ParameterNamespaces {
        public static final String ENDOWMENT = "KFS-ENDOW";
        public static final String PURCHASING = "KFS-PURAP";
        public static final String CONCUR = "KFS-CNCR";
    }
    public static class DocumentTypeAttributes {
        public static final String ACCOUNTING_DOCUMENT_TYPE_NAME = "ACCT";
    }
    
    public static class ObjectCodeConstants {
        public static final String PARAMETER_KC_ENABLE_RESEARCH_ADMIN_OBJECT_CODE_ATTRIBUTE_IND = "ENABLE_RESEARCH_ADMIN_OBJECT_CODE_ATTRIBUTE_IND";
    }
      
    // KFSUPGRADE-72 Account Reversion
    public static class Reversion {
        public static final String VALID_PREFIX = "EXTENDED_DEFINITIONS_INCLUDE_";
        public static final String INVALID_PREFIX = "EXTENDED_DEFINITIONS_EXCLUDE_";
        public static final String IS_EXPENSE_PARAM = "EXTENDED_DEFINITIONS_EXPENSE_CATEGORIES";
        public static final String OBJECT_CONSOL_PARAM_SUFFIX = "OBJECT_CONSOLIDATIONS_BY_REVERSION_CATEGORY";
        public static final String OBJECT_LEVEL_PARAM_SUFFIX = "OBJECT_LEVELS_BY_REVERSION_CATEGORY";
        public static final String OBJECT_TYPE_PARAM_SUFFIX = "OBJECT_TYPES_BY_REVERSION_CATEGORY";
        public static final String OBJECT_SUB_TYPE_PARAM_SUFFIX = "OBJECT_SUB_TYPES_BY_REVERSION_CATEGORY";
        public static final String SELECTION_1 = "SELECTION_1";
        public static final String SELECTION_4 = "SELECTION_4";
    }
    
    // Confidential-attachment-related constants.
    public static class ConfidentialAttachmentTypeCodes {
        public static final String CONFIDENTIAL_ATTACHMENT_TYPE = "Confidential";
        public static final String NON_CONFIDENTIAL_ATTACHMENT_TYPE = "";
    }

    public static final String NON_CONFIDENTIAL_ATTACHMENT_TYPE_LABEL = "Default";
    
    // I Want document constants
    public static final String I_WANT_DOC_ITEM_TAB_ERRORS = "document.item*,newIWantItemLine*";
    public static final String I_WANT_DOC_ACCOUNT_TAB_ERRORS = "newSourceLine*,document.account*,document.favoriteAccountLineIdentifier";
    public static final String I_WANT_DOC_VENDOR_TAB_ERRORS = "document.vendor*";
    public static final String I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS = "document.completeOption";
    public static final String I_WANT_DOC_MISC_ERRORS = "document.servicePerformedOnCampus*,document.commentsAndSpecialInstructions*";
    
    //KFSPTS-1460
    public static final String SEMICOLON = ";";
    
    // KFSUPGRADE-75
    public static final String MAINTAIN_FAVORITE_ACCOUNT = "Maintain Favorite Account";

    public static final String REPORTS_DIR = "reports";

    //KFSPTS-2400
    public static final String STAGING_DIR = "staging";
    public static final String LD_DIR = "ld";
    public static final String ENTERPRISE_FEED_DIR = "enterpriseFeed";
    public static final String FILE_SEPARATOR = "file.separator";

    // KFSUPGRADE-779
    public static final String PREQ_WIRETRANSFER_TAB_ERRORS = "PREQWireTransfersErrors,document.preqWireTransfer.preqBankName,document.preqWireTransfer.preqBankRoutingNumber,document.preqWireTransfer.preqBankCityName,document.preqWireTransfer.preqBankStateCode," + "document.preqWireTransfer.preqBankCountryCode,document.preqWireTransfer.preqAttentionLineText,document.preqWireTransfer.preqAdditionalWireText,document.preqWireTransfer.preqPayeeAccountNumber,document.preqWireTransfer.preqCurrencyTypeName,document.preqWireTransfer.preqCurrencyTypeCode," + "document.preqWireTransfer.preqWireTransferFeeWaiverIndicator,document.preqWireTransfer.preqPayeeAccountName,document.preqWireTransfer.preqPayeeAccountTypeCode,document.preqWireTransfer.preqAutomatedClearingHouseProfileNumber";
    public static final String CM_WIRETRANSFER_TAB_ERRORS = "CMWireTransfersErrors,document.cmWireTransfer.cmBankName,document.cmWireTransfer.cmBankRoutingNumber,document.cmWireTransfer.cmBankCityName,document.cmWireTransfer.cmBankStateCode," + "document.cmWireTransfer.cmBankCountryCode,document.cmWireTransfer.cmAttentionLineText,document.cmWireTransfer.cmAdditionalWireText,document.cmWireTransfer.cmPayeeAccountNumber,document.cmWireTransfer.cmCurrencyTypeName,document.cmWireTransfer.cmCurrencyTypeCode," + "document.cmWireTransfer.cmWireTransferFeeWaiverIndicator,document.cmWireTransfer.cmPayeeAccountName,document.cmWireTransfer.cmPayeeAccountTypeCode,document.cmWireTransfer.cmAutomatedClearingHouseProfileNumber";
    public static final String PREQ_FOREIGNDRAFTS_TAB_ERRORS = "PREQForeignDraftErrors,document.preqWireTransfer.preqForeignCurrencyTypeCode,document.preqWireTransfer.preqForeignCurrencyTypeName";
    public static final String CM_FOREIGNDRAFTS_TAB_ERRORS = "CMForeignDraftErrors,document.cmWireTransfer.cmForeignCurrencyTypeCode,document.cmWireTransfer.cmForeignCurrencyTypeName";
    
    public static final String DV_WIRETRANSFER_TAB_ERRORS_EXTENSION_FIELDS = "document.dvWireTransfer.extension.disbVchrBankStreetAddress,document.dvWireTransfer.extension.disbVchrBankProvince,document.dvWireTransfer.extension.disbVchrBankIBAN,document.dvWireTransfer.extension.disbVchrCorrespondentBankName,document.dvWireTransfer.extension.disbVchrBankSWIFTCode,document.dvWireTransfer.extension.disbVchrCorrespondentBankAddress,document.dvWireTransfer.extension.disbVchrSortOrTransitCode,document.dvWireTransfer.extension.disbVchrCorrespondentBankSwiftCode,document.dvWireTransfer.extension.disbVchrCorrespondentBankRoutingNumber,document.dvWireTransfer.extension.disbVchrCorrespondentBankAccountNumber";
    public static final String DV_WIRETRANSFER_TAB_ERRORS = "DVWireTransfersErrors,document.dvWireTransfer.disbursementVoucherBankName,document.dvWireTransfer.disbVchrBankRoutingNumber,document.dvWireTransfer.disbVchrBankCityName,document.dvWireTransfer.disbVchrBankStateCode," + "document.dvWireTransfer.disbVchrBankCountryCode,document.dvWireTransfer.disbVchrAttentionLineText,document.dvWireTransfer.disbVchrAdditionalWireText,document.dvWireTransfer.disbVchrPayeeAccountNumber,document.dvWireTransfer.disbVchrCurrencyTypeName,document.dvWireTransfer.disbVchrCurrencyTypeCode," + "document.dvWireTransfer.disbursementVoucherWireTransferFeeWaiverIndicator,document.dvWireTransfer.disbursementVoucherPayeeAccountName,document.dvWireTransfer.disbursementVoucherPayeeAccountTypeCode,document.dvWireTransfer.disbursementVoucherAutomatedClearingHouseProfileNumber," + DV_WIRETRANSFER_TAB_ERRORS_EXTENSION_FIELDS;

    public static class CGParms{
    	public static final String ACCOUNTS_EXEMPT_FROM_MULTIPLE_AWARDS_VALIDATION = "ACCOUNTS_EXEMPT_FROM_MULTIPLE_AWARDS_VALIDATION";
    	public static final String BYPASS_AWARD_EXTENSION_AUTO_APPROVE_REASON_RULE_INITIATORS = "BYPASS_AWARD_EXTENSION_AUTO_APPROVE_REASON_RULE_INITIATORS";
    	public static final String AWARD_RULE_COMPNONENT = "AwardRule";
    }
    
    public static final String COA_DOCUMENT_TYPE = "COA";
    public static final String GACC_DOCUMENT_TYPE = "GACC";
    
    // KFSPTS-3877
    public static class AccountCreateAndUpdateNotePrefixes {
        public static final String ADD = "Add";
        public static final String EDIT = "Edit";
    }
        
    public static final String ACCOUNT_NOTE_TEXT = " account document ID ";

    public static final String NOTE_SEQUENCE_NAME = "KRNS_NTE_S";

    public static final Set<String> OBJECTS_WITH_IMMEDIATE_BO_LEVEL_NOTE_UPDATE = Collections.singleton("org.kuali.kfs.coa.businessobject.Account");

    public static class A21SubAccountDocumentConstants {
        public static final String OFF_CAMPUS_INDICATOR_QUESTION_ID = "OffCampusIndicatorQuestion";
    }

    public static class AccountDocumentConstants {
        public static final String OFF_CAMPUS_INDICATOR_QUESTION_ID = "OffCampusIndicatorQuestion";
    }

    public static class BasicAccountingCategory {
        public static final String ASSET = "AS";
        public static final String EXPENSE = "EX";
    }

    public static class KimFeedConstants {
        public static final String ALL_UNPROCESSED_DELTAS_MODE = "ALL_UNPROCESSED_DELTAS";
        public static final String LATEST_DATE_ONLY_MODE = "LATEST_DATE_ONLY";
    }

    public static final String ACCOUNT_EXPIRED_OVERRIDE_PRESENT_PARAMETER_SUFFIX = ".accountExpiredOverride.present";
    public static final String ACCOUNT_EXPIRED_OVERRIDE_PARAMETER_SUFFIX = ".accountExpiredOverride";

    public static final String SUB_ACCOUNT_GLOBAL_CG_ICR_SECTION_NAME = "Global Sub Account CG ICR Maintenance";
    public static final String SUB_ACCOUNT_GLOBAL_CG_ICR_ACCOUNTS_SECTION_NAME = "Global Sub Account Indirect Cost Recovery Accounts Maintenance";

    public static final String SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_LABEL = "New Sub-Account";

    public static final String INTEGER_VALIDATION_PATTERN_TYPE = "integer";

    public static final String EQUALS_SIGN = "=";
    public static final String AMPERSAND = "&";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String CAPITAL_X = "X";
    public static final String PLUS_SIGN = "+";
    public static final String ELLIPSIS = "...";
    public static final String PADDED_HYPHEN = " - ";
    public static final String COMMA_AND_SPACE = ", ";
    public static final String NULL = "NULL";
    
    public static final String DOCUMENT_ID = "documentId";
    
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String TEXT_FILE_EXTENSION = ".txt";

    public static final String LOCATION_SERVICE_BEAN_NAME = "locationService-fin";

    public static final String DOCUMENT_REINDEX_FILE_NAME_PREFIX = "documentReindex";

    public static final class FileExtensions {
        public static final String CSV = ".csv";
        public static final String DONE = ".done";
        public static final String DATA = ".data";
    }
    
    public static final class OptionalModuleNamespaces {
        public static final String CONTRACTS_AND_GRANTS = "KFS-CG";
    }
    
    public static final class ProcurementCardParameters {
        public static final String PCARD_BATCH_LOAD_STEP = "ProcurementCardLoadStep";
    }

    public static final class NumericStrings {
        public static final String ONE_HUNDRED = "100";
    }

    public static final class WebappPaths {
        public static final String BASE_PATH = "/webapp";
        public static final String LOOKUP = BASE_PATH + "/lookup";
    }

    public static final String CU_ALLOW_LOCAL_BATCH_EXECUTION_KEY = "cu.allow.local.batch.execution";
    public static final String EXCEPTION_MESSAGING_GROUP = "Exception Messaging";
    public static final String EXCEPTION_MESSAGE_JOB_NAME_PREFIX = "Exception_Message_Job ";
    public static final String DELAYED_ASYNCHRONOUS_CALL_GROUP = "Delayed_Asynchronous_Call";
    public static final String DELAYED_ASYNCHRONOUS_CALL_JOB_NAME_PREFIX = "Delayed_Asynchronous_Call-";
    
    public static final String RESTRICTED_DATA_PLACEHOLDER = "RestrictedData";
    
    public static final class Config {
    	public static final String FIXED_POOL_SIZE = "ksb.fixedPoolSize";
    }
    
    public static final String LOCKING_DOCUMENT_CACHE_KEY = "lockingForDocumentId_";

    //** CU Generic ISO-FIPS Country modification items **
    public static final boolean MAPPING_ACTIVE = true;
    public static final boolean MAPPING_INACTIVE = false;
    public static final String ISO = "ISO";
    public static final String FIPS = "FIPS";

    public static final String COUNTRY_NAME_UNITED_STATES = "United States";
    public static final String ISO_COUNTRY_CODE_UNKNOWN = "ZZ";
    
    public static final class CuPaymentSourceConstants {
        public static final String PAYMENT_METHOD_INTERNAL_BILLING = "B";
    }
}
