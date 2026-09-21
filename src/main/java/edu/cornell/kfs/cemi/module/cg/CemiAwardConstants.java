package edu.cornell.kfs.cemi.module.cg;

public final class CemiAwardConstants {
    
    /* Award */
    public static final String SPREADSHEET_KEY_FORMAT = "ITH_{0}";
    public static final String AWARD_REFERENCE_ID_FORMAT = "ITH_{0}";
    public static final String AWARD_NUMBER_FORMAT = "ITH_{0}";
    public static final String RECEIVABLE_CONTRACT_LINE_REFERENCE_ID_ACCOUNT_FORMAT = "{0}_{1}";
    public static final String RECEIVABLE_CONTRACT_LINE_REFERENCE_ID_SUB_ACCOUNT_FORMAT = "{0}_{1}_{2}";

    public static final String NUMERIC_ONE = "1";
    public static final String PAYMENT_TYPE_EFT = "EFT";
    public static final String CURRENCY_USD = "USD";
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";
    public static final String ACTIVE = "Active";
    public static final String ITHACA_STANDARD_INTERIM = "Ithaca_Standard_Interim";
    public static final String AWARD = "Award";
    public static final String NO = "N";
    public static final String N = "N";
    public static final String NULL = "NULL";
    
    public static final String KFS_FIX_BAD_DATA = "KFS_FIX_BAD_DATA";
    public static final String KFS_FIX = "KFS_FIX";
    
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
    
    // Award Header: Award Life Cycle Status: Many-to-many legacy codes requiring additional logic 
    //               before translate table lookup can be performed using these key values.
    // before logic codes many-to-many
    public static final String AS = "AS";
    public static final String NF = "NF";
    public static final String PO = "PO";
    // after logic codes 1-to-many
    public static final String AS_ALL_SUBFUND_CGPREA = "AS-ALL-SUBFUND-CGPREA";
    public static final String AS_NON_SUBFUND_CGPREA_CNT_GRTR_ZERO = "AS-NON-SUBFUND-CGPREA-CNT-GRTR-ZERO";
    public static final String NF_NOT_ALL_ACCT_CLOSED = "NF-NOT-ALL-ACCT-CLOSED";
    public static final String NF_ALL_ACCT_CLOSED = "NF-ALL-ACCT-CLOSED";
    public static final String PO_ALL_ACCT_CLOSED = "PO-ALL-ACCT-CLOSED";
    public static final String PO_NOT_ALL_ACCT_CLOSED = "PO-NOT-ALL-ACCT-CLOSED";
    
    //FDM Default Values
    //public static final String DEFAULT_COST_CENTER = "CC000089";
    public static final String DEFAULT_FUND = "FD300";
    public static final String DEFAULT_FUNCTION = "FN0007";
    public static final String DEFAULT_GIFT = "GFITH";
    public static final String DEFAULT_PROJECT = "PRITH";
    public static final String DEFAULT_SPEND_CATEGORY = "SC9999";
    public static final String DEFAULT_REVENUE_CATEGORY = "RC9999";
}
