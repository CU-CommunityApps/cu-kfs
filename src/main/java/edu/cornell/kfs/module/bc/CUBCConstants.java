package edu.cornell.kfs.module.bc;

import org.kuali.kfs.module.bc.BCConstants;

public class CUBCConstants extends BCConstants {
    public static final String DEFAULT_FINANCIAL_SUB_OBJECT_CODE = "---";
    public static final String DEFAULT_SUB_ACCOUNT_NUMBER = "-----";
    public static final String POSITION_NUMBER_PREFIX = "00";
    
    // parameter constants
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE = "KFS-BC";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP = "PSBudgetFeedStep";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_VALUE = "N";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_ALLOWED = "A";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION = "Tells the psBudgetFeedJob if it should wipe out the entries in the BC and SIP related tables";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_TYPE = "CONFG";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE = "KFS";

    public enum PSEntryStatus {
        ADD, DELETE, UPDATE
    }
    
    public enum StatusFlag {
        NEW("N"), CHANGED("C"), DELETED("D"), ACTIVE("-");

        private StatusFlag(String flagValue) {
            this.flagValue = flagValue;
        }

        public String getFlagValue() {
            return flagValue;
        }

        private String flagValue;
    }
    
    

}
