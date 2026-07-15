package edu.cornell.kfs.cemi.module.cg;

public final class CemiAwardScheduleConstants {
    
    /* Award Schedule */
    public static final String SPREADSHEET_KEY_FORMAT = "AS_ITH_{0}";
    public static final String AWARD_SCHEDULE_REFERENCE_ID_FORMAT = "AS_ITH_{0}";
    public static final String AWARD_PERIOD_REFERENCE_ID_FORMAT = "AS_ITH_{0}_Period";
    public static final String AWARD_POSTING_INTERVAL_ID_FORMAT = "AS_ITH_{0}_PSTINT";
    
    public static final String AWARD_PERIOD = "Award_Period";
    public static final String BUDGET_PERIOD = "Budget_Period";
    public static final String CINV_PERIOD = "CNV Period";
    public static final String NUMERIC_ONE = "1";
    
    // resources folder partial path location starting from folder AFTER "cemi" where output template defininton
    // is located. Do not include a forward slash in this definition.
    // Full path definition pertaining to Award Schedule output defintion file is below for comparison to 
    // how this parameter would need to be configured.
    //    classpath:edu/cornell/kfs/cemi/module/cg/batch/CemiAwardScheduleExtractFileOutputDefinition.xml
    public static final String AWARD_SCHEDULE_OUTPUT_DEFINITION_PATH_SUFFIX = "module/cg/batch/CemiAwardScheduleExtractFileOutputDefinition.xml";
    
    // resources folder partial path location starting from folder AFTER "cemi" where Excel template workkbook
    // to populate with the data being extracted is located. Do not include a forward slash in this definition.
    // Full path definition pertaining to Award Schedule mapping template to opulate file is below for comparison to 
    // how this parameter would need to be configured.
    //    classpath:edu/cornell/kfs/cemi/module/cg/batch/Put_Award_Schedule.xlsx
    public static final String AWARD_SCHEDULE_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "module/cg/batch/Put_Award_Schedule.xlsx";
    
    public static final String AWARD_SCHEDULE_EXTRACT_FILENAME_PREFIX = "Put_Award_Schedule_ITH_";
    public static final String AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME = "Put_Award_Schedule.xlsx";
    
    public static final class AwardScheduleExtractSheets {
        public static final String AWARD_SCHEDULE = "Award Schedule";
    }
    
    public static final String CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_SEQ = "CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_SEQ";
}
