package edu.cornell.kfs.cemi.vnd;

public final class CemiEntityContactConstants {

    // This section would hold the format definitions that a key or id should have in the data extraction.
    // These formats are defined in the Huron mapping sessions and would contain some form of static string
    // along with an actual legacy key value. The Java MessageFormat class would utilize these constants 
    // in the  following manner when performing legacy to Workday data value conversions that would be 
    // contained in the business object factory:
    //      workdayStringValue = MessageFormat.format(Cemi{EXTRACTNAME}Constants.WORKDAY_OBJECT_REFERENCE_ID_FORMAT, legacyObjectSequenceNumber);
    //
    //    Examples of the constant definitions:
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

    public static final String ENTITY_CONTACT_OUTPUT_DEFINITION_PATH_SUFFIX = "vnd/batch/CemiEntityContactExtractFileOutputDefinition.xml";
    public static final String ENTITY_CONTACT_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "vnd/batch/Business_Entity_Contact_Supplier.xlsx";
    public static final String ENTITY_CONTACT_EXTRACT_FILENAME_PREFIX = "Business_Entity_Contact_Supplier_ITH_";
    public static final String ENTITY_CONTACT_EXTRACT_PLAIN_FILENAME = "Business_Entity_Contact_Supplier.xlsx";
 
    public static final int US_AREA_CODE_LENGTH = 3;
    public static final int MAX_TENANTED_TYPES = 10;
    public static final String ROW_ID_1 = "1";
    public static final String PHONE_PARSE_ERROR_MESSAGE = "Error_Parsing_Phone";

    public static final class EntityContactExtractSheets {
        public static final String BUSINESS_ENTITY_CONTACT = "Business Entity Contact";
    }

    public static final class PhoneDeviceTypes {
        public static final String TELEPHONE = "Telephone";
    }

    public static final class CommunicationUsageTypes {
        public static final String WORK = "WORK";
    }

}
