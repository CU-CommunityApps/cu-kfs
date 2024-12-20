package edu.cornell.kfs.tax;

import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Helper class containing various tax-processing-related constants.
 */
public final class CUTaxConstants {

    public static final String TAX_NAMESPACE = "KFS-TAX";
    public static final String TAX_PARM_DETAIL = "IrsTaxExtract";
    public static final String TAX_1099_PARM_DETAIL = "Irs1099Extract";
    public static final String TAX_1042S_PARM_DETAIL = "Irs1042sExtract";
    public static final String YEAR_TO_DATE = "YTD";
    public static final String PREVIOUS_YEAR_TO_DATE = "PYTD";

    public static final String TAX_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER = "taxOutputDefinitionFileType";
    public static final String TAX_OUTPUT_DEFINITION_V2_FILE_TYPE_IDENTIFIER = "taxOutputDefinitionV2FileType";
    public static final String TAX_DATA_DEFINITION_FILE_TYPE_IDENTIFIER = "taxDataDefinitionFileType";
    public static final String TRANSACTION_OVERRIDE_FILE_TYPE_IDENTIFIER = "transactionOverrideCSVInputFileType";

    public static final String RECORD_SOURCE_KFS = "KFS";
    public static final String TAX_TYPE_1099 = "1099";
    public static final String TAX_TYPE_1042S = "1042S";
    public static final String TAX_TYPE_1042S_CREATE_TRANSACTION_ROWS_ONLY = "1042S_CREATE_TRANSACTION_ROWS_ONLY";
    public static final String TAX_SOURCE_DV = "DV";
    public static final String TAX_SOURCE_PDP = "PDP";
    public static final String TAX_SOURCE_PRNC = "PRNC";
    public static final String DOC_ID_ZERO = "0";
    public static final String DOC_TITLE_IF_NOT_FOUND = "No Doc Header";
    public static final String NETID_IF_NOT_FOUND = "No NetID";
    public static final String NO_US_VENDOR_ADDRESS = "No US Address on File!";
    public static final String NO_FOREIGN_VENDOR_ADDRESS = "No Foreign Address on File!";
    public static final String NO_ANY_VENDOR_ADDRESS = "No Address on File!";
    public static final String TAX_1099_MISC_FORM_TYPE = "MISC";
    public static final String TAX_1099_NEC_FORM_TYPE = "NEC";
    public static final String TAX_1099_UNKNOWN_FORM_TYPE = "????";
    public static final String NEEDS_UPDATING_BOX_KEY = "?";
    public static final String TAX_1099_UNKNOWN_BOX_KEY = "???";
    public static final Pair<String, String> TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY = Pair.of(
            TAX_1099_UNKNOWN_FORM_TYPE, TAX_1099_UNKNOWN_BOX_KEY);
    public static final String TAX_1042S_UNKNOWN_BOX_KEY = "????";
    public static final String ANY_OR_NONE_PAYMENT_REASON = "*";
    public static final String UNKNOWN_COUNTRY = "UC";
    public static final String MASKED_VALUE_9_CHARS = "XXXXXXXXX";
    public static final String MASKED_VALUE_11_CHARS = "XXXXXXXXXXX";
    public static final String MASKED_VALUE_19_CHARS = "XXXXXXXXXXXXXXXXXXX";
    public static final int INSERT_BATCH_SIZE = 500;
    public static final int TAX_1099_MAX_BUCKET_LENGTH = 3;
    public static final double TAX_1099_DEFAULT_MIN_REPORTING_AMOUNT = 0.01;

    public static final String TAX_1099_MISC_OUTPUT_FILE_PREFIX = "irs_1099_misc_extract_";
    public static final String TAX_1099_NEC_OUTPUT_FILE_PREFIX = "irs_1099_nec_extract_";
    public static final String TAX_1099_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX = "irs_1099_transaction_details_";
    public static final String TAX_1042S_BIO_OUTPUT_FILE_PREFIX = "irs_1042s_biographical_extract_";
    public static final String TAX_1042S_DETAIL_OUTPUT_FILE_PREFIX = "irs_1042s_detail_extract_";
    public static final String TAX_1042S_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX = "irs_1042s_transaction_details_";
    public static final String TAX_OUTPUT_FILE_SUFFIX = ".txt";

