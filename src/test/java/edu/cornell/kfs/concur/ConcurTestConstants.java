package edu.cornell.kfs.concur;

public class ConcurTestConstants {

    public static final String EMPLOYEE_GROUP_ID = "CORNELL";
    public static final String EMPLOYEE_DEFAULT_STATUS = "EMPLOYEE";
    public static final String UNRECOGNIZED_PAYMENT_CODE = "????";
    public static final String DEFAULT_REPORT_ACCOUNT = "1122334";
    public static final String REPORT_ID_1 = "ABCDEFGHIJ1234567890";
    public static final String REPORT_ID_2 = "JJJJJKKKKK5555566666";
    public static final String REPORT_ID_3 = "ZZYYXXVVWW7766554433";
    public static final String REPORT_ID_SHORT = "GGGG5555";
    public static final String CHART_IT = "IT";
    public static final String CHART_QQ = "QQ";
    public static final String OBJ_6200 = "6200";
    public static final String OBJ_7777 = "7777";
    public static final String OBJ_1414 = "1414";
    public static final String SUB_OBJ_333 = "333";
    public static final String SUB_OBJ_864 = "864";
    public static final String ACCT_1234321 = "1234321";
    public static final String ACCT_4455667 = "4455667";
    public static final String ACCT_XXXXXXX = "XXXXXXX";
    public static final String SUB_ACCT_55655 = "55655";
    public static final String SUB_ACCT_88888 = "88888";
    public static final String SUB_ACCT_13579 = "13579";
    public static final String SUB_ACCT_24680 = "24680";
    public static final String SUB_ACCT_00001 = "00001";
    public static final String PROJ_AA_778899 = "AA-778899";
    public static final String PROJ_QX_400000 = "QX-400000";
    public static final String ORG_REF_123ABC = "123ABC";
    public static final String ORG_REF_777JJJ = "777JJJ";
    public static final String JAN_04_2017 = "01/04/2017";
    public static final int FY_2017 = 2017;
    public static final String DEFAULT_POLICY_NAME = "Travel";
    public static final String DEFAULT_EXPENSE_TYPE_NAME = "Hotel";
    public static final String PERSONAL_CAR_MILEAGE_EXPENSE_TYPE = "Personal Car Mileage";
    public static final String TAXI_EXPENSE_TYPE = "Taxi";
    public static final String REPORT_ENTRY_ID_1 = "1234";
    public static final String REPORT_ENTRY_ID_2 = "1235";
    public static final String REPORT_ENTRY_ID_3 = "2234";
    public static final String CASH_ADVANCE_KEY_1 = "101";
    public static final String CASH_ADVANCE_KEY_2 = "102";
    public static final String CASH_ADVANCE_KEY_ATM_1 = "500";
    public static final String CASH_ADVANCE_KEY_NONEXISTENT = "999";
    public static final String REQUEST_ID_1 = "A1B2C3D4E5F6G7H8I9J0";
    public static final String REQUEST_ID_2 = "ZZ11YY22XX33WW44VV55";
    public static final String SOURCE_DOC_NUMBER_1 = "10987654";
    public static final String TEST_FILE_NAME = "testFile01.txt";
    public static final String DELIMITED_PAYMENT_TYPE = "User|Or|University";

    public static final String DASH_SUB_ACCOUNT_NUMBER = "-----";
    public static final String DASH_SUB_OBJECT_CODE = "---";
    public static final String DASH_PROJECT_CODE = "----------";

    public static final String CURRENCY_USD = "USD";
    public static final String REQUEST_TRAVEL_TYPE_CODE = "TRAVEL";
    public static final String REQUEST_TRAVEL_TYPE_LABEL = "Travel";
    public static final String REQUEST_EXCEPTION_LEVEL_NONE = "NONE";

    public static class PropertyTestValues {
        public static final String ORPHANED_CASH_ADVANCE_MESSAGE = "Cash Advance with key {0} had no matching Request Extract entry.";
        public static final String GROUP_WITH_ORPHANED_CASH_ADVANCE_MESSAGE = "Line not processed due to orphaned Cash Advance with same Report ID.";
        public static final String REQUESTV4_REQUEST_LIST_SEARCH_MESSAGE = "Travel Request Listing (page {0})";
        public static final String REQUESTV4_SINGLE_REQUEST_SEARCH_MESSAGE = "Travel Request {0}";
    }

