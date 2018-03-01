package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum CloudCheckrWrapperFixture {
    BILL_RESULT_1(CloudCheckrFixtureConstants.CORNELL_TEST_FILE_TOTAL, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MAX, 
            CloudCheckrFixtureConstants.CORNELL_TEST_FILE_MIN, CloudCheckrFixtureConstants.CORNELL_TEST_FILE_AVG, 
            awsAccountFixtureBuilder(AwsAccountFixture.DEPT1, AwsAccountFixture.DEPT2, AwsAccountFixture.DEPT3),
            costsByTimeFixtureBuilder(CostsByTimeFixture.DEPT1_GROUP1, CostsByTimeFixture.DEPT1_GROUP2, CostsByTimeFixture.DEPT1_GROUP3,
                    CostsByTimeFixture.DEPT1_GROUP4, CostsByTimeFixture.DEPT1_GROUP5, CostsByTimeFixture.DEPT2_GROUP1,
                    CostsByTimeFixture.DEPT3_GROUP1));
    
    public final KualiDecimal total;
    public final KualiDecimal max;
    public final KualiDecimal min;
    public final KualiDecimal average;
    public final List<AwsAccountFixture> awsAccountFixtures;
    public final List<CostsByTimeFixture> costsByTimeFixtures;
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, AwsAccountFixture[] accountFixtureArray, 
            CostsByTimeFixture[] costsByTimeFixtureArray) {
        this.total = new KualiDecimal(total);
        this.max = new KualiDecimal(max);
        this.min = new KualiDecimal(min);
        this.average = new KualiDecimal(average);
        awsAccountFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(accountFixtureArray);
        costsByTimeFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(costsByTimeFixtureArray);
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
    
    
    private static AwsAccountFixture[] awsAccountFixtureBuilder(AwsAccountFixture...accountFixtures) {
        return accountFixtures;
    }
    
    private static CostsByTimeFixture[] costsByTimeFixtureBuilder(CostsByTimeFixture...accountFixtures) {
        return accountFixtures;
    }

}
