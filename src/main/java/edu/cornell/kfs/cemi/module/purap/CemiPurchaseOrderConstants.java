package edu.cornell.kfs.cemi.module.purap;

public final class CemiPurchaseOrderConstants {
    
    public static final int MAX_PURCHASE_ORDER_PRELOAD_BATCH_SIZE = 50;

    public static final String PURCHASE_ORDER_AMOUNT_FORMAT = "#########################0.00####";
    public static final String PURCHASE_ORDER_SPLIT_AMOUNT_FORMAT = "#################0.00#";
    public static final String PURCHASE_ORDER_QUANTITY_FORMAT = "#####################0.##";

    public static final String PURCHASE_ORDER_OUTPUT_DEFINITION_PATH_SUFFIX = "module/purap/batch/CemiPurchaseOrderExtractFileOutputDefinition.xml";
    
    public static final String PURCHASE_ORDER_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "module/purap/batch/Purchase_Order.xlsx";

    public static final String PURCHASE_ORDER_EXTRACT_FILENAME_PREFIX = "Purchase_Order_ITH_";
     
    public static final String PURCHASE_ORDER_EXTRACT_PLAIN_FILENAME = "Purchase_Order.xlsx";

    public static final String PO_ISSUE_OPTION_PRINT = "PRINT";

    public static final String REQUESTOR_LABEL = "Requestor";
    public static final String DELIVERY_RECIPIENT_LABEL = "Delivery Recipient";

    public static final String DEFAULT_BUYER_PRINCIPAL_NAME = "mls398";

    public static final String ROOM_NUMBER_SEGMENT_PREFIX = "Room #";
    public static final String ORIGINAL_PO_AMOUNT_MEMO_PREFIX = "Original PO Amount: ";
    public static final String ORIGINAL_PO_LINE_AMOUNT_MEMO_PREFIX = "Original PO Line Amount: ";

    public static final String LEGACY_PO_CONVERSION_LABEL = "Legacy PO Conversion";

    public static final String CATALOG_NUMBER_NONE = "none";

    public static final class PurchaseOrderExtractSheets {
        public static final String SUBMIT_PURCHASE_ORDER = "Submit Purchase Order";
    }

    public static final class PurchaseOrderExtractColumnNames {
        public static final String FDOC_NBR = "FDOC_NBR";
        public static final String DOC_TYP_NM = "DOC_TYP_NM";
    }

}
