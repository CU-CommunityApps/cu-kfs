package edu.cornell.kfs.coa.batch;

import java.util.function.BiConsumer;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
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
    public static final String DFA_ATTACHMENTS_API_KEY = "x-api-key";

    public enum LegacyAccountAttachmentProperty {
        ID("COPYING_ACCT_ATTACH_ID", LegacyAccountAttachment::setId),
        LEGACY_ACCOUNT_CODE("ORIGINAL_ACCOUNT_CODE", LegacyAccountAttachment::setLegacyAccountCode),
        KFS_CHART_CODE("KFS_CHART_CODE", LegacyAccountAttachment::setKfsChartCode),
        KFS_ACCOUNT_NUMBER("KFS_ACCOUNT_NUMBER", LegacyAccountAttachment::setKfsAccountNumber),
        FILE_NAME("FILE_NAME", LegacyAccountAttachment::setFileName),
        ADDED_BY("ADDED_BY", LegacyAccountAttachment::setAddedBy),
        FILE_DESCRIPTION("FILE_DESCRIPTION", LegacyAccountAttachment::setFileDescription),
        FILE_PATH("FILE_SYSTEM_FILE_NAME", LegacyAccountAttachment::setFilePath),
        RETRY_COUNT("RETRY_COUNT", LegacyAccountAttachment::setRetryCount),
        IS_COPIED("COPIED_IND", LegacyAccountAttachment::setCopied);

        public final String columnName;
        public final BiConsumer<LegacyAccountAttachment, String> propertySetter;

        private LegacyAccountAttachmentProperty(final String columnName,
                final BiConsumer<LegacyAccountAttachment, String> propertySetter) {
            this.columnName = columnName;
            this.propertySetter = propertySetter;
        }

        public String getColumnName() {
            return columnName;
        }

        public BiConsumer<LegacyAccountAttachment, String> getPropertySetter() {
            return propertySetter;
        }
    }

}
