package edu.cornell.kfs.sys;

import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * Property name constants.
 */
public class CUKFSPropertyConstants {

    // KFSUPGRADE-779
    public static final String DOC_HDR_FINANCIAL_DOCUMENT_STATUS_CODE = "documentHeader.financialDocumentStatusCode";
    
    public static final String PROGRAM_CODE = "programCode";
    public static final String APPROPRIATION_ACCT_NUMBER = "appropriationAccountNumber";

    public static final String ACCT_REVERSION_CHART_OF_ACCT_CODE = "chartOfAccountsCode";
    public static final String ACCT_REVERSION_BUDGET_REVERSION_CHART_OF_ACCT_CODE = "budgetReversionChartOfAccountsCode";
    public static final String ACCT_REVERSION_CASH_REVERSION_CHART_OF_ACCT_CODE = "cashReversionFinancialChartOfAccountsCode";
    public static final String ACCT_REVERSION_ACCT_NUMBER = "accountNumber";
    public static final String ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER = "budgetReversionAccountNumber";
    public static final String ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER = "cashReversionAccountNumber";
    public static final String ACCT_REVERSION_CATEGORY_CODE = "accountReversionCategoryCode";
    public static final String ACCT_REVERSION_ACTIVE = "active";
    
    public static final String SUB_OBJ_CODE_EDIT_CHANGE_DETAILS = "subObjCdGlobalEditDetails";
    public static final String SUB_ACCOUNT_GLBL_CHANGE_DETAILS = "subAccountGlobalDetails";
    public static final String SUB_ACCOUNT_GLBL_NEW_DETAILS = "subAccountGlobalNewAccountDetails";

    public static final String DOCUMENT_FAVORITE_ACCOUNT_LINE_IDENTIFIER = "document.favoriteAccountLineIdentifier";

    public static final String AWARD_EXTENSION_BUDGET_ENDING_DATE = "extension.budgetEndingDate";
    
    public static final String RECURRING_DV_PARTIAL_TRANSACTION_COUNT_FIELD_NAME = "partialTransactionCount";

    public static final String CONTRACTS_AND_GRANTS_ACCOUNT_RESPOSIBILITY_ID = "contractsAndGrantsAccountResponsibilityId";
    public static final String REMOVE_INCOME_STREAM_CHART_AND_ACCOUNT = "removeIncomeStreamChartAndAccount";
    public static final String REMOVE_CONTINUATION_CHART_AND_ACCOUNT = "removeContinuationChartAndAccount";
    public static final String RECURRING_DV_PAYMENT_CANCEL_REASON_FIELD_NAME = "paymentCancelReason";

    public static final String WEB_SERVICE_CREDENTIAL_GROUP_CODE = "credentialGroupCode";
    public static final String WEB_SERVICE_CREDENTIAL_KEY = "credentialKey";

    public static final String PATH = "path";
    public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
    
    public static final String ACCOUNT_GLOBAL_ACCOUNT_SECTION_FIELD_NAME_FORMAT = "accountGlobalDetails[{0}]";
    
    public static final String INACTIVATE = "inactivate";
    public static final String NEW_SUB_ACCOUNT_TYPE_CODE = "newSubAccountTypeCode";
    public static final String NEW_SUB_ACCOUNT_NUMBER = "newSubAccountNumber";
    public static final String NEW_SUB_ACCOUNT_NAME = "newSubAccountName";
    public static final String NEW_SUB_ACCOUNT_OFF_CAMPUS_CODE = "newSubAccountOffCampusCode";
    public static final String APPLY_TO_ALL_NEW_SUB_ACCOUNTS = "applyToAllNewSubAccounts";
    public static final String SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS = "subAccountGlobalNewAccountDetails";
    public static final String A21_COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE
            = KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE;
    public static final String A21_COST_SHARE_SOURCE_ACCOUNT_NUMBER
            = KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER;
    public static final String A21_INDIRECT_COST_RECOVERY_TYPE_CODE
            = KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE_CODE;
    
