package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum CloudCheckrWrapperFixture {
    BASIC_CORNELL_TEST(CloudCheckrFixtureConstants.CORNELL_TEST_FILE_TOTAL, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MAX, 
            CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MIN, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_AVG, 
            awsAccountFixtureBuilder(GroupLevelFixture.DEPT1_ACCOUNT_GROUP, GroupLevelFixture.DEPT2_ACCOUNT_GROUP, GroupLevelFixture.DEPT3_ACCOUNT_GROUP),
            costsByTimeFixtureBuilder(CostsByTimeFixture.DEPT1_GROUP1, CostsByTimeFixture.DEPT1_GROUP2, CostsByTimeFixture.DEPT1_GROUP3,
                    CostsByTimeFixture.DEPT1_GROUP4, CostsByTimeFixture.DEPT1_GROUP5, CostsByTimeFixture.DEPT2_GROUP1,
                    CostsByTimeFixture.DEPT3_GROUP1)),
    CLOUDCHECKR_TEST(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_TOTAL, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_MAX,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_MIN, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_AVG,
            awsAccountFixtureBuilder(GroupLevelFixture.CLOUDCHECKR_GROUP_1, GroupLevelFixture.CLOUDCHECKR_GROUP_2));
    
    public final KualiDecimal total;
    public final KualiDecimal max;
    public final KualiDecimal min;
    public final KualiDecimal average;
    public final List<GroupLevelFixture> awsAccountFixtures;
    public final List<CostsByTimeFixture> costsByTimeFixtures;
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, GroupLevelFixture[] accountFixtureArray, 
            CostsByTimeFixture[] costsByTimeFixtureArray) {
        this.total = new KualiDecimal(total);
        this.max = new KualiDecimal(max);
        this.min = new KualiDecimal(min);
        this.average = new KualiDecimal(average);
        awsAccountFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(accountFixtureArray);
        costsByTimeFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(costsByTimeFixtureArray);
    }
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, GroupLevelFixture[] accountFixtureArray) {
        this(total, max, min, average, accountFixtureArray, new CostsByTimeFixture[0]);
    }
    
    public CloudCheckrWrapper toCloudCheckrWrapper() {
        CloudCheckrWrapper wrapper = new CloudCheckrWrapper();
        wrapper.setAverage(average);
        wrapper.setMaximum(max);
        wrapper.setMinimum(min);
        wrapper.setTotal(total);
        awsAccountFixtures.stream().forEach(awsAccount -> wrapper.getCostsByAccounts().add(awsAccount.toGroupLevel()));;
        costsByTimeFixtures.stream().forEach(fixture -> wrapper.getCostsByTimes().add(fixture.toCostsByTime()));
        return wrapper;
    }
    
    
    private static GroupLevelFixture[] awsAccountFixtureBuilder(GroupLevelFixture...accountFixtures) {
        return accountFixtures;
    }
    
    private static CostsByTimeFixture[] costsByTimeFixtureBuilder(CostsByTimeFixture...accountFixtures) {
        return accountFixtures;
    }

}
