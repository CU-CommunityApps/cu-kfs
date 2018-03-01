package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CostsByTimeGrouping;

public enum CostsByTimeGroupingFixture {
    GROUPING_DEPT1_ACCOUNT("Account", "AWS-abc (Cornell Dept1)", "Cornell Dept1"),
    GROUPING_DEPT1_COSTCENTER1("Cost Center", "U353803"),
    GROUPING_DEPT1_COSTCENTER2("Cost Center", "None"),
    GROUPING_DEPT1_COSTCENTER3("Cost Center", "U353805"),
    GROUPING_DEPT1_COSTCENTER4("Cost Center", "1503307"),
    GROUPING_DEPT1_COSTCENTER5("Cost Center", "U353901"),
    GROUPING_DEPT2_ACCOUNT("Account", "AWS-def (Cornell dept2)", "Cornell dept2"),
    GROUPING_DEPT2_COSTCENTER1("Cost Center", "None"),
    GROUPING_DEPT3_ACCOUNT("Account", "AWS-ghi (Cornell dept3)", "Cornell dept3"),
    GROUPING_DEPT3_COSTCENTER1("Cost Center", "None");
    
    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    
    private CostsByTimeGroupingFixture(String groupName, String groupValue, String friendlyName) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
    }
    
    private CostsByTimeGroupingFixture(String groupName, String groupValue) {
        this(groupName, groupValue, StringUtils.EMPTY);
    }
    
    public CostsByTimeGrouping toCostsByTimeGrouping() {
        CostsByTimeGrouping grouping = new CostsByTimeGrouping();
        grouping.setFriendlyName(friendlyName);
        grouping.setGroupName(groupName);
        grouping.setGroupValue(groupValue);
        return grouping;
    }
}
