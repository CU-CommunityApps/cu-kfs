package edu.cornell.kfs.coa.batch;

import edu.cornell.kfs.fp.CuFPConstants;

public class CuCoaBatchConstants {
    
    public static class WorkdayOpenAccountsFileCreationConstants {
        public static final String OUTPUT_FILE_NAME = "kfsOpenAccountsSubaccountsSubobjectcodes";
    }
    
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
        SUB_OBJECT_NAME("subObjectName");
        
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
