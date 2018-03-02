package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum CloudCheckrWrapperFixture {
    BASIC_CORNELL_TEST(CloudCheckrFixtureConstants.CORNELL_TEST_FILE_TOTAL, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MAX, 
            CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MIN, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_AVG, 
            groupLevelFixtureArrayBuilder(GroupLevelFixture.DEPT1_ACCOUNT_GROUP, GroupLevelFixture.DEPT2_ACCOUNT_GROUP, GroupLevelFixture.DEPT3_ACCOUNT_GROUP),
            groupingByTimeFixtureArrayBuilder(GroupingByTimeFixture.DEPT1_GROUP1, GroupingByTimeFixture.DEPT1_GROUP2, GroupingByTimeFixture.DEPT1_GROUP3,
                    GroupingByTimeFixture.DEPT1_GROUP4, GroupingByTimeFixture.DEPT1_GROUP5, GroupingByTimeFixture.DEPT2_GROUP1,
                    GroupingByTimeFixture.DEPT3_GROUP1)),
    CLOUDCHECKR_TEST(CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_TOTAL, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_MAX,
            CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_MIN, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_AVG,
            groupLevelFixtureArrayBuilder(GroupLevelFixture.CLOUDCHECKR_GROUP_1, GroupLevelFixture.CLOUDCHECKR_GROUP_2));
    
    public final KualiDecimal total;
    public final KualiDecimal max;
    public final KualiDecimal min;
    public final KualiDecimal average;
    public final List<GroupLevelFixture> groupLevelFixtures;
    public final List<GroupingByTimeFixture> groupingByTimeFixtures;
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, GroupLevelFixture[] groupLevelFixtureArray, 
            GroupingByTimeFixture[] groupingByTimeFixtureArray) {
        this.total = new KualiDecimal(total);
        this.max = new KualiDecimal(max);
        this.min = new KualiDecimal(min);
        this.average = new KualiDecimal(average);
        groupLevelFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(groupLevelFixtureArray);
        groupingByTimeFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(groupingByTimeFixtureArray);
    }
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, GroupLevelFixture[] groupLevelFixtureArray) {
        this(total, max, min, average, groupLevelFixtureArray, new GroupingByTimeFixture[0]);
    }
    
    public CloudCheckrWrapper toCloudCheckrWrapper() {
        CloudCheckrWrapper wrapper = new CloudCheckrWrapper();
        wrapper.setAverage(average);
        wrapper.setMaximum(max);
        wrapper.setMinimum(min);
        wrapper.setTotal(total);
        groupLevelFixtures.stream().forEach(awsAccount -> wrapper.getCostsByAccounts().add(awsAccount.toGroupLevel()));;
        groupingByTimeFixtures.stream().forEach(fixture -> wrapper.getGroupingByTimes().add(fixture.toGroupingByTime()));
        return wrapper;
    }
    
    
    private static GroupLevelFixture[] groupLevelFixtureArrayBuilder(GroupLevelFixture... fixtures) {
        return fixtures;
    }
    
    private static GroupingByTimeFixture[] groupingByTimeFixtureArrayBuilder(GroupingByTimeFixture... fixtures) {
        return fixtures;
    }

}
