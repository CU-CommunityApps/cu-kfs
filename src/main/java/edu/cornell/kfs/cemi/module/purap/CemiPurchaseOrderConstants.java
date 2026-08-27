package edu.cornell.kfs.cemi.module.purap;

public final class CemiPurchaseOrderConstants {
    
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
