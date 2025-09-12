package edu.cornell.kfs.sys.databaseviews.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum ViewsIntegFixture {
    COLLEGE_ORG_HRCY_V(
            "SELECT FIN_COA_CD, ORG_CD, ORG_NM, COLLEGE_FIN_COA_CD, COLLEGE_ORG_CD, COLLEGE_ORG_NM FROM KFS.COLLEGE_ORG_HRCY_V WHERE ORG_CD = '0100'",
            createExpectedResults_COLLEGE_ORG_HRCY_V()),
    
    PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA(
            "SELECT NET_ID, FULL_NAME, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PRIMARY_DEPARTMENT, DEPARTMENT_CHART, DEPARTMENT_ORG_CD, DEPARTMENT_ORG_NAME, COLLEGE_CHART, COLLEGE_ORG_CD, COLLEGE_ORG_NAME FROM KFS.PERSON_DEPARTMENT_INFO_V WHERE NET_ID = 'imw4'",
            createExpectedResults_PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA()),
    
    PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA(
            "SELECT NET_ID, FULL_NAME, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PRIMARY_DEPARTMENT, DEPARTMENT_CHART, DEPARTMENT_ORG_CD, DEPARTMENT_ORG_NAME, COLLEGE_CHART, COLLEGE_ORG_CD, COLLEGE_ORG_NAME FROM KFS.PERSON_DEPARTMENT_INFO_V WHERE NET_ID = 'jdh34'",
            createExpectedResults_PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA()),
    
    PCDO_INFO_V(
            "SELECT DOCUMENT_ID, TRANSACTION_DATE, TOTAL_AMOUNT, TOTAL_AMOUNT_FORMATTED, HOLDER_NAME, HOLDER_NET_ID, VENDOR_NAME FROM KFS.PCDO_INFO_V WHERE DOCUMENT_ID = '132868'",
            createExpectedResults_PCDO_INFO_V()),
    
    PCARD_USER_INFO_V_INCOMPLETE_DATA(
            "SELECT FULL_NAME, NET_ID, DEPARTMENT_ORG_CD, DEPARTMENT_ORG_NAME, COLLEGE_ORG_CD, COLLEGE_ORG_NAME, CARD_ACCOUNT_NBR, EMPLOYEE_ID, CARD_ACCOUNT_STATUS, CYCLE_START_DATE, SUMMARY_AMOUNT, LOAD_DATE, HAS_ESHOP FROM KFS.PCARD_USER_INFO_V WHERE NET_ID = 'jdh34'",
            createExpectedResults_PCARD_USER_INFO_V_INCOMPLETE_DATA()),
    
    PERSON_ESHOP_ROLE_V_COMPLETE_DATA(
            "SELECT NET_ID, ESHOP_ROLE_COUNT FROM KFS.PERSON_ESHOP_ROLE_V WHERE NET_ID = 'tjh265'",
            createExpectedResults_PERSON_ESHOP_ROLE_V_COMPLETE_DATA()),
    
    PERSON_ESHOP_ROLE_V_INCOMPLETE_DATA(
            "SELECT NET_ID, ESHOP_ROLE_COUNT FROM KFS.PERSON_ESHOP_ROLE_V WHERE NET_ID = 'jdh34'",
            createExpectedResults_PERSON_ESHOP_ROLE_V_INCOMPLETE_DATA()),
    
    PERSON_ROLE_V_INCOMPLETE_DATA(
            "SELECT PRINCIPAL_ID, NET_ID, ROLE_NAMESPACE, ROLE_NAME, ROLE_ID FROM KFS.PERSON_ROLE_V WHERE NET_ID = 'imw4'",
            createExpectedResults_PERSON_ROLE_V_INCOMPLETE_DATA()),
    
    PERSON_ROLE_V_COMPLETE_DATA(
            "SELECT PRINCIPAL_ID, NET_ID, ROLE_NAMESPACE, ROLE_NAME, ROLE_ID FROM KFS.PERSON_ROLE_V WHERE NET_ID = 'jdh34'",
            createExpectedResults_PERSON_ROLE_V_COMPLETE_DATA()),
    
    PURCHASE_DETAILS_V_COMPLETE_DATA(
            "SELECT NET_ID, PRINCIPAL_ID, DOCUMENT_ID, DOC_TITLE, STATUS_CODE, CREATE_DATE, FINALIZATION_DATE, DOC_TYPE_ID, TOTAL_AMOUNT, DOC_TYPE_NAME, DOCUMENT_LABEL, REQS_VENDOR_NAME, IWANT_VENDOR_NAME, VENDOR_NAME FROM KFS.PURCHASE_DETAILS_V WHERE NET_ID = 'dp65'",
            createExpectedResults_PURCHASE_DETAILS_V_COMPLETE_DATA()),
    
    PURCHASE_DETAILS_V_INCOMPLETE_DATA(
            "SELECT NET_ID, PRINCIPAL_ID, DOCUMENT_ID, DOC_TITLE, STATUS_CODE, CREATE_DATE, FINALIZATION_DATE, DOC_TYPE_ID, TOTAL_AMOUNT, DOC_TYPE_NAME, DOCUMENT_LABEL, REQS_VENDOR_NAME, IWANT_VENDOR_NAME, VENDOR_NAME FROM KFS.PURCHASE_DETAILS_V WHERE NET_ID = 'jdh34'",
            createExpectedResults_PURCHASE_DETAILS_V_INCOMPLETE_DATA());
    
    private static List<HashMap<String, String>> createExpectedResults_COLLEGE_ORG_HRCY_V() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("ORG_CD", "0100");
        expectedResultSetItem_1.put("COLLEGE_ORG_NM", "Agriculture and Life Sciences");
        expectedResultSetItem_1.put("COLLEGE_ORG_CD", "0100");
        expectedResultSetItem_1.put("COLLEGE_FIN_COA_CD", "IT");
        expectedResultSetItem_1.put("FIN_COA_CD", "IT"); 
        expectedResultSetItem_1.put("ORG_NM", "Agriculture and Life Sciences");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList; 
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("NET_ID", "imw4");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_CD", null);
        expectedResultSetItem_1.put("COLLEGE_CHART", null);
        expectedResultSetItem_1.put("PRIMARY_DEPARTMENT", null);
        expectedResultSetItem_1.put("DEPARTMENT_CHART", null);
        expectedResultSetItem_1.put("FULL_NAME", "Wicks Isabel M");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_NAME", null);
        expectedResultSetItem_1.put("MIDDLE_NAME", "M");
        expectedResultSetItem_1.put("LAST_NAME", "Wicks");
        expectedResultSetItem_1.put("COLLEGE_ORG_CD", null);
        expectedResultSetItem_1.put("COLLEGE_ORG_NAME", null);
        expectedResultSetItem_1.put("FIRST_NAME", "Isabel");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList; 
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("NET_ID", "jdh34");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_CD", "3807");
        expectedResultSetItem_1.put("COLLEGE_CHART", "IT");
        expectedResultSetItem_1.put("PRIMARY_DEPARTMENT", "IT-3807");
        expectedResultSetItem_1.put("DEPARTMENT_CHART", "IT");
        expectedResultSetItem_1.put("FULL_NAME", "Hulslander Jay D.");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_NAME", "CIT Infrastructure");
        expectedResultSetItem_1.put("MIDDLE_NAME", "D.");
        expectedResultSetItem_1.put("LAST_NAME", "Hulslander");
        expectedResultSetItem_1.put("COLLEGE_ORG_CD", "3800");
        expectedResultSetItem_1.put("COLLEGE_ORG_NAME", "Information Technologies");
        expectedResultSetItem_1.put("FIRST_NAME", "Jay");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PCDO_INFO_V() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("VENDOR_NAME", "UPS*00009R540606042011");
        expectedResultSetItem_1.put("TRANSACTION_DATE", "30-JUN-11");
        expectedResultSetItem_1.put("HOLDER_NET_ID", null);
        expectedResultSetItem_1.put("TOTAL_AMOUNT_FORMATTED", "91.79");
        expectedResultSetItem_1.put("DOCUMENT_ID", "132868");
        expectedResultSetItem_1.put("HOLDER_NAME", "KIMBERLEE GOODWIN");
        expectedResultSetItem_1.put("TOTAL_AMOUNT", "91.79");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PCARD_USER_INFO_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("FULL_NAME", "Hulslander Jay D.");
        expectedResultSetItem_1.put("NET_ID", "jdh34");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_CD", "3807");
        expectedResultSetItem_1.put("DEPARTMENT_ORG_NAME", "CIT Infrastructure");
        expectedResultSetItem_1.put("COLLEGE_ORG_CD", "3800");
        expectedResultSetItem_1.put("COLLEGE_ORG_NAME", "Information Technologies");
        expectedResultSetItem_1.put("CARD_ACCOUNT_NBR", null);
        expectedResultSetItem_1.put("EMPLOYEE_ID", null);
        expectedResultSetItem_1.put("CARD_ACCOUNT_STATUS", null);
        expectedResultSetItem_1.put("CYCLE_START_DATE", null);
        expectedResultSetItem_1.put("SUMMARY_AMOUNT", null);
        expectedResultSetItem_1.put("LOAD_DATE", null);
        expectedResultSetItem_1.put("HAS_ESHOP", "0");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ESHOP_ROLE_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("NET_ID", "tjh265");
        expectedResultSetItem_1.put("ESHOP_ROLE_COUNT", "1");
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ESHOP_ROLE_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ROLE_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("PRINCIPAL_ID", "1007635");
        expectedResultSetItem_1.put("NET_ID", "imw4");
        expectedResultSetItem_1.put("ROLE_NAMESPACE", null);
        expectedResultSetItem_1.put("ROLE_NAME", null);
        expectedResultSetItem_1.put("ROLE_ID", null);
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ROLE_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("PRINCIPAL_ID", "50580");
        expectedResultSetItem_1.put("NET_ID", "jdh34");
        expectedResultSetItem_1.put("ROLE_NAMESPACE", "KFS-SYS");
        expectedResultSetItem_1.put("ROLE_NAME", "Tech Monitoring (cu)");
        expectedResultSetItem_1.put("ROLE_ID", "100000174");
        expectedResultSetList.add(expectedResultSetItem_1);
        
        HashMap<String, String>  expectedResultSetItem_2 = new HashMap<String, String>();
        expectedResultSetItem_2.put("PRINCIPAL_ID", "50580");
        expectedResultSetItem_2.put("NET_ID", "jdh34");
        expectedResultSetItem_2.put("ROLE_NAMESPACE", "KFS-SYS");
        expectedResultSetItem_2.put("ROLE_NAME", "Campus Viewer(cu)");
        expectedResultSetItem_2.put("ROLE_ID", "100000453");
        expectedResultSetList.add(expectedResultSetItem_2);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PURCHASE_DETAILS_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PURCHASE_DETAILS_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("NET_ID", "dp65");
        expectedResultSetItem_1.put("PRINCIPAL_ID", "1022525");
        expectedResultSetItem_1.put("DOCUMENT_ID", "33412793");
        expectedResultSetItem_1.put("DOC_TITLE", "I Want Document - IT-3802 ");
        expectedResultSetItem_1.put("STATUS_CODE", "D");
        expectedResultSetItem_1.put("CREATE_DATE", "21-JUN-20");
        expectedResultSetItem_1.put("FINALIZATION_DATE", "22-JUN-20");
        expectedResultSetItem_1.put("DOC_TYPE_ID", "17660402");
        expectedResultSetItem_1.put("TOTAL_AMOUNT", "0");
        expectedResultSetItem_1.put("DOC_TYPE_NAME", "IWNT");
        expectedResultSetItem_1.put("DOCUMENT_LABEL", "I Want Document");
        expectedResultSetItem_1.put("REQS_VENDOR_NAME", null);
        expectedResultSetItem_1.put("IWANT_VENDOR_NAME", null);
        expectedResultSetItem_1.put("VENDOR_NAME", null);
        expectedResultSetList.add(expectedResultSetItem_1);
        
        HashMap<String, String>  expectedResultSetItem_2 = new HashMap<String, String>();
        expectedResultSetItem_2.put("NET_ID", "dp65");
        expectedResultSetItem_2.put("PRINCIPAL_ID", "1022525");
        expectedResultSetItem_2.put("DOCUMENT_ID", "33391300");
        expectedResultSetItem_2.put("DOC_TITLE", "I Want Document - IT-3802 ");
        expectedResultSetItem_2.put("STATUS_CODE", "D");
        expectedResultSetItem_2.put("CREATE_DATE", "17-JUN-20");
        expectedResultSetItem_2.put("FINALIZATION_DATE", "17-JUN-20");
        expectedResultSetItem_2.put("DOC_TYPE_ID", "17660402");
        expectedResultSetItem_2.put("TOTAL_AMOUNT", "0");
        expectedResultSetItem_2.put("DOC_TYPE_NAME", "IWNT");
        expectedResultSetItem_2.put("DOCUMENT_LABEL", "I Want Document");
        expectedResultSetItem_2.put("REQS_VENDOR_NAME", null);
        expectedResultSetItem_2.put("IWANT_VENDOR_NAME", null);
        expectedResultSetItem_2.put("VENDOR_NAME", null);
        expectedResultSetList.add(expectedResultSetItem_2);
        return expectedResultSetList;
    }
    
    private final String query;
    private final List<HashMap<String, String>> expectedResults;
    
    
    public String getQuery() {
        return query;
    }
    
    public List<HashMap<String, String>> getExpectedResults() {
        return expectedResults;
    }
    
    ViewsIntegFixture(String query, List<HashMap<String, String>> expectedResults) {
        this.query = query;
        this.expectedResults = expectedResults;
    }

}