    public static class Sprintax {
        public static final int MAX_FIELD_LENGTH = 90;

        public static final String PAYMENTS_OUTPUT_FILE_PREFIX = "irs_1042s_sprintax_payments_";
        public static final String BIO_OUTPUT_FILE_PREFIX = "irs_1042s_sprintax_bio";
        public static final String TAX_CSV_FILE_SUFFIX = ".csv";

    }

    public static final String FORM_1042S_GROSS_BOX = "GROSS";
    public static final String FORM_1042S_FED_TAX_WITHHELD_BOX = "FTW";
    public static final String FORM_1042S_STATE_INC_TAX_WITHHELD_BOX = "SITW";

    public static final String CH3_EXEMPTION_NOT_EXEMPT_KEY = "NotExempt";
    public static final String CH3_EXEMPTION_TAX_TREATY_KEY = "TaxTreaty";
    public static final String CH3_EXEMPTION_FOREIGN_SOURCE_KEY = "ForeignSource";

    // Constants pertaining to number and date formatting for output.
    public static final String DEFAULT_AMOUNT_FORMAT = "#########.##";
    public static final String DEFAULT_PERCENT_FORMAT = "00.00";
    public static final String DEFAULT_SPRINTAX_PERCENT_FORMAT = "00.##";
    public static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    public static final String FILENAME_SUFFIX_DATE_FORMAT = "_MMddyyyy_HH_mm_ss_SSS";
    public static final int DEFAULT_AMOUNT_MAX_INT_DIGITS = 9;
    public static final int DEFAULT_PERCENT_MAX_INT_DIGITS = 2;

    // Constants pertaining to the default ResultSet and PreparedStatement setup for TransactionRowProcessor.
    public static final int VENDOR_DETAIL_INDEX = 0;
    public static final int DOC_NOTES_INDEX = 1;
    public static final int DEFAULT_EXTRA_RS_SIZE = 2;
    public static final int VENDOR_ADDRESS_COUNTRY_CODE_PARAM_INDEX = 3;

    public static final Pattern TAX_1099_BOX_MAPPING_KEY_PATTERN = Pattern.compile(
            "^(\\w{1,4}|\\?{1,4})\\((\\w{1,3}|\\?{1,3})\\)$");
    public static final int TAX_1099_BOX_MAPPING_KEY_FORM_TYPE_GROUP = 1;
    public static final int TAX_1099_BOX_MAPPING_KEY_BOX_NUMBER_GROUP = 2;
    public static final String TAX_1099_BOX_MAPPING_KEY_FORMAT = "{0}({1})";

    /**
     * Helper subclass containing config-prop-related constants.
     */
    public static final class CUTaxKeyConstants {
        public static final String TAX_OUTPUT_EIN = "tax.output.ein";
        public static final String TAX_OUTPUT_SCRUBBED = "tax.output.scrubbed";
        public static final String MESSAGE_BATCH_UPLOAD_TITLE_TAX_OUTPUT_DEFINITION = "message.batchUpload.title.taxOutputDefinition";
        public static final String MESSAGE_BATCH_UPLOAD_TITLE_TAX_OUTPUT_DEFINITION_V2 = "message.batchUpload.title.taxOutputDefinitionV2";
        public static final String MESSAGE_BATCH_UPLOAD_TITLE_TAX_DATA_DEFINITION = "message.batchUpload.title.taxDataDefinition";
        public static final String MESSAGE_BATCH_UPLOAD_TITLE_TRANSACTION_OVERRIDE = "message.batchUpload.title.transactionOverride";
        public static final String ERROR_BATCH_UPLOAD_INVALID_TRANSACTION_OVERRIDES = "error.batchUpload.invalidTransactionOverrides";
        public static final String ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1099_BOX_LENGTH =
                "error.document.transactionOverrideMaintenance.1099.box.length";
        public static final String ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1099_FORMTYPE_EMPTY =
                "error.document.transactionOverrideMaintenance.1099.formType.empty";
        public static final String ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1042S_FORMTYPE_NONEMPTY =
                "error.document.transactionOverrideMaintenance.1042s.formType.nonEmpty";
        
