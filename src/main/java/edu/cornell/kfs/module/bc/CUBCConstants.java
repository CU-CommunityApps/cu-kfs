package edu.cornell.kfs.module.bc;

import org.kuali.kfs.module.bc.BCConstants;

public class CUBCConstants extends BCConstants {
    public static final String DEFAULT_FINANCIAL_SUB_OBJECT_CODE = "---";
    public static final String DEFAULT_SUB_ACCOUNT_NUMBER = "-----";
    public static final String POSITION_NUMBER_PREFIX = "00";

    public enum PSEntryStatus {
        ADD, DELETE, UPDATE
    }
    
    public enum StatusFlag {
        NEW("N"), CHANGED("C"), DELETED("D");

        private StatusFlag(String flagValue) {
            this.flagValue = flagValue;
        }

        public String getFlagValue() {
            return flagValue;
        }

        private String flagValue;
    }
    
    

}
