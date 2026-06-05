package edu.cornell.kfs.cemi.vnd;

public final class CemiOrderFromSupplierConstants {

    public static final String ORDER_FROM_SUPPLIER_EXTRACT_FILENAME_PREFIX = "Submit_Order_From_Supplier_Connection_ITH_";
    public static final String ORDER_FROM_SUPPLIER_EXTRACT_PLAIN_FILENAME = "Submit_Order_From_Supplier_Connection.xlsx";
    public static final String ORDER_FROM_SUPPLIER_OUTPUT_DEFINITION_PATH_SUFFIX =
            "vnd/batch/CemiOrderFromSupplierExtractFileOutputDefinition.xml";
    public static final String ORDER_FROM_SUPPLIER_TEMPLATE_FILE_PATH =
            "classpath:edu/cornell/kfs/cemi/vnd/batch/Submit_Order_From_Supplier_Connection.xlsx";

    public static final String ORDER_FROM_CONNECTION_NAME_CATALOG = "Catalog";
    public static final String ORDER_FROM_CONNECTION_NAME_EMAIL_PREFIX = "Send PO to: ";

    public static final String PO_ISSUE_OPTION_XML_AUTO = "XML Auto";
    public static final String PO_ISSUE_OPTION_EMAIL = "Email";

    public static final class DefaultPOTypes {
        public static final String CATALOG = "Catalog";
        public static final String STANDARD = "Standard";
        public static final String BLANKET_ORDER = "Blanket_Order";
        public static final String SOLE_SOURCE = "Sole_Source";
    }

}
