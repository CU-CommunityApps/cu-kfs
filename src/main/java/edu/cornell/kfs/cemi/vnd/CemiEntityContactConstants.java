package edu.cornell.kfs.cemi.vnd;

public final class CemiEntityContactConstants {

    public static final String ENTITY_CONTACT_OUTPUT_DEFINITION_PATH_SUFFIX = "vnd/batch/CemiEntityContactExtractFileOutputDefinition.xml";
    public static final String ENTITY_CONTACT_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "vnd/batch/Business_Entity_Contact_Supplier.xlsx";
    public static final String ENTITY_CONTACT_EXTRACT_FILENAME_PREFIX = "Business_Entity_Contact_Supplier_ITH_";
    public static final String ENTITY_CONTACT_EXTRACT_PLAIN_FILENAME = "Business_Entity_Contact_Supplier.xlsx";
 
    public static final int MAX_TENANTED_TYPES = 10;
    public static final int USA_CANADA_PHONE_CODE = 1;
    public static final int USA_CANADA_AREA_CODE_LENGTH = 3;
    public static final int USA_CANADA_PHONE_LENGTH = 10;
    public static final String ROW_ID_1 = "1";
    public static final String PHONE_PARSE_ERROR_MESSAGE = "Error_Parsing_Phone";

    public static final class EntityContactExtractSheets {
        public static final String BUSINESS_ENTITY_CONTACT = "Business Entity Contact";
    }

    public static final class PhoneDeviceTypes {
        public static final String LANDLINE = "Landline";
        public static final String MOBILE = "Mobile";
    }

    public static final class CommunicationUsageTypes {
        public static final String WORK = "WORK";
    }

}
