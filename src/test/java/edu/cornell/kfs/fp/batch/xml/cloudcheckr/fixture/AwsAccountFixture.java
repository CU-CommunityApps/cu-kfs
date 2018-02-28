package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum AwsAccountFixture {
    DEPT1("AWS-abc (Cornell Dept1)", "Cornell Dept1", 980.9246516995, 
            costCentersBuilder(CostCenterFixture.DEPT_1_COST_CENTER_1, CostCenterFixture.DEPT_1_COST_CENTER_2, 
                    CostCenterFixture.DEPT_1_COST_CENTER_3, CostCenterFixture.DEPT_1_COST_CENTER_4 ,CostCenterFixture.DEPT_1_COST_CENTER_5)),
    DEPT2("AWS-def (Cornell dept2)", "Cornell dept2", 266.0180803809, costCentersBuilder(CostCenterFixture.DEPT_2_COST_CENTER_1)),
    DEPT3("AWS-ghi (Cornell dept3)", "Cornell dept3", 3855.0375293243, costCentersBuilder(CostCenterFixture.DEPT_3_COST_CENTER_1));
    
    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    public final KualiDecimal cost;
    public final Long usageQuantity;
    public final List<CostCenterFixture> costCenters;
    
    private AwsAccountFixture (String groupName, String groupValue, String friendlyName, double cost, long usageQuantity, CostCenterFixture[] costCenterFixtures) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Long(usageQuantity);
        costCenters = AccountingXmlDocumentFixtureUtils.toImmutableList(costCenterFixtures);
    }
    
    private AwsAccountFixture (String groupValue, String friendlyName, double cost, CostCenterFixture[] costCenterFixtures) {
        this(CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT, groupValue, friendlyName, cost, 0, costCenterFixtures);
    }
    
    public GroupLevel toGroupLevel() {
        GroupLevel group = new GroupLevel();
        group.setCost(cost);
        group.setFriendlyName(friendlyName);
        group.setGroupName(groupName);
        group.setGroupValue(groupValue);
        group.setUsageQuantity(usageQuantity);
        List<GroupLevel> groups = new ArrayList<GroupLevel>();
        for (CostCenterFixture costCenter : costCenters) {
            groups.add(costCenter.toGroupLevel());
        }
        group.setNextLevel(groups);
        return group;
    }
    
    private static CostCenterFixture[] costCentersBuilder(CostCenterFixture... fixtures) {
        return fixtures;
    }
    
}