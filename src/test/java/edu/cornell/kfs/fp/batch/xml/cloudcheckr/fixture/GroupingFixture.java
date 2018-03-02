package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.Grouping;

public enum GroupingFixture {
    GROUPING_DEPT1_ACCOUNT(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT, CloudCheckrFixtureConstants.DEPARTMENT_1_GROUP_VALUE, 
            CloudCheckrFixtureConstants.DEPARTMENT_1_FRIENDLY_NAME),
    GROUPING_DEPT1_COSTCENTER1(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_U353803),
    GROUPING_DEPT1_COSTCENTER2(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_NONE),
    GROUPING_DEPT1_COSTCENTER3(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_U353805),
    GROUPING_DEPT1_COSTCENTER4(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_1503307),
    GROUPING_DEPT1_COSTCENTER5(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_U353901),
    GROUPING_DEPT2_ACCOUNT(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT, CloudCheckrFixtureConstants.DEPARTMENT_2_GROUP_VALUE, 
            CloudCheckrFixtureConstants.DEPARTMENT_2_FRIENDLY_NAME),
    GROUPING_DEPT2_COSTCENTER1(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_NONE),
    GROUPING_DEPT3_ACCOUNT(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT, CloudCheckrFixtureConstants.DEPARTMENT_3_GROUP_VALUE, 
            CloudCheckrFixtureConstants.DEPARTMENT_3_FRIENDLY_NAME),
    GROUPING_DEPT3_COSTCENTER1(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, CloudCheckrFixtureConstants.ACCOUNT_NONE);
    
    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    
    private GroupingFixture(String groupName, String groupValue, String friendlyName) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
    }
    
    private GroupingFixture(String groupName, String groupValue) {
        this(groupName, groupValue, StringUtils.EMPTY);
    }
    
    public Grouping toGrouping() {
        Grouping grouping = new Grouping();
        grouping.setFriendlyName(friendlyName);
        grouping.setGroupName(groupName);
        grouping.setGroupValue(groupValue);
        return grouping;
    }
}