    public static final class RequestV4Dates {
        public static final String DATE_2022_01_01 = "01/01/2022 11:22:33";
        public static final String DATE_2022_01_02 = "01/02/2022 11:22:33";
        public static final String DATE_2022_01_03 = "01/03/2022 11:22:33";
        public static final String DATE_2022_04_05 = "04/05/2022 11:22:33";
        public static final String DATE_2022_04_06 = "04/06/2022 11:22:33";
        public static final String DATE_2022_04_07 = "04/07/2022 11:22:33";
        public static final String DATE_2022_04_30 = "04/30/2022 11:22:33";
    }

    public static class ParameterTestValues {
        public static final String OBJECT_CODE_OVERRIDE = "6750";
        public static final String COLLECTOR_DOCUMENT_TYPE = "CLTE";
        public static final String COLLECTOR_SYSTEM_ORIGINATION_CODE = "CN";
        public static final String COLLECTOR_CHART_CODE = "IT";
        public static final String COLLECTOR_HIGHEST_LEVEL_ORG_CODE = "6101";
        public static final String COLLECTOR_DEPARTMENT_NAME = "Fin Sys Admin & Info Delivery";
        public static final String COLLECTOR_CAMPUS_CODE = "NY";
        public static final String COLLECTOR_CAMPUS_ADDRESS = "101 Main St";
        public static final String COLLECTOR_NOTIFICATION_CONTACT_EMAIL = "saecollector@somedomain.com";
        public static final String COLLECTOR_NOTIFICATION_CONTACT_PERSON = "James Smith";
        public static final String COLLECTOR_NOTIFICATION_CONTACT_PHONE = "1112223333";
        public static final String COLLECTOR_PREPAID_OFFSET_CHART_CODE = "PQ";
        public static final String COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER = "7171717";
        public static final String COLLECTOR_PREPAID_OFFSET_OBJECT_CODE = "7711";
        public static final String COLLECTOR_PAYMENT_OFFSET_OBJECT_CODE = "8282";
        public static final String COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE = "1444";
        public static final String COLLECTOR_ATM_FEE_DEBIT_CHART_CODE = "TI";
        public static final String COLLECTOR_ATM_FEE_DEBIT_ACCOUNT_NUMBER = "G333555";
        public static final String COLLECTOR_ATM_FEE_DEBIT_OBJECT_CODE = "6699";
        public static final String COLLECTOR_UNUSED_ATM_OFFSET_OBJECT_CODE = "1488";
        public static final String DEFAULT_OBJECT_CODE_5500 = "5500";
        public static final String REQUEST_V4_RELATIVE_ENDPOINT = "/travelrequest/v4/requests";
        public static final String REQUEST_V4_PAGE_SIZE_2 = "2";
        public static final String REQUEST_V4_DEFAULT_FROM_DATE_2022_01_02 = RequestV4Dates.DATE_2022_01_02;
        public static final String MAX_RETRIES_1 = "1";
    }
    
    public static final String PDP_LINE_FIXTURE_CASH_ADVANCE_KEY = "ABC123";
    public static final String PDP_LINE_FIXTURE_REPORT_ENTRY_ID = "entryID101";
    
    public static class PdpFeedFileConstants {
        public static final String CHART = "IT";
        public static final String BATCH_DATE = "02/17/2017";
        public static final String UNIT = "CRNL";
        public static final String SUB_UNIT = "CLIF";
        
        public static final String PAYEE_NAME_BIMBO = "Bimbo Foods Inc";
        public static final String PAYEE_ID_BIMBO = "13086-0";
        public static final String CUSTOM_INSTITUTION_IDENTIFIER_BIMBO = "18901";
        
        public static final String PAYEE_NAME_JAY = "Jay Hulslander";
        public static final String PAYEE_ID_JAY = "1231354353";
        public static final String PAYEE_ID_TYPE = "E";
        
        public static final String PAYEE_TYPE_VENDOR = "V";
        public static final String BANK_CODE = "DISB";
        public static final String COMBINE_GROUP_INDICATOR_YES = "Y";
        public static final String PAYMENT_DATE = "02/03/2017";
        public static final String PAYMENT_OWNERSHIP_CODE = "xyz";
        public static final String SOURCE_DOC_NUMBER = "C16326";
        public static final String INVOICE_NUMBER = "66432520714";
        public static final String ORIGIN_CODE = "Z1";
        public static final String FDOC_NUMBER = "APCL";
        public static final String ACCOUNT_NUMBER = "H833810";
        public static final String OBJECT_CODE = "6000";
    }

}
