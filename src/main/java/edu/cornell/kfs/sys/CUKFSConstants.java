package edu.cornell.kfs.sys;

public class CUKFSConstants {
        
        public static final String COMMODITY_CODE_FILE_TYPE_INDENTIFIER = "commodityCodeInputFileType";
        
        public static class PreEncumbranceDocumentConstants {
                public static final String BIWEEKLY = "biWeekly";
                public static final String CUSTOM = "custom";
                public static final String SEMIMONTHLY = "semiMonthly";
                public static final String MONTHLY = "monthly";
        }
        
        public static class PreEncumbranceSourceAccountingLineConstants {
                public static final String END_DATE = "endDate";
                public static final String START_DATE = "startDate";
                public static final String AUTO_DISENCUMBER_TYPE = "autoDisEncumberType";
                public static final String PARTIAL_TRANSACTION_COUNT = "partialTransactionCount";
                public static final String PARTIAL_AMOUNT = "partialAmount";
        }
        
        // I Want document constants
        public static final String I_WANT_DOC_ITEM_TAB_ERRORS = "document.item*,newIWantItemLine*";
        public static final String I_WANT_DOC_ACCOUNT_TAB_ERRORS = "newSourceLine*,document.account*";
        public static final String I_WANT_DOC_VENDOR_TAB_ERRORS = "document.vendor*";
        public static final String I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS = "document.completeOption";
        
        //KFSPTS-1460
        public static final String SEMICOLON = ";";
    
        
}
