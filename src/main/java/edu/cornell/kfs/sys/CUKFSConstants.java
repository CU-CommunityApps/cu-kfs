package edu.cornell.kfs.sys;

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
    
    public static class SysKimApiConstants {
        public static final String ESHOP_USER_ROLE_NAME = "eShop User (cu)";
        public static final String ESHOP_SUPER_USER_ROLE_NAME = "eShop Plus User(cu)";
        public static final String CONTRACTS_AND_GRANTS_PROCESSOR = "Contracts & Grants Processor";
        public static final String ADVANCE_DEPOSIT_ORGANIZATION_REVIEWER_ROLE_NAME = "Advanced Deposit Organization Review";
        public static final String CREATE_DONE_FILE_PERMISSION_TEMPLATE_NAME = "Create Done File";
    }       
    public static final class TaxRegionConstants {
        public static final String CREATE_TAX_REGION_FROM_LOOKUP_PARM = "createTaxRegionFromLookup";
    }
    public static final class ReportGeneration {
        public final static String SIP_EXPORT_FILE_NAME = "sip_export.txt";
        public final static String SIP_EXPORT_FILE_NAME_EXECUTIVES = "sip_export_executives.txt";
    }        
    public static final class FinancialDocumentTypeCodes {
        public static final String PAYMENT_APPLICATION = "APP";
        
    }        
    public static class ParameterNamespaces {
        public static final String ENDOWMENT = "KFS-ENDOW";
        public static final String PURCHASING = "KFS-PURAP";
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
    
    public static class PreEncumbranceDocumentConstants {
        public static final String BIWEEKLY = "biWeekly";
        public static final String CUSTOM = "custom";
        public static final String SEMIMONTHLY = "semiMonthly";
        public static final String MONTHLY = "monthly";
    }
    
    public static class PreEncumbranceSourceAccountingLineConstants {
        public static final String END_DATE = "endDate";
        public static final String START_DATE = "startDate";
        public static final String AUTO_DISENCUMBER_TYPE = "autoDisEncumberType";
        public static final String PARTIAL_TRANSACTION_COUNT = "partialTransactionCount";
        public static final String PARTIAL_AMOUNT = "partialAmount";
    }
    
    // I Want document constants
    public static final String I_WANT_DOC_ITEM_TAB_ERRORS = "document.item*,newIWantItemLine*";
    public static final String I_WANT_DOC_ACCOUNT_TAB_ERRORS = "newSourceLine*,document.account*";
    public static final String I_WANT_DOC_VENDOR_TAB_ERRORS = "document.vendor*";
    public static final String I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS = "document.completeOption";
    public static final String I_WANT_DOC_MISC_ERRORS = "document.servicePerformedOnCampus*,document.commentsAndSpecialInstructions*";
    
    //KFSPTS-1460
    public static final String SEMICOLON = ";";
    
    // KFSUPGRADE-75
    public static final String MAINTAIN_FAVORITE_ACCOUNT = "Maintain Favorite Account";

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

    public static class ChartApcParms{
    	public static final String EXPIRATION_DATE_BACKDATING_FUND_GROUPS = "EXPIRATION_DATE_BACKDATING_FUND_GROUPS";
    }
        
}
