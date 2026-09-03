package edu.cornell.kfs.cemi.module.cam;

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
    
    public static final class WorkdayCompany{
        public static final String STATUTORY = "C001";
        public static final String ENDOWED = "C002";
    }
    
    public static final String NUMERIC_ONE = "1";
    
    public static final String COST_CENTER_ID = "Cost_Center_ID";
    public static final String FUND_ID = "Fund_ID";
    public static final String PROGRAM_ID = "Program_ID";
    public static final String CORNELL_UNIVERSITY_ITHACA = "Cornell_University_Ithaca";
    public static final String STRAIGHT_LINE = "STRAIGHT_LINE";
    public static final String DEPRECATION_START_DATE = "7/1/2026";

}
