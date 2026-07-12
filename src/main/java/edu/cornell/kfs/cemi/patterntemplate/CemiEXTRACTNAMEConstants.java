package edu.cornell.kfs.cemi.patterntemplate;

public final class CemiEXTRACTNAMEConstants {
    
    // This section would hold the format definitions that a key or id should have in the data extraction.
    // These formats are defined in the Huron mapping sessions and would contain some form of static string
    // along with an actual legacy key value. The Java MessageFormat class would utilize these constants 
    // in the  following manner when performing legacy to Workday data value conversions:
    //      workdayStringValue = MessageFormat.format(Cemi{EXTRACTNAME}Constants.WORKDAY_OBJECT_REFERENCE_ID_FORMAT, legacyObjectSequenceNumber);
    //
    //    Examples:
    //        public static final String SPREADSHEET_KEY_FORMAT = "AS_ITH_{0}";
    //        public static final String AWARD_SCHEDULE_REFERENCE_ID_FORMAT = "AS_ITH_{0}";
    //        public static final String AWARD_PERIOD_REFERENCE_ID_FORMAT = "AS_ITH_{0}_Period";
    //        public static final String AWARD_POSTING_INTERVAL_ID_FORMAT = "AS_ITH_{0}_PSTINT";
    
    
    // This section would have any constant value defined by Huron during the
    // mapping sessions that should be used in place of a legacy system data value.
    // All of these values should be defined as Java static Strings.
    //    Examples:
    //        public static final String AWARD_PERIOD = "Award_Period";
    //        public static final String BUDGET_PERIOD = "Budget_Period";
    //        public static final String CINV_PERIOD = "CNV Period";
    //        public static final String NUMERIC_ONE = "1";
    
    
    // Partial file name without file extension used for the spreadsheet file as it is being populated.
    // This name should be the {EXTRACTNAME}_EXTRACT_PLAIN_FILENAME with _ITH appended to it.
    //    Example:
    //        public static final String AWARD_SCHEDULE_EXTRACT_FILENAME_PREFIX = "Put_Award_Schedule_ITH_";
    //
    public static final String EXTRACTNAME_EXTRACT_FILENAME_PREFIX = "ACTUAL_EXTRACT_FILE_NAME_ITH_";
    
    
    // Actual file name including file extension for the data extraction 
    // spreadsheet file that will be transmitted to the validation server.
    // This file name with extension should exactly match what Huron has 
    // specified in their mapping templates to populate Sharepoint folder. 
    //    Example: 
    //        public static final String AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME = "Put_Award_Schedule.xlsx";
    //
    public static final String EXTRACTNAME_EXTRACT_PLAIN_FILENAME = "ACTUAL_EXTRACT_FILE_NAME.xlsx";
    
    
    // This XML file defines the association between the Huron provided xlsx Excel spreadsheet
    // and the business object used to gather the data to be output.
    // The file path path in this constant should start at the first folder in the directory path: "edu/cornell/kfs/cemi/"
    //    Example:
    //        public static final String AWARD_SCHEDULE_OUTPUT_DEFINITION_FILE_PATH_SUFFIX = "module/cg/batch/CemiAwardScheduleExtractFileOutputDefinition.xml";
    //
    public static final String EXTRACTNAME_OUTPUT_DEFINITION_FILE_PATH_SUFFIX = "module/patterntemplate/batch/CemiEXTRACTNAMEExtractFileOutputDefinition.xml";
    
    // Where to find the empty mapping template version of the Excel spreadsheet provided by  
    // Huron to populate. The definition should include both the filename and the file extension.
    //    Example:
    //        public static final String AWARD_SCHEDULE_TEMPLATE_FILE_PATH = "classpath:edu/cornell/kfs/cemi/module/cg/batch/Put_Award_Schedule.xlsx";
    //
    public static final String EXTRACTNAME_TEMPLATE_WORKBOOK_FILE_PATH = "classpath:edu/cornell/kfs/cemi/module/patterntemplate/batch/ACTUAL_EXTRACT_FILE_NAME.xlsx";
    

    
    // {EXTRACTNAME}ExtractSheets is a subclass that lists the names of all tabs/sheets in the spreadsheet.
    // PATTERN_TEMPLATE_SHEET1 should be a constant value matching the extact string value for every tab/sheet in the spreadsheet.
    //     Actual example for Supplier.xlsx would be:
    //
    //         public static final class SupplierExtractSheets {
    //             public static final String SUPPLIER = "Supplier";
    //             public static final String ADDRESSES = "Addresses";
    //             public static final String EMAILS = "Emails";
    //             public static final String PHONES = "Phones";
    //             public static final String BANK_ACCOUNTS = "Bank_Accounts";
    //             public static final String CHILDREN = "Children";
    //         }
    //
    public static final class EXTRACTNAMEExtractSheets {
        public static final String PATTERN_TEMPLATE_SHEET1 = "Pattern_Template_Sheet1";
    }
    
    // Pattern constants:
    // These items are purposely being left so they will not compile to force configuring each. 
    
    // This value SHOULD match what is being substituted for term {EXTRACTNAME} when this
    // set of template files is used to create a specific CEMI data extraction batch job.
    // Actual examples for this value would be:
    //      AwardSchedule
    //      OrderFromSupplier
    //      PaymentElection
    //      RemitToSupplier
    //      Supplier
    public static final String CEMI_DATA_EXTRACT_NAME = "EXTRACTNAME";
    
    public static final String CEMI_IN_SCOPE_BUSINESS_OBJECT_NAME = "IN-SCOPE-BUSINESS-OBJECT-NAME";
}
