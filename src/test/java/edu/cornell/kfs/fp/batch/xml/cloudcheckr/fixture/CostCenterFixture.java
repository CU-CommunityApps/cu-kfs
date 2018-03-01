package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;

public enum CostCenterFixture {
    DEPT_1_COST_CENTER_1(CloudCheckrFixtureConstants.DEPT1_COSTCENTER2_COST, CloudCheckrFixtureConstants.ACCOUNT_NONE),
    DEPT_1_COST_CENTER_2(CloudCheckrFixtureConstants.DEPT1_COSTCENTER5_COST, CloudCheckrFixtureConstants.ACCOUNT_U353901),
    DEPT_1_COST_CENTER_3(CloudCheckrFixtureConstants.DEPT1_COSTCENTER1_COST, CloudCheckrFixtureConstants.ACCOUNT_U353803),
    DEPT_1_COST_CENTER_4(CloudCheckrFixtureConstants.DEPT1_COSTCENTER3_COST, CloudCheckrFixtureConstants.ACCOUNT_U353805),
    DEPT_1_COST_CENTER_5(CloudCheckrFixtureConstants.DEPT1_COSTCENTER4_COST, CloudCheckrFixtureConstants.ACCOUNT_1503307),
    DEPT_2_COST_CENTER_1(CloudCheckrFixtureConstants.DEPT2_COSTCENTER1_COST, CloudCheckrFixtureConstants.ACCOUNT_NONE),
    DEPT_3_COST_CENTER_1(CloudCheckrFixtureConstants.DEPT3_COSTCENTER1_COST, CloudCheckrFixtureConstants.ACCOUNT_NONE);
    
    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    public final KualiDecimal cost;
    public final Long usageQuantity;
    
    private CostCenterFixture (String groupName, String groupValue, String friendlyName, double cost, long usageQuantity) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Long(usageQuantity);
    }
    
    private CostCenterFixture (double cost, String groupValue) {
        this(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER, groupValue, StringUtils.EMPTY, cost, 0);
    }
    
    public GroupLevel toGroupLevel() {
        GroupLevel group = new GroupLevel();
        group.setCost(cost);
        group.setFriendlyName(friendlyName);
        group.setGroupName(groupName);
        group.setGroupValue(groupValue);
        group.setUsageQuantity(usageQuantity);
        return group;
    }
}