    public static final String LOOKUP_RESULT_ACTION_LABEL = "label";
    public static final String LOOKUP_RESULT_ACTION_URL = "url";
    public static final String LOOKUP_RESULT_ACTION_METHOD = "method";

    public static final String PROFILE_USER = "profileUser";
    public static final String PROFILE_USER_PRINCIPAL_NAME =
            PROFILE_USER + KFSConstants.DELIMITER + KIMPropertyConstants.Principal.PRINCIPAL_NAME;
    public static final String ACCOUNT_DELEGATE_PRINCIPAL_NAME = KFSPropertyConstants.ACCOUNT_DELEGATE
            + KFSConstants.DELIMITER + KIMPropertyConstants.Principal.PRINCIPAL_NAME;
    public static final String PROJECT_MANAGER_UNIVERSAL_PRINCIPAL_NAME =
            KFSPropertyConstants.PROJECT_MANAGER_UNIVERSAL + KFSConstants.DELIMITER
                    + KIMPropertyConstants.Principal.PRINCIPAL_NAME;

    public static final String UNMASKED_PROPERTY_SUFFIX = "Unmasked";

    public static final String DOCUMENT_ID = "documentId";
    public static final String FINALIZED_DATE = "finalizedDate";
    public static final String INITIATOR_WORKFLOW_ID = "initiatorWorkflowId";

    public static final String BUILDING_ROOM = "buildingRoom";

    //** CU Generic ISO-FIPS Country modification items **
    public static class Location {
        public static final String COUNTRY_CODE = "countryCode";
        public static final String ISO_COUNTRY_CODE = "isoCountryCode";
        public static final String FIPS_COUNTRY_CODE = "fipsCountryCode";
        public static final String COUNTRY_NAME = "countryName";
        public static final String ISO_COUNTRY_NAME = "isoCountryName";
    }
    public static class ISOFIPSCountryMap {
        public static final String ISO_COUNTRY_CODE = Location.ISO_COUNTRY_CODE;
        public static final String FIPS_COUNTRY_CODE = Location.FIPS_COUNTRY_CODE;
        public static final String ACTIVE = KFSPropertyConstants.ACTIVE;
    }
    private static final String NAME = "name";
    private static final String CODE = "code";
    private static final String ALTERNATE_CODE = "alternateCode";
    public static class ISOCountry {
        public static final String NAME = CUKFSPropertyConstants.NAME;
        public static final String CODE = CUKFSPropertyConstants.CODE;
        public static final String ALTERNATE_CODE = CUKFSPropertyConstants.ALTERNATE_CODE;
        public static final String ACTIVE = KFSPropertyConstants.ACTIVE;
    }
    public static class Country {
        public static final String NAME = CUKFSPropertyConstants.NAME;
        public static final String CODE = CUKFSPropertyConstants.CODE;
        public static final String ALTERNATE_CODE = CUKFSPropertyConstants.ALTERNATE_CODE;
        public static final String ACTIVE = KFSPropertyConstants.ACTIVE;
    }
    
    public static final String DESCRIPTION = "description";
    public static final String ADDITIONAL_SCRIPT_FILE_PREFIX = "additionalScriptFile";

    public static final String CU_SPRING_MVC_SOURCE_FILE_KEY = "cu.spring.mvc.source.file";
    public static final String KUALICO_SPRING_MVC_SOURCE_FILE_KEY = "kualico.spring.mvc.source.file";

    public static final String STATUS = "status";
    public static final String ERRORS = "errors";

    /*
     * The following constants are used by our code but were removed by the 2024-03-06 financials patch,
     * so we had to copy them here.
     */
    public static final String COST_SHARE_SOURCE_ACCOUNT = "costShareAccount";
    public static final String COST_SHARE_SOURCE_SUB_ACCOUNT = "costShareSourceSubAccount";
    public static final String CHART_OF_ACCOUNTS = "chartOfAccounts";
    public static final String BUILDING_NAME = "buildingName";
    public static final String AGENCY_TYPE = "agencyType";
    public static final String CHECK_NUMBER = "checkNumber";

}
