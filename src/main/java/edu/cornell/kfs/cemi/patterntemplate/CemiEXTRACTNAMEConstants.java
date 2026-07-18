package edu.cornell.kfs.cemi.patterntemplate;

public final class CemiEXTRACTNAMEConstants {
    
    // This section would hold the format definitions that a key or id should have in the data extraction.
    // These formats are defined in the Huron mapping sessions and would contain some form of static string
    // along with an actual legacy key value. The Java MessageFormat class would utilize these constants 
    // in the  following manner when performing legacy to Workday data value conversions that would be 
    // contained in the business object factory:
    //      workdayStringValue = MessageFormat.format(Cemi{EXTRACTNAME}Constants.WORKDAY_OBJECT_REFERENCE_ID_FORMAT, legacyObjectSequenceNumber);
    //
    //    Examples of the constantt definitions:
    //        public static final String SPREADSHEET_KEY_FORMAT = "AS_ITH_{0}";
    //        public static final String AWARD_SCHEDULE_REFERENCE_ID_FORMAT = "AS_ITH_{0}";
    //        public static final String AWARD_PERIOD_REFERENCE_ID_FORMAT = "AS_ITH_{0}_Period";
    //        public static final String AWARD_POSTING_INTERVAL_ID_FORMAT = "AS_ITH_{0}_PSTINT";
    //    Examples of how those constants would be used:
    //        MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    //        MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);
    
    
    // This section would have any constant value defined by Huron during the
    // mapping sessions that should be used in place of a legacy system data value.
    // All of these values should be defined as Java static Strings.
    //    Examples:
    //        public static final String AWARD_PERIOD = "Award_Period";
    //        public static final String BUDGET_PERIOD = "Budget_Period";
    //        public static final String CINV_PERIOD = "CNV Period";
    //        public static final String NUMERIC_ONE = "1";
    
    
    // This XML file defines the association between the Huron provided xlsx Excel spreadsheet and the business
    // object used to gather the data. The XML file should be placed in the resources folder. The definition here
    // is a partial path to that location starting from the first folder AFTER "cemi" where that output template 
    // definition is located. When creating this constant, DO NOT include a forward slash in this definition.
    //
    // The partial file path in this constant should start at the first folder after the directory path: "edu/cornell/kfs/cemi/"
    // This partial file path is then used with a base constant in an abstract class to construct the full file path
    // to the XML output definintion. 
    //    Example:
    //        public static final String AWARD_SCHEDULE_OUTPUT_DEFINITION_PATH_SUFFIX = "module/cg/batch/CemiAwardScheduleExtractFileOutputDefinition.xml";
    //
    // Full path definition pertaining to Award Schedule output defintion file is below for comparison to show
    // how this parameter would need to be configured.
    //    (1) Abstract class CemiDataExtractServiceBase contains abstrct method:
    //              protected abstract String getOutputDefinitionFilePathSuffix();
    //    (2) Concrete class CemiAwardScheduleExtractServiceImpl returns constant 
    //              CemiAwardScheduleConstants.AWARD_SCHEDULE_OUTPUT_DEFINITION_PATH_SUFFIX
    //    (3) A method within abstract classs CemiDataExtractServiceBase then makes utility class call 
    //        CemiUtils.getOutputDefinitionFromCemiResourcesFile to generate the full path of:
    //              classpath:edu/cornell/kfs/cemi/module/cg/batch/CemiAwardScheduleExtractFileOutputDefinition.xml
    public static final String EXTRACTNAME_OUTPUT_DEFINITION_PATH_SUFFIX = "module/patterntemplate/batch/CemiEXTRACTNAMEExtractFileOutputDefinition.xml";
    
    // Where to find the empty mapping template version of the Excel spreadsheet provided by Huron to populate.
    // The definition should include both the filename and the file extension. The XLSX file should be placed in
    // the resources folder. The definition here is a partial path to that location stating from the starting from
    // the first folder AFTER "cemi" where that XLSX file is located. When creating this constant, DO NOT include 
    // a forward slash in this definition.
    //     Example:
    //        public static final String AWARD_SCHEDULE_TEMPLATE_FILE_PATH = "classpath:edu/cornell/kfs/cemi/module/cg/batch/Put_Award_Schedule.xlsx";
    //
    // Full path definition pertaining to Award Schedule mapping template XLSX to populate is below for comparison to 
    // show how this parameter would need to be configured.
    //  (1) Abstract class CemiDataExtractServiceBase contains abstrct method:
    //            protected abstract String getTemplateWorkbookFilePathSuffix();
    //  (2) Concrete class CemiAwardScheduleExtractServiceImpl returns constant 
    //            CemiAwardScheduleConstants.AWARD_SCHEDULE_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX
    //  (3) A method within abstract classs CemiDataExtractServiceBase then makes utility class call 
    //      CemiUtils.getTemplateWorkbookFullFilePath to generate the full path of:
    //            classpath:edu/cornell/kfs/cemi/module/cg/batch/Put_Award_Schedule.xlsx
     public static final String EXTRACTNAME_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "module/patterntemplate/batch/ACTUAL_EXTRACT_FILE_NAME.xlsx";
    
    
     // Partial file name without file extension used for the spreadsheet file as it is being populated.
     //     (1) Constant name should have the format: {EXTRACTNAME}_EXTRACT_FILENAME_PREFIX
     //     (2) Constant value should have the format: "{ActualXlsxFilename}_ITH_";
     //
     // This name should be in the format of {EXTRACTNAME}_EXTRACT_FILENAME with _ITH appended to it.
     //    Example:
     //        public static final String AWARD_SCHEDULE_EXTRACT_FILENAME_PREFIX = "Put_Award_Schedule_ITH_";
     //
     public static final String EXTRACTNAME_EXTRACT_FILENAME_PREFIX = "ActualXlsxFilenme_ITH_";
     
     
     // Actual file name including file extension for the data extraction spreadsheet file that will be
     // transmitted to the validation server.  This file name with extension should exactly match what 
     // Huron has specified in their mapping templates to populate Sharepoint folder. 
     //     (1) Constant name should have the format: {EXTRACTNAME}_EXTRACT_PLAIN_FILENAME
     //     (2) Constant value should have the format: "{ActualXlsxFilename}.xlsx";
     //    Example: 
     //        public static final String AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME = "Put_Award_Schedule.xlsx";
     //
     public static final String EXTRACTNAME_EXTRACT_PLAIN_FILENAME = "ACTUAL_EXTRACT_FILE_NAME.xlsx";
     
    
    // {EXTRACTNAME}ExtractSheets is a subclass that lists the names of all tabs/sheets in the spreadsheet.
    // PATTERN_TEMPLATE_SHEET1 should be a constant value matching the exact string value for every tab/sheet in the spreadsheet.
    //     Actual example for multi-tabbed spreadsheet Supplier.xlsx would be:
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
  
}
