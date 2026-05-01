package edu.cornell.kfs.cemi.sys;

import java.sql.Types;

import org.kuali.kfs.sys.KFSConstants;

public final class CemiBaseConstants {

    public static final String CEMI_ENVIRONMENT_LANE_NAME = "kfs-cemi";

    public static final String CEMI_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER = "cemiOutputDefinitionFileType";
    public static final String CEMI_OUTPUT_DEFINITION_FILE_PATH_FORMAT =
            "classpath:edu/cornell/kfs/cemi/{0}/batch/{1}.xml";

    // Change this constant to "CEMI" when we're ready to move the sheet tables to the CEMI schema.
    public static final String DATA_SCHEMA = "KFS";
    public static final String DATA_SCHEMA_PREFIX = DATA_SCHEMA + KFSConstants.DELIMITER;
    public static final String SHEET_TABLE_PREFIX = "CU_CEMI_";
    public static final String SHEET_TABLE_SUFFIX = "_T";
    public static final String VAL_SUFFIX = "_VAL";
    public static final int DEFAULT_SHEET_COLUMN_SIZE = 200;
    public static final int DEFAULT_SHEET_COLUMN_PRECISION = 10;
    public static final int SHEET_TABLE_BATCH_SIZE = 200;

    public enum CemiFieldDefinitionType {
        STATIC(Types.VARCHAR, true),
        STRING(Types.VARCHAR, true),
        STRING_ENCRYPTED(Types.VARCHAR, true),
        STRING_DB_ONLY(Types.VARCHAR, false),
        INTEGER_DB_ONLY(Types.INTEGER, false),
        BIGINT_DB_ONLY(Types.BIGINT, false);

        public final int jdbcType;
        public final boolean includedInFileOutput;

        private CemiFieldDefinitionType(final int jdbcType, final boolean includedInFileOutput) {
            this.jdbcType = jdbcType;
            this.includedInFileOutput = includedInFileOutput;
        }
    }
    
    // This is a boolean KFS local configuration property value.
    // When set to true, the amount of data retrieved for Supplier CEMI file creation WILL be restricted
    // to what is currently HARD CODED in CuVendorDaoOjb.getVendorsForCemiSupplierExtractAsCloseableStream
    // The batch job WILL successfully execute when this local configuration property is not defined.
    public static final String CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY = "cu.cemi.development.use.smaller.data.set";
    
    public static final String UNMASK = "UNMASK";
    

    public static final class FileExtensions {
        public static final String XLSX = ".xlsx";
    }

    public static final class ModuleNames {
        public static final String VENDOR = "vnd";
    }

    public static final class OutputDefinitionFileNames {
        public static final String SUPPLIER = "CemiSupplierExtractFileOutputDefinition";
    }

}
