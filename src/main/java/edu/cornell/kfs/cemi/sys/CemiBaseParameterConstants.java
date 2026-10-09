package edu.cornell.kfs.cemi.sys;

public class CemiBaseParameterConstants {

    public static final String COPY_CEMI_FILE_TO_OUTBOUND_FOLDER = "COPY_CEMI_FILE_TO_OUTBOUND_FOLDER";
    public static final String CEMI_SENSITIVE_DATA_MASKING_SETTING = "CEMI_SENSITIVE_DATA_MASKING_SETTING";
    public static final String CEMI_LEGACY_DATA_IMPORT_FILE_TYPE = "CEMI_LEGACY_DATA_IMPORT_FILE_TYPE";

    // Overrides defined on the KFS-CEMI "All" component. When populated, these take precedence over the
    // job-specific COPY_*_TO_OUTBOUND_FOLDER and CEMI_SENSITIVE_DATA_MASKING_SETTING parameters for every extract.
    // When blank, each extract uses its own job-specific parameter.
    public static final String CEMI_ALL_EXTRACTS_COPY_FILE_TO_OUTBOUND_FOLDER_OVERRIDE =
            "CEMI_ALL_EXTRACTS_COPY_FILE_TO_OUTBOUND_FOLDER_OVERRIDE";
    public static final String CEMI_ALL_EXTRACTS_SENSITIVE_DATA_MASKING_OVERRIDE =
            "CEMI_ALL_EXTRACTS_SENSITIVE_DATA_MASKING_OVERRIDE";

}
