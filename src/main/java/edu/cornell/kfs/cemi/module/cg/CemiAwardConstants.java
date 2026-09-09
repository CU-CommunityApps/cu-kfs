package edu.cornell.kfs.cemi.module.cg;

public final class CemiAwardConstants {
    
    /* Award */
    public static final String SPREADSHEET_KEY_FORMAT = "ITH_{0}";
    public static final String AWARD_REFERENCE_ID_FORMAT = "ITH_{0}";
    public static final String AWARD_NUMBER_FORMAT = "ITH_{0}";
    public static final String RECEIVABLE_CONTRACT_LINE_REFERENCE_ID_FORMAT = "{0}_{1}";

    public static final String NUMERIC_ONE = "1";
    public static final String PAYMENT_TYPE_EFT = "EFT";
    public static final String CURRENCY_USD = "USD";
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";
    public static final String ACTIVE = "Active";
    public static final String ITHACA_STANDARD_INTERIM = "Ithaca_Standard_Interim";
    public static final String AWARD = "Award";
    public static final String NO = "N";
    public static final String NULL = "NULL";
    
    public static final String KFS_FIX_BAD_DATA = "KFS_FIX_BAD_DATA";
    
    
    public static final String AWARD_OUTPUT_DEFINITION_PATH_SUFFIX = "module/cg/batch/CemiAwardExtractFileOutputDefinition.xml";
    
    public static final String AWARD_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "module/cg/batch/Submit_Award.xlsx";
    
    public static final String AWARD_EXTRACT_FILENAME_PREFIX = "Submit_Award_ITH_";
    public static final String AWARD_EXTRACT_PLAIN_FILENAME = "Submit_Award.xlsx";
    
    public static final String AWARD_EXTRACT_SKIPPED_AWARDS_FILE_PREFIX = "award-extract-skipped-awards-";
    
    public static final class AwardExtractSheets {
        public static final String SUBMIT_AWARD = "Submit Award";
    }
 
    public enum AwardTranslateTables {
        SPONSOR_AWARD_TYPES_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_AWD_SPNSR_AWD_TYP_T WHERE CAMPUS = 'Ithaca'"),
        AWARD_PURPOSE_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_AWD_AWD_PURPOSE_T WHERE CAMPUS = 'Ithaca'"),
        AWARD_LINE_LIFECYCLE_STATUS_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_AWD_LN_AWD_LFCYC_STAT_T WHERE CAMPUS = 'Ithaca'"),
        AWARD_LINE_TYPES_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_AWD_AWD_LINE_TYP_T WHERE CAMPUS = 'Ithaca'");

        public final String queryString;
        
        private AwardTranslateTables(String queryString) {
            this.queryString = queryString;
        }
    }
    
    // Company value to use for Ithaca hard coding.
    public static final String COMPANY_CORNELL_UNIVERISY_MAIN_CAMPUS = "C001";
    
}
