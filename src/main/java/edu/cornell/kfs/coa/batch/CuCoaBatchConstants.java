package edu.cornell.kfs.coa.batch;

import java.sql.Date;

public class CuCoaBatchConstants {
    
    public static final String CSV_FILE_EXTENSION = ".csv";
    
    public static class ClosedLaborAccountsFileCreationConstants {
    
        public static final int PARAMETER_LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET = -1;
        public static final int PARAMETER_SET_TO_CREATE_FULL_SEED_FILE = 0;
        
        public static final Date SEED_FILE_DEFAULT_FROM_DATE = Date.valueOf("2013-03-01");
                
        public static final String FROM_DATE = "FROM_DATE";
        public static final String TO_DATE = "TO_DATE";
        
        public static final String FILE_DATA_CONTENT_TYPE_IS = "FILE_DATA_CONTENT_TYPE_IS";
        
        public static class FILE_DATA_CONTENT_TYPES {
            public static final int NO_PARMETER_FOUND = PARAMETER_LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET;
            public static final int SEED = PARAMETER_SET_TO_CREATE_FULL_SEED_FILE;
            public static final int RANGE = 1;
        }
        
        public static final String OUTPUT_FILE_NAME = "kfsClosedLaborAccounts";
    }
}
