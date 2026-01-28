package edu.cornell.kfs.sys.databaseviews.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum ViewsIntegFixture {
    CA_OBJECT_CODE_V(
            "SELECT UNIV_FISCAL_YR, FIN_COA_CD, FIN_OBJECT_CD, FIN_OBJ_CD_NM, FIN_OBJ_CD_SHRT_NM, FIN_OBJ_LEVEL_CD, RPTS_TO_FIN_COA_CD, RPTS_TO_FIN_OBJ_CD, FIN_OBJ_TYP_CD, FIN_OBJ_SUB_TYP_CD, HIST_FIN_OBJECT_CD, FIN_OBJ_ACTIVE_CD, FOBJ_BDGT_AGGR_CD, FOBJ_MNXFR_ELIM_CD, FIN_FED_FUNDED_CD, NXT_YR_FIN_OBJ_CD, RSCH_BDGT_CTGRY_CD, RSCH_OBJ_CD_DESC, RSCH_ON_CMP_IND FROM KFS.CA_OBJECT_CODE_V WHERE FIN_COA_CD = 'IT' AND FIN_OBJECT_CD = '6550'",
            createExpectedResults_CA_OBJECT_CODE_V()),
    
    CA_SUB_OBJECT_CD_V(
            "SELECT UNIV_FISCAL_YR, FIN_COA_CD, ACCOUNT_NBR, FIN_OBJECT_CD, FIN_SUB_OBJ_CD, FIN_SUB_OBJ_CD_NM, FIN_SUBOBJ_SHRT_NM, FIN_SUBOBJ_ACTV_CD FROM KFS.CA_SUB_OBJECT_CD_V WHERE ACCOUNT_NBR = 'A403002' AND FIN_COA_CD = 'IT' AND FIN_OBJECT_CD = '6550'",
            createExpectedResults_CA_SUB_OBJECT_CD_V()),
    
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
    
    private static List<HashMap<String, String>> createExpectedResults_CA_OBJECT_CODE_V() {
        // NOTE: Enum ViewsIntegDynamicFixture.CURRENT_UNIV_FISCAL_YR will obtain the "UNIV_FISCAL_YR"
        // as seen by the system when the unit test runs. The actual unit test will combine that dynamic
        // value with this static result set to create the complete result set that will ultimately be
        // used by the unit test for validation.
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("FIN_COA_CD", "IT");
        expectedResultSetItem.put("FIN_OBJECT_CD", "6550");
        expectedResultSetItem.put("FIN_OBJ_CD_NM", "Supplies - Office");
        expectedResultSetItem.put("FIN_OBJ_CD_SHRT_NM", "Office");
        expectedResultSetItem.put("FIN_OBJ_LEVEL_CD", "SMAT");
        expectedResultSetItem.put("RPTS_TO_FIN_COA_CD", "CU");
        expectedResultSetItem.put("RPTS_TO_FIN_OBJ_CD", "E370");
        expectedResultSetItem.put("FIN_OBJ_TYP_CD", "EX");
        expectedResultSetItem.put("FIN_OBJ_SUB_TYP_CD", "OE");
        expectedResultSetItem.put("HIST_FIN_OBJECT_CD", "666*");
        expectedResultSetItem.put("FIN_OBJ_ACTIVE_CD", "Y");
        expectedResultSetItem.put("FOBJ_BDGT_AGGR_CD", "O");
        expectedResultSetItem.put("FOBJ_MNXFR_ELIM_CD", "N");
        expectedResultSetItem.put("FIN_FED_FUNDED_CD", "N");
        expectedResultSetItem.put("NXT_YR_FIN_OBJ_CD", null);
        expectedResultSetItem.put("RSCH_BDGT_CTGRY_CD", null);
        expectedResultSetItem.put("RSCH_OBJ_CD_DESC", null);
        expectedResultSetItem.put("RSCH_ON_CMP_IND", null);
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_CA_SUB_OBJECT_CD_V() {
        // NOTE: Enum ViewsIntegDynamicFixture.CURRENT_UNIV_FISCAL_YR will obtain the "UNIV_FISCAL_YR"
        // as seen by the system when the unit test runs. The actual unit test will combine that dynamic
        // value with this static result set to create the complete result set that will ultimately be
        // used by the unit test for validation.
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String> expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("FIN_COA_CD", "IT");
        expectedResultSetItem.put("ACCOUNT_NBR", "A403002");
        expectedResultSetItem.put("FIN_OBJECT_CD", "6550");
        expectedResultSetItem.put("FIN_SUB_OBJ_CD", "603");
        expectedResultSetItem.put("FIN_SUB_OBJ_CD_NM", "COPIES");
        expectedResultSetItem.put("FIN_SUBOBJ_SHRT_NM", "COPIES");
        expectedResultSetItem.put("FIN_SUBOBJ_ACTV_CD", "Y");
        expectedResultSetList.add(expectedResultSetItem);
        
        expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("FIN_COA_CD", "IT");
        expectedResultSetItem.put("ACCOUNT_NBR", "A403002");
        expectedResultSetItem.put("FIN_OBJECT_CD", "6550");
        expectedResultSetItem.put("FIN_SUB_OBJ_CD", "604");
        expectedResultSetItem.put("FIN_SUB_OBJ_CD_NM", "SUPPLY");
        expectedResultSetItem.put("FIN_SUBOBJ_SHRT_NM", "SUPPLY");
        expectedResultSetItem.put("FIN_SUBOBJ_ACTV_CD", "Y");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_COLLEGE_ORG_HRCY_V() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("ORG_CD", "0100");
        expectedResultSetItem.put("COLLEGE_ORG_NM", "Agriculture and Life Sciences");
        expectedResultSetItem.put("COLLEGE_ORG_CD", "0100");
        expectedResultSetItem.put("COLLEGE_FIN_COA_CD", "IT");
        expectedResultSetItem.put("FIN_COA_CD", "IT"); 
        expectedResultSetItem.put("ORG_NM", "Agriculture and Life Sciences");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList; 
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("NET_ID", "imw4");
        expectedResultSetItem.put("DEPARTMENT_ORG_CD", null);
        expectedResultSetItem.put("COLLEGE_CHART", null);
        expectedResultSetItem.put("PRIMARY_DEPARTMENT", null);
        expectedResultSetItem.put("DEPARTMENT_CHART", null);
        expectedResultSetItem.put("FULL_NAME", "Wicks Isabel M");
        expectedResultSetItem.put("DEPARTMENT_ORG_NAME", null);
        expectedResultSetItem.put("MIDDLE_NAME", "M");
        expectedResultSetItem.put("LAST_NAME", "Wicks");
        expectedResultSetItem.put("COLLEGE_ORG_CD", null);
        expectedResultSetItem.put("COLLEGE_ORG_NAME", null);
        expectedResultSetItem.put("FIRST_NAME", "Isabel");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList; 
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("NET_ID", "jdh34");
        expectedResultSetItem.put("DEPARTMENT_ORG_CD", "3807");
        expectedResultSetItem.put("COLLEGE_CHART", "IT");
        expectedResultSetItem.put("PRIMARY_DEPARTMENT", "IT-3807");
        expectedResultSetItem.put("DEPARTMENT_CHART", "IT");
        expectedResultSetItem.put("FULL_NAME", "Hulslander Jay D.");
        expectedResultSetItem.put("DEPARTMENT_ORG_NAME", "CIT Infrastructure");
        expectedResultSetItem.put("MIDDLE_NAME", "D.");
        expectedResultSetItem.put("LAST_NAME", "Hulslander");
        expectedResultSetItem.put("COLLEGE_ORG_CD", "3800");
        expectedResultSetItem.put("COLLEGE_ORG_NAME", "Information Technologies");
        expectedResultSetItem.put("FIRST_NAME", "Jay");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PCDO_INFO_V() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("VENDOR_NAME", "UPS*00009R540606042011");
        expectedResultSetItem.put("TRANSACTION_DATE", "30-JUN-11");
        expectedResultSetItem.put("HOLDER_NET_ID", null);
        expectedResultSetItem.put("TOTAL_AMOUNT_FORMATTED", "91.79");
        expectedResultSetItem.put("DOCUMENT_ID", "132868");
        expectedResultSetItem.put("HOLDER_NAME", "KIMBERLEE GOODWIN");
        expectedResultSetItem.put("TOTAL_AMOUNT", "91.79");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PCARD_USER_INFO_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("FULL_NAME", "Hulslander Jay D.");
        expectedResultSetItem.put("NET_ID", "jdh34");
        expectedResultSetItem.put("DEPARTMENT_ORG_CD", "3807");
        expectedResultSetItem.put("DEPARTMENT_ORG_NAME", "CIT Infrastructure");
        expectedResultSetItem.put("COLLEGE_ORG_CD", "3800");
        expectedResultSetItem.put("COLLEGE_ORG_NAME", "Information Technologies");
        expectedResultSetItem.put("CARD_ACCOUNT_NBR", null);
        expectedResultSetItem.put("EMPLOYEE_ID", null);
        expectedResultSetItem.put("CARD_ACCOUNT_STATUS", null);
        expectedResultSetItem.put("CYCLE_START_DATE", null);
        expectedResultSetItem.put("SUMMARY_AMOUNT", null);
        expectedResultSetItem.put("LOAD_DATE", null);
        expectedResultSetItem.put("HAS_ESHOP", "0");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ESHOP_ROLE_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("NET_ID", "tjh265");
        expectedResultSetItem.put("ESHOP_ROLE_COUNT", "1");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ESHOP_ROLE_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ROLE_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("PRINCIPAL_ID", "1007635");
        expectedResultSetItem.put("NET_ID", "imw4");
        expectedResultSetItem.put("ROLE_NAMESPACE", null);
        expectedResultSetItem.put("ROLE_NAME", null);
        expectedResultSetItem.put("ROLE_ID", null);
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PERSON_ROLE_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("PRINCIPAL_ID", "50580");
        expectedResultSetItem.put("NET_ID", "jdh34");
        expectedResultSetItem.put("ROLE_NAMESPACE", "KFS-SYS");
        expectedResultSetItem.put("ROLE_NAME", "Tech Monitoring (cu)");
        expectedResultSetItem.put("ROLE_ID", "100000174");
        expectedResultSetList.add(expectedResultSetItem);
        
        expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("PRINCIPAL_ID", "50580");
        expectedResultSetItem.put("NET_ID", "jdh34");
        expectedResultSetItem.put("ROLE_NAMESPACE", "KFS-SYS");
        expectedResultSetItem.put("ROLE_NAME", "Campus Viewer(cu)");
        expectedResultSetItem.put("ROLE_ID", "100000453");
        expectedResultSetList.add(expectedResultSetItem);
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PURCHASE_DETAILS_V_INCOMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        return expectedResultSetList;
    }
    
    private static List<HashMap<String, String>> createExpectedResults_PURCHASE_DETAILS_V_COMPLETE_DATA() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("NET_ID", "dp65");
        expectedResultSetItem.put("PRINCIPAL_ID", "1022525");
        expectedResultSetItem.put("DOCUMENT_ID", "33412793");
        expectedResultSetItem.put("DOC_TITLE", "I Want Document - IT-3802 ");
        expectedResultSetItem.put("STATUS_CODE", "D");
        expectedResultSetItem.put("CREATE_DATE", "21-JUN-20");
        expectedResultSetItem.put("FINALIZATION_DATE", "22-JUN-20");
        expectedResultSetItem.put("DOC_TYPE_ID", "17660402");
        expectedResultSetItem.put("TOTAL_AMOUNT", "0");
        expectedResultSetItem.put("DOC_TYPE_NAME", "IWNT");
        expectedResultSetItem.put("DOCUMENT_LABEL", "I Want Document");
        expectedResultSetItem.put("REQS_VENDOR_NAME", null);
        expectedResultSetItem.put("IWANT_VENDOR_NAME", null);
        expectedResultSetItem.put("VENDOR_NAME", null);
        expectedResultSetList.add(expectedResultSetItem);
        
        expectedResultSetItem = new HashMap<String, String>();
        expectedResultSetItem.put("NET_ID", "dp65");
        expectedResultSetItem.put("PRINCIPAL_ID", "1022525");
        expectedResultSetItem.put("DOCUMENT_ID", "33391300");
        expectedResultSetItem.put("DOC_TITLE", "I Want Document - IT-3802 ");
        expectedResultSetItem.put("STATUS_CODE", "D");
        expectedResultSetItem.put("CREATE_DATE", "17-JUN-20");
        expectedResultSetItem.put("FINALIZATION_DATE", "17-JUN-20");
        expectedResultSetItem.put("DOC_TYPE_ID", "17660402");
        expectedResultSetItem.put("TOTAL_AMOUNT", "0");
        expectedResultSetItem.put("DOC_TYPE_NAME", "IWNT");
        expectedResultSetItem.put("DOCUMENT_LABEL", "I Want Document");
        expectedResultSetItem.put("REQS_VENDOR_NAME", null);
        expectedResultSetItem.put("IWANT_VENDOR_NAME", null);
        expectedResultSetItem.put("VENDOR_NAME", null);
        expectedResultSetList.add(expectedResultSetItem);
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
