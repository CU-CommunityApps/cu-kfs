package edu.cornell.kfs.coa.batch;

import edu.cornell.kfs.fp.CuFPConstants;

public class CuCoaBatchConstants {
    
    public static class ClosedAccountsFileCreationConstants {
        
        public static final int PARAMETER_CLOSED_ACCOUNTS_SEED_FILE_DEFAULT_FROM_DATE_NOT_SET = -2;
        public static final int PARAMETER_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET = -1;
        public static final int PARAMETER_SET_TO_CREATE_FULL_SEED_FILE = 0;
                
        public static final String FROM_DATE = "FROM_DATE";
        public static final String TO_DATE = "TO_DATE";
        
        public static final String FILE_DATA_CONTENT_TYPE_IS = "FILE_DATA_CONTENT_TYPE_IS";
        
        public static class FILE_DATA_CONTENT_TYPES {
            public static final int NO_PARAMETER_FOUND = PARAMETER_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET;
            public static final int SEED = PARAMETER_SET_TO_CREATE_FULL_SEED_FILE;
            public static final int RANGE = 1;
        }
        
        public static final String OUTPUT_FILE_NAME = "kfsClosedAccounts";
    }
    
    public static class WorkdayOpenAccountsFileCreationConstants {
        public static final String OUTPUT_FILE_NAME = "kfsOpenAccountsSubaccountsSubobjectcodes";
    }
    
    // KFSPTS-34678:
    // Field ACCOUNT_CFDA_NUMBER was added after initial coding was performed.
    // That data element was not placed with the rest of the account table attributes per specific customer request.
    public enum WorkdayOpenAccountDetailDTOCsvColumn {
        CHART("chart"),
        ACCOUNT_NUMBER("accountNumber"),
        ACCOUNT_NAME("accountName"),
        SUB_FUND_GROUP_WAGE_IND("subFundGroupWageIndicator"),
        SUB_FUND_GROUP_CODE("subFundGroupCode"),
        HIGHER_ED_FUNCTION_CODE("higherEdFunctionCode"),
        ACCOUNT_EFFECTIVE_DATE("accountEffectiveDate"),
        ACCOUNT_CLOSED_IND("accountClosedIndicator"),
        ACCOUNT_TYPE_CODE("accountTypeCode"),
        SUB_ACCOUNT_NUMBER("subAccountNumber"),
        SUB_ACCOUNT_NAME("subAccountName"),
        SUB_ACCOUNT_NUMBER_ACTIVE_IND("subAccountActiveIndicator"),
        OBJECT_CODE("objectCode"),
        SUB_OBJECT_CODE("subObjectCode"),
        SUB_OBJECT_NAME("subObjectName"),
        ACCOUNT_CFDA_NUMBER("accountCfdaNumber");
        
        public final String headerLabel;
        public final String workdayOpenAccountDetailPropertyName;
        
        private WorkdayOpenAccountDetailDTOCsvColumn(String headerLabel) {
            this(headerLabel, headerLabel);
        }
        
        private WorkdayOpenAccountDetailDTOCsvColumn(String headerLabel, String workdayOpenAccountDetailPropertyName) {
            this.headerLabel = headerLabel;
            this.workdayOpenAccountDetailPropertyName = workdayOpenAccountDetailPropertyName;
        }
        
        public String getHeaderLabel() {
            return headerLabel;
        }

        public String getWorkdayOpenAccountDetailPropertyName() {
            return workdayOpenAccountDetailPropertyName;
        }
        
    }

    public static final String DFA_ATTACHMENTS_GROUP_CODE = "DFAATTACH";
    public static final String DFA_ATTACHMENTS_URL_KEY = CuFPConstants.CREDENTIAL_BASE_URL + "1";

}
