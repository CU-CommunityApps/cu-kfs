package  edu.cornell.kfs.module.purap;

public class CUPurapParameterConstants {
    public static final String PURAP_PREQ_PAY_DATE_VARIANCE = "NUMBER_OF_DAYS_TO_DECREASE_PAY_DATE_BY";
	
	public static final String B2B_TOTAL_AMOUNT_FOR_AUTO_PO = "B2B_TOTAL_AMOUNT_FOR_AUTO_PO";
	
	public static final String APO_CONTRACT_MANAGER_EMAIL = "APO_CONTRACT_MANAGER_EMAIL";
	
	public static final String AUTO_CLOSE_PO_RESULTS_LIMIT = "AUTO_CLOSE_PO_RESULTS_LIMIT";
	
	public static final String MANUAL_DISTRIBUTION_EMAIL = "MANUAL_DISTRIBUTION_EMAIL";
	
    // KFSPTS-1625
	public static final String B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO = "B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO";

    //KFSUPGRADE-377
    public static final String PURAP_CR_PREQ_CANCEL_NOTE = "CR_CANCEL_NOTE";
    public static final String PURAP_CR_CM_CANCEL_NOTE = "CR_CANCEL_NOTE";

 // KFSPTS-1705
    public static final String PO_NOTIFY_EXCLUSIONS = "NOTIFY_REQUISITION_SOURCES";
    
    public static class ElectronicInvoiceParameters {
        public static final String SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL = "SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL";
        public static final String DEFAULT_PROCESSING_CAMPUS = "DEFAULT_PROCESSING_CAMPUS";
    }
    
    public static final String ROUTE_REQS_WITH_EXPIRED_CONTRACT_TO_CM = "ROUTE_REQS_WITH_EXPIRED_CONTRACT_TO_CM";

    public static final String DEFAULT_PURCHASE_ORDER_POS_APRVL_LMT_FOR_PREQ = "DEFAULT_PURCHASE_ORDER_POS_APRVL_LMT_FOR_PREQ";

    public static final String AUTOMATIC_FEDERAL_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT = "AUTOMATIC_FEDERAL_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT";
    public static final String CONTRACTING_SOURCES_EXCLUDED_FROM_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT
            = "CONTRACTING_SOURCES_EXCLUDED_FROM_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT";
    public static final String CONTRACTING_SOURCES_ALLOWED_OVERRIDE_OF_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT
            = "CONTRACTING_SOURCES_ALLOWED_OVERRIDE_OF_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT";
    
    public static final String JAGGAER_UPLOAD_PROCESSING_MODE = "JAGGAER_UPLOAD_PROCESSING_MODE";
    public static final String JAGGAER_UPLOAD_PO_DATE = "JAGGAER_UPLOAD_PO_DATE";
    public static final String JAGGAER_UPLOAD_VENDOR_DATE = "JAGGAER_UPLOAD_VENDOR_DATE";
    public static final String JAGGAER_MAX_NUMBER_OF_VENDORS_PER_XML_FILE = "JAGGAER_MAX_NUMBER_OF_VENDORS_PER_XML_FILE";
    public static final String JAGGAER_DEFAULT_SUPPLIER_ADDRESS_NOTE_TEXT = "JAGGAER_DEFAULT_SUPPLIER_ADDRESS_NOTE_TEXT";
    public static final String JAGGAER_DEFAULT_SUPPLIER_OUTPUT_FILE_NAME_STARTER = "JAGGAER_DEFAULT_SUPPLIER_OUTPUT_FILE_NAME_STARTER";
    public static final String JAGGAER_UPLOAD_SUPPLIERS_VERSION_NUMBER_TAG = "JAGGAER_UPLOAD_SUPPLIERS_VERSION_NUMBER_TAG";
    public static final String JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG = "JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG";
    public static final String JAGGAER_ENABLE_UPLOAD_FILES = "JAGGAER_ENABLE_UPLOAD_FILES";
    public static final String JAGGAER_UPLOAD_RETRY_COUNT = "JAGGAER_UPLOAD_RETRY_COUNT";
    public static final String JAGGAER_UPLOAD_ENDPOINT = "JAGGAER_UPLOAD_ENDPOINT";
    public static final String JAGGAER_XML_REPORT_EMAIL = "JAGGAER_XML_REPORT_EMAIL";
    
    public static final String JAGGAER_WEBSERVICE_GROUP_CODE = "JAGGAER";
    public static final String JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_NAME = "UPLOAD_SUPPLIER_NAME";
    public static final String JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_PASSWORD = "UPLOAD_SUPPLIER_PASSWORD";
    
    public static final String MAX_FILE_SIZE_PO_SEND_TO_VENDOR = "MAX_FILE_SIZE_PO_SEND_TO_VENDOR";
    
    public static final String ENABLE_IWANT_CONTRACT_TAB_IND = "ENABLE_IWANT_CONTRACT_TAB_IND";
}
