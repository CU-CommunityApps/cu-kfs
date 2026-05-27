package edu.cornell.kfs.cemi.vnd;

public final class CemiRemitToSupplierConstants {

    public static final String REMIT_TO_SUPPLIER_SHEET_NAME = "Remit_To_Supplier";
    public static final String REMIT_TO_SUPPLIER_EXTRACT_FILENAME_PREFIX = "Remit_To_Supplier_ITH_";
    public static final String REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME = "Remit_To_Supplier.xlsx";
    public static final String REMIT_TO_SUPPLIER_OUTPUT_DEFINITION_PATH_SUFFIX =
            "vnd/batch/CemiRemitToSupplierExtractFileOutputDefinition.xml";
    public static final String REMIT_TO_SUPPLIER_TEMPLATE_FILE_PATH =
            "classpath:edu/cornell/kfs/cemi/vnd/batch/Remit_To_Supplier.xlsx";

    public static final String SUPPLIER_REMIT_TO_CONNECTION_NAME_FORMAT = "{0} | {1}";
    public static final String SUPPLIER_REMIT_TO_CONNECTION_ID_FORMAT = "{0}_{1}_{2}";

    public static final String ITHACA_PAYMENT_MEMO = "Ithaca";

}
