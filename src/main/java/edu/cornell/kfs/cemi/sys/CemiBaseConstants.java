package edu.cornell.kfs.cemi.sys;

import java.util.regex.Pattern;

public final class CemiBaseConstants {

    public static final String CEMI_ENVIRONMENT_LANE_NAME = "kfs-cemi";

    public static final String CEMI_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER = "cemiOutputDefinitionFileType";
    public static final String CEMI_OUTPUT_DEFINITION_FILE_PATH_PREFIX = "classpath:edu/cornell/kfs/cemi/";
    
    public static final String CEMI_TEMPLATE_WORKBOOK_FILE_PATH_PREFIX = "classpath:edu/cornell/kfs/cemi/";

    public enum CemiFieldDefinitionType {
        STATIC,
        STRING;
    }
    
    // This is a boolean KFS local configuration property value.
    //
    // When set to true, the amount of data retrieved for CEMI data extraction file creation WILL
    // be restricted to what is currently HARD CODED in the business object data access service 
    
    // CemiEXTRACTNAMEExtractOrmDaoOjbImpl.getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream
    // The batch job WILL successfully execute when this local configuration property is not defined.
    public static final String CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY = "cu.cemi.development.use.smaller.data.set";
    
    public static final String UNMASK = "UNMASK";

    public static final String ISO_3_CHAR_COUNTRY_CODE_UNKNOWN = "ZZZ";

    public static final class FileExtensions {
        public static final String XLSX = ".xlsx";
    }

    public static final String YES = "Y";
    
    public static final String EMPTY_STRING = "";
    
    // Date Formats
    public static final String DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd";

    public static final int BULK_DATA_BATCH_SIZE = 200;
    
    public static final String LAST_UPDT_TS = "LAST_UPDT_TS";

    // Regular expression pattern consisting of one or more word characters (letters, digits, underscores) from start to finish.
    public static final Pattern WORD_CHARS_PATTERN = Pattern.compile("^\\w+$");
}
