package edu.cornell.kfs.cemi.module.cam;

import java.util.Set;

public final class CemiRegisterAssetConstants {
    
    /* Register Asset */
    public static final String SPREADSHEET_KEY_FORMAT = "BA_{0}";
    
    public static final String REGISTER_ASSET_OUTPUT_DEFINITION_PATH_SUFFIX = "module/cam/batch/CemiRegisterAssetExtractFileOutputDefinition.xml";
    
    public static final String REGISTER_ASSET_WORKBOOK_FILE_PATH_SUFFIX = "module/cam/batch/Register_Asset.xlsx";
    
    public static final String REGISTER_ASSET_EXTRACT_FILENAME_PREFIX = "Register_Asset_ITH_";
    public static final String REGISTER_ASSET_EXTRACT_PLAIN_FILENAME = "Register_Asset.xlsx";
    
    
    
    public static final class RegisterAssetExtractSheets {
        public static final String REGISTER_ASSET = "Register_Asset";
    }
    
    public static final class AccountType {
        public static final String EN = "EN";
        public static final String CC = "CC";
    }
    
    public static final class WorkdayCompany {
        public static final String CU_MAIN_CAMPUS = "C001";
        public static final String CU_COLLEGES_HCM = "C002";
    }
    
    public static final String DEFAULT_ITHACA_COST_CENTER = "CC000089";
    public static final String DEFAULT_ITHACA_FUND = "FD300";
    public static final String DEFAULT_ITHACA_FUNCTION = "FN0007";
    public static final String DEFAULT_ITHACA_PROJECT = "PRITH";
    public static final String DEFAULT_ITHACA_SPEND_CATEGORY = "SC9999";
    
    public static final String NUMERIC_ONE = "1";
    
    public static final String COST_CENTER_ID = "Cost_Center_ID";
    public static final String FUND_ID = "Fund_ID";
    public static final String CORNELL_UNIVERSITY_ITHACA = "Cornell_University_Ithaca";
    public static final String STRAIGHT_LINE = "STRAIGHT_LINE";
    public static final String DEPRECIATION_START_DATE = "7/1/2026";
    
    
    public enum RegisterAssetTranslateTables {
        ASSET_TYPE_CODE_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ASST_TYP_T WHERE CAMPUS = 'Ithaca'"),
        ACCOUNTING_TREATMENT_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ACCTNG_TRTMNT_T WHERE CAMPUS = 'Ithaca'"),
        ASSET_CLASS_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ASST_CLSS_T WHERE CAMPUS = 'Ithaca'"),
        DEPRECIATION_PROFILE_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_DEPR_PRFL_T WHERE CAMPUS = 'Ithaca'");

        public final String queryString;

        private RegisterAssetTranslateTables(String queryString) {
            this.queryString = queryString;
        }
    }
    
    public static final String ACQUISITION_METHOD_OTHER = "OTHER";
    public static final String ACQUISITION_METHOD_PURCHASED = "PURCHASED";

    public static final Set<String> OTHER_ACQUISITION_TYPE_CODES = Set.of("G", "T", "Y");
    public static final Set<String> PURCHASED_ACQUISITION_TYPE_CODES = Set.of("N");

}