        /*
         * The filename parameters pointing to the TaxOutputDefinition and TaxDataDefinition XML
         * are expected to have the following formats:
         * 
         * tax.format.[taxType].[reportYear]
         * tax.format.[taxType].default (for cases where a report-year-specific format does not exist)
         * tax.format.[taxType].summary.[reportYear]
         * tax.format.[taxType].summary.default (for cases where a report-year-specific format does not exist)
         * 
         * tax.table.[taxType].[reportYear]
         * tax.table.[taxType].default (for cases where a report-year-specific format does not exist)
         * 
         * The "summary" ones refer to output definitions that should simply print the transaction row data.
         */
        public static final String TAX_FORMAT_1099_PREFIX = "tax.format.1099.";
        public static final String TAX_FORMAT_1042S_PREFIX = "tax.format.1042s.";
        public static final String TAX_FORMAT_SUMMARY_SUFFIX = "summary.";
        public static final String TAX_TABLE_1099_PREFIX = "tax.table.1099.";
        public static final String TAX_TABLE_1042S_PREFIX = "tax.table.1042s.";
        public static final String TAX_CONFIG_DEFAULT_SUFFIX = "default";
        
        private CUTaxKeyConstants() {
            throw new UnsupportedOperationException("do not call CUTaxKeyConstants constructor");
        }
    }



    /**
     * Helper subclass containing common-parameter-related constants.
     */
    public static final class TaxCommonParameterNames {
        public static final String TAX_TYPE = "TAX_TYPE";
        
        public static final String DATES_TO_PROCESS_PARAMETER_SUFFIX = "_DATES_TO_PROCESS";
        public static final String EXCLUDE_BY_VENDOR_TYPE_PARAMETER_SUFFIX = "_EXCLUDE_BY_VENDOR_TYPE";
        public static final String EXCLUDE_BY_OWNERSHIP_TYPE_PARAMETER_SUFFIX = "_EXCLUDE_BY_OWNERSHIP_TYPE";
        public static final String SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX = "_SOLE_PROPRIETOR_OWNER_CODE";
        
        private TaxCommonParameterNames() {
            throw new UnsupportedOperationException("do not call TaxCommonParameterNames constructor");
        }
    }



    /**
     * Helper subclass containing 1099-parameter-related constants.
     */
    public static final class Tax1099ParameterNames {
        public static final String INCLUDED_VENDOR_OWNERS_AND_CATEGORIES = "1099_INCLUDED_VENDOR_OWNERS_AND_CATEGORIES";
        public static final String EXCLUDED_PAYEE_ID = "1099_EXCLUDED_PAYEE_ID";
        public static final String PDP_EXCLUDED_DOC_TYPES = "1099_PDP_EXCLUDED_DOC_TYPES";
        public static final String EXCLUDED_PAYMENT_REASON_CODE = "1099_EXCLUDED_PAYMENT_REASON_CODE";
        public static final String OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR = "1099_OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR";
        public static final String EXCLUDED_DOC_NOTE_TEXT = "1099_EXCLUDED_DOC_NOTE_TEXT";
        public static final String EXCLUDED_OBJECT_CODE_AND_INITIATOR_PRNCPL_NM = "1099_EXCLUDED_OBJECT_CODE_AND_INITIATOR_PRNCPL_NM";
        public static final String ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT = "1099_ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT";
        public static final String NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_VENDOR_NAME =
                "1099_NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_VENDOR_NAME";
        public static final String NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_PARENT_VENDOR_NAME =
                "1099_NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_PARENT_VENDOR_NAME";
        public static final String NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DOC_TITLE =
                "1099_NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DOC_TITLE";
        public static final String TAB_SITE_ID = "1099_TAB_SITE_ID";
        public static final String RENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_RENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String OTHER_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_OTHER_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String FED_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_FED_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String FISHING_BOAT_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_FISHING_BOAT_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String MEDICAL_HEALTH_CARE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_MEDICAL_HEALTH_CARE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String NONEMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_NONEMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String SUBSTITUTE_PAYMENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_SUBSTITUTE_PAYMENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String CROP_INSURANCE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_CROP_INSURANCE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String GOLDEN_PARACHUTE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_GOLDEN_PARACHUTE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String ATTORNEY_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_ATTORNEY_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String SECTION_409A_DEFERRALS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_SECTION_409A_DEFERRALS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String SECTION_409A_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_SECTION_409A_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String NONQUALIFIED_DEFERRED_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT =
                "1099_NONQUALIFIED_DEFERRED_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String STATE_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_STATE_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String STATE_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1099_STATE_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String PAYMENT_REASON_TO_TAX_BOX = "1099_PAYMENT_REASON_TO_TAX_BOX";
        public static final String PAYMENT_REASON_TO_NO_TAX_BOX = "1099_PAYMENT_REASON_TO_NO_TAX_BOX";
        public static final String DOCUMENT_TYPE_TO_TAX_BOX = "1099_DOCUMENT_TYPE_TO_TAX_BOX";
        public static final String TAX_BOX_NUMBER_MAPPINGS = "1099_TAX_BOX_NUMBER_MAPPINGS";
        public static final String TAX_BOX_MINIMUM_REPORTING_AMOUNTS = "1099_TAX_BOX_MINIMUM_REPORTING_AMOUNTS";
        public static final String FILER_ADDRESS = "1099_FILER_ADDRESS";
        
