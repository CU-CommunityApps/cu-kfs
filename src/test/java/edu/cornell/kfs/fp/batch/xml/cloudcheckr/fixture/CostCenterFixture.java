package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;

public enum CostCenterFixture {
    DEPT_1_COST_CENTER_1(18.7591161721, "None"),
    DEPT_1_COST_CENTER_2(2.4449795375, "U353901"),
    DEPT_1_COST_CENTER_3(951.4736250193, "U353803"),
    DEPT_1_COST_CENTER_4(4.1275978282, "U353805"),
    DEPT_1_COST_CENTER_5(4.1193331424, "1503307"),
    DEPT_2_COST_CENTER_1(266.0180803809, "None"),
    DEPT_3_COST_CENTER_1(3855.0375293243, "None");
    
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
