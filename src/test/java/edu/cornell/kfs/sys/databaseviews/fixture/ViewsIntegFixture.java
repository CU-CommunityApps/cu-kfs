package edu.cornell.kfs.sys.databaseviews.fixture;

import java.util.HashMap;

public enum ViewsIntegFixture {
    COLLEGE_ORG_HRCY_V("SELECT FIN_COA_CD, ORG_CD, ORG_NM, COLLEGE_FIN_COA_CD, COLLEGE_ORG_CD, COLLEGE_ORG_NM FROM KFS.COLLEGE_ORG_HRCY_V WHERE ORG_CD = '0100'",
            new HashMap<String, String>(){{
                put("ORG_CD", "0100");
                put("COLLEGE_ORG_NM", "Agriculture and Life Sciences");
                put("COLLEGE_ORG_CD", "0100");
                put("COLLEGE_FIN_COA_CD", "IT");
                put("FIN_COA_CD", "IT"); 
                put("ORG_NM", "Agriculture and Life Sciences");
            }}),
    
    PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA("SELECT NET_ID, FULL_NAME, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PRIMARY_DEPARTMENT, DEPARTMENT_CHART, DEPARTMENT_ORG_CD, DEPARTMENT_ORG_NAME, COLLEGE_CHART, COLLEGE_ORG_CD, COLLEGE_ORG_NAME FROM KFS.PERSON_DEPARTMENT_INFO_V WHERE NET_ID = 'imw4'",
            new HashMap<String, String>(){{
                put("NET_ID" ,"imw4");
                put("DEPARTMENT_ORG_CD", null);
                put("COLLEGE_CHART", null);
                put("PRIMARY_DEPARTMENT", null);
                put("DEPARTMENT_CHART", null);
                put("FULL_NAME", "Wicks Isabel M");
                put("DEPARTMENT_ORG_NAME", null);
                put("MIDDLE_NAME", "M");
                put("LAST_NAME", "Wicks");
                put("COLLEGE_ORG_CD", null);
                put("COLLEGE_ORG_NAME", null);
                put("FIRST_NAME", "Isabel");
            }}),
    
    PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA("SELECT NET_ID, FULL_NAME, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PRIMARY_DEPARTMENT, DEPARTMENT_CHART, DEPARTMENT_ORG_CD, DEPARTMENT_ORG_NAME, COLLEGE_CHART, COLLEGE_ORG_CD, COLLEGE_ORG_NAME FROM KFS.PERSON_DEPARTMENT_INFO_V WHERE NET_ID = 'nms32'",
            new HashMap<String, String>(){{
                put("NET_ID" ,"nms32");
                put("DEPARTMENT_ORG_CD", "3807");
                put("COLLEGE_CHART", "IT");
                put("PRIMARY_DEPARTMENT", "IT-3807");
                put("DEPARTMENT_CHART", "IT");
                put("FULL_NAME", "Dockstader Nicole Meredith");
                put("DEPARTMENT_ORG_NAME", "CIT Infrastructure");
                put("MIDDLE_NAME", "Meredith");
                put("LAST_NAME", "Dockstader");
                put("COLLEGE_ORG_CD", "3800");
                put("COLLEGE_ORG_NAME", "Information Technologies");
                put("FIRST_NAME", "Nicole");
            }}),
    
    PCDO_INFO_V("SELECT DOCUMENT_ID, TRANSACTION_DATE, TOTAL_AMOUNT, TOTAL_AMOUNT_FORMATTED, HOLDER_NAME, HOLDER_NET_ID, VENDOR_NAME FROM KFS.PCDO_INFO_V WHERE ROWNUM = 1",
            new HashMap<String, String>(){{
                put("VENDOR_NAME", "UPS*00009R540606042011");
                put("TRANSACTION_DATE", "2011-06-30T00:00");
                put("HOLDER_NET_ID", null);
                put("TOTAL_AMOUNT_FORMATTED", "91.79");
                put("DOCUMENT_ID", "132868");
                put("HOLDER_NAME", "KIMBERLEE GOODWIN");
                put("TOTAL_AMOUNT", "91.79");
            }});
    
    public final String query;
    public final  HashMap<String, String> expectedResults;
    
    ViewsIntegFixture(String query,  HashMap<String, String> expectedResults) {
        this.query = query;
        this.expectedResults = expectedResults;
    }
}
