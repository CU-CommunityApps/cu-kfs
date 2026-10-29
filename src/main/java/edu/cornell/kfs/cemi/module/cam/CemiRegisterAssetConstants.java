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
    public static final String NUMERIC_ZERO = "0";
    
    public static final String COST_CENTER_ID = "Cost_Center_Reference_ID";
    public static final String FUND_ID = "Fund_ID";
    public static final String FUNCTION_ID = "Custom_Organization_Reference_ID";
    public static final String CORNELL_UNIVERSITY_ITHACA = "Cornell_University_Ithaca";
    public static final String STRAIGHT_LINE = "STRAIGHT_LINE";
    public static final String DEPRECIATION_START_DATE = "2026-07-01";
    
    
    public enum RegisterAssetTranslateTables {
        ASSET_TYPE_CODE_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ASST_TYP_T WHERE CAMPUS = 'Ithaca'"),
        ACCOUNTING_TREATMENT_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ACCTNG_TRTMNT_T WHERE CAMPUS = 'Ithaca'"),
        ASSET_CLASS_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_ASST_CLSS_T WHERE CAMPUS = 'Ithaca'"),
        DEPRECIATION_PROFILE_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_DEPR_PRFL_T WHERE CAMPUS = 'Ithaca'"),
        SPEND_CATEGORY_QUERY("SELECT LEGACY_CODE, WORKDAY_REF_ID FROM CEMI.CU_CEMI_TRANS_KFS_WRKDY_REGISTER_ASST_SPEND_CTGRY_T WHERE CAMPUS = 'Ithaca'");
        public final String queryString;

        private RegisterAssetTranslateTables(String queryString) {
            this.queryString = queryString;
        }
    }
    
    public static final String ACQUISITION_METHOD_OTHER = "OTHER";
    public static final String ACQUISITION_METHOD_PURCHASED = "PURCHASED";
    public static final String ACQUISITION_METHOD_COMPOSED = "COMPOSED";
    public static final String ACQUISITION_METHOD_LEASED = "LEASED";

    public static final Set<String> OTHER_ACQUISITION_TYPE_CODES = Set.of("G", "T", "Y", "A", "F", "P");
    public static final Set<String> PURCHASED_ACQUISITION_TYPE_CODES = Set.of("N");
    public static final Set<String> COMPOSED_ACQUISITION_TYPE_CODES = Set.of("C");
    public static final Set<String> LEASED_ACQUISITION_TYPE_CODES = Set.of("L", "O");
    
    
    public static final String NON_DEPRECIATION_ASSET_CLASS_ID = "Non_depreciable";
    
    public static final Set<String> INVALID_EMPL_IDS = Set.of(
            "1000492", "1000993", "1001445", "1001851", "1002444", "1003548",
            "1004023", "1004591", "1005319", "1005419", "1005506", "1005732",
            "1005765", "1005905", "1006204", "1006425", "1007073", "1007135",
            "1007631", "1007645", "1008485", "1008721", "1008901", "1008919",
            "1010676", "1010708", "1011037", "1011536", "1011960", "1012050",
            "1014067", "1015862", "1016026", "1016219", "1016390", "1016642",
            "1018038", "1021369", "1021480", "1084701", "1126866", "1272682",
            "1276989", "1278024", "1278040", "1286712", "1288501", "1288734",
            "1297073", "1297877", "1299486", "1300410", "1300544", "1309149",
            "1310657", "1314631", "1319145", "1495522", "1774427", "1776376",
            "1826946", "1836725", "1839756", "1842238", "1852664", "1856784",
            "1859346", "1862138", "2108976", "2241997", "2285066", "2318408",
            "2340276", "2478626", "2483322", "2626769", "2676609", "2803398",
            "3129315", "3543133", "3962714", "4026246", "4046834", "4165093",
            "4181928", "4284311", "4388262", "4446047", "4890676", "4912915",
            "4926202", "5013270", "5085472", "5156834", "5165219", "5199047",
            "5346024", "5348774", "5444365", "5474603", "5482399", "5595896",
            "5598071", "5599559"
        );

}
