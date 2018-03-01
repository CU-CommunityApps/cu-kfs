package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum AwsAccountFixture {
    DEPT1(CloudCheckrFixtureConstants.DEPARTMENT_1_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_1_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_1_COST, 
            costCentersBuilder(CostCenterFixture.DEPT_1_COST_CENTER_1, CostCenterFixture.DEPT_1_COST_CENTER_2, 
                    CostCenterFixture.DEPT_1_COST_CENTER_3, CostCenterFixture.DEPT_1_COST_CENTER_4 ,CostCenterFixture.DEPT_1_COST_CENTER_5)),
    DEPT2(CloudCheckrFixtureConstants.DEPARTMENT_2_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_2_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_2_COST,  costCentersBuilder(CostCenterFixture.DEPT_2_COST_CENTER_1)),
    DEPT3(CloudCheckrFixtureConstants.DEPARTMENT_3_GROUP_VALUE, CloudCheckrFixtureConstants.DEPARTMENT_3_FRIENDLY_NAME, 
            CloudCheckrFixtureConstants.DEPARTMENT_3_COST, costCentersBuilder(CostCenterFixture.DEPT_3_COST_CENTER_1)),
    CLOUDCHECKR_GROUP_1(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_SERVICE, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_S3, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_S3,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_S3_COST, 0,
            costCentersBuilder(CostCenterFixture.CLOUDCHECKR_EXAMPLE_COST_CENTER_MIKEB)),
    CLOUDCHECKR_GROUP_2(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_NAME_SERVICE, 
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VALUE_EC2, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_EC2,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_EC2_COST, 0,
            costCentersBuilder(CostCenterFixture.CLOUDCHECKR_EXAMPLE_COST_CENTER_VANW));
    
    public final String groupName;
    public final String groupValue;
    public final String friendlyName;
    public final KualiDecimal cost;
    public final Double usageQuantity;
    public final List<CostCenterFixture> costCenters;
    
    private AwsAccountFixture (String groupName, String groupValue, String friendlyName, double cost, double usageQuantity, CostCenterFixture[] costCenterFixtures) {
        this.groupName = groupName;
        this.groupValue = groupValue;
        this.friendlyName = friendlyName;
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Double(usageQuantity);
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
        costCenters.stream().forEach(costCenter -> group.getNextLevel().add(costCenter.toGroupLevel()));
        return group;
    }
    
    private static CostCenterFixture[] costCentersBuilder(CostCenterFixture... fixtures) {
        return fixtures;
    }
    
}