        private Tax1099ParameterNames() {
            throw new UnsupportedOperationException("do not call Tax1099ParameterNames constructor");
        }
    }



    /**
     * Helper subclass containing 1042S-parameter-related constants.
     */
    public static final class Tax1042SParameterNames {
        public static final String INCOME_CLASS_CODE_VALID_OBJECT_CODES = "1042S_INCOME_CLASS_CODE_VALID_OBJECT_CODES";
        public static final String INCOME_CLASS_CODE_TO_IRS_INCOME_CODE = "1042S_INCOME_CLASS_CODE_TO_IRS_INCOME_CODE";
        public static final String INCOME_CLASS_CODE_TO_IRS_INCOME_CODE_SUB_TYPE = "1042S_INCOME_CLASS_CODE_TO_IRS_INCOME_CODE_SUB_TYPE";
        public static final String NON_REPORTABLE_INCOME_CODE = "1042S_NON_REPORTABLE_INCOME_CODE";
        public static final String EXCLUDED_INCOME_CODE = "1042S_EXCLUDED_INCOME_CODE";
        public static final String EXCLUDED_INCOME_CODE_SUB_TYPE = "1042S_EXCLUDED_INCOME_CODE_SUB_TYPE";
        public static final String VENDOR_OWNERSHIP_TO_CHAPTER3_STATUS_CODE = "1042S_VENDOR_OWNERSHIP_TO_CHAPTER3_STATUS_CODE";
        public static final String CHAPTER3_EXEMPTION_CODES = "1042S_CHAPTER3_EXEMPTION_CODES";
        public static final String CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES = "1042S_CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES";
        public static final String CHAPTER4_DEFAULT_EXEMPTION_CODE = "1042S_CHAPTER4_DEFAULT_EXEMPTION_CODE";
        public static final String ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT = "1042S_ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT";
        public static final String FEDERAL_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT = "1042S_FEDERAL_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT";
        public static final String STATE_INCOME_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT =
                "1042S_STATE_INCOME_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT";
        public static final String ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT = "1042S_ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT";
        public static final String FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES = "1042S_FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES";
        public static final String STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES = "1042S_STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES";
        public static final String PDP_EXCLUDED_DOC_TYPES = "1042S_PDP_EXCLUDED_DOC_TYPES";
        public static final String INCOME_CLASS_CODE_DENOTING_ROYALTIES = "1042S_INCOME_CLASS_CODE_DENOTING_ROYALTIES";
        public static final String EXCLUDED_PAYMENT_REASON_CODE = "1042S_EXCLUDED_PAYMENT_REASON_CODE";
        public static final String OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR = "1042S_OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR";
        public static final String EXCLUDED_DOC_NOTE_TEXT = "1042S_EXCLUDED_DOC_NOTE_TEXT";
        public static final String STATE_NAME = "1042S_STATE_NAME";
        
        private Tax1042SParameterNames() {
            throw new UnsupportedOperationException("do not call Tax1042SParameterNames constructor");
        }
    }



    private CUTaxConstants() {
        throw new UnsupportedOperationException("do not call");
    }

}
