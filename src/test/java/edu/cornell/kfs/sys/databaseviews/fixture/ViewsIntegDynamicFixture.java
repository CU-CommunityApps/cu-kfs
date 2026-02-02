package edu.cornell.kfs.sys.databaseviews.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum ViewsIntegDynamicFixture {
    CURRENT_UNIV_FISCAL_YR(
            "SELECT UNIV_FISCAL_YR FROM KFS.SH_UNIV_DATE_T WHERE UNIV_DT LIKE SYSDATE",
            createExpectedResultsColumnNames_CURRENT_UNIV_FISCAL_YR());
    
    private static List<HashMap<String, String>> createExpectedResultsColumnNames_CURRENT_UNIV_FISCAL_YR() {
        List<HashMap<String, String>> expectedResultSetList = new ArrayList<>();
        HashMap<String, String>  expectedResultSetItem_1 = new HashMap<String, String>();
        expectedResultSetItem_1.put("UNIV_FISCAL_YR", null);
        expectedResultSetList.add(expectedResultSetItem_1);
        return expectedResultSetList;
    }

    private final String query;
    private final List<HashMap<String, String>> expectedResultsColumnNames;
    
    public String getQuery() {
        return query;
    }
    
    public List<HashMap<String, String>> getExpectedResults() {
        return expectedResultsColumnNames;
    }
    
    ViewsIntegDynamicFixture(String query, List<HashMap<String, String>> expectedResultsColumnNames) {
        this.query = query;
        this.expectedResultsColumnNames = expectedResultsColumnNames;
    }
}
