package edu.cornell.kfs.sys;

import org.kuali.kfs.sys.ParameterKeyConstants;
import org.kuali.rice.core.util.JSTLConstants;

public class CUKFSConstants extends JSTLConstants implements ParameterKeyConstants {
        
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
        // KFSPTS-1960
        public static final String CURLY_BRACKET_LEFT = "{";
        public static final String CURLY_BRACKET_RIGHT = "}";
   
}
