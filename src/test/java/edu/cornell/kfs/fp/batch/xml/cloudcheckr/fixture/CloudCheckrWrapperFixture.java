package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum CloudCheckrWrapperFixture {
    BILL_RESULT_1(5101.98026, 980.9246516995, 266.0180803809, 326.9748839, 
            awsAccountFixtureBuilder(AwsAccountFixture.DEPT1, AwsAccountFixture.DEPT2, AwsAccountFixture.DEPT3));
    
    public final KualiDecimal total;
    public final KualiDecimal max;
    public final KualiDecimal min;
    public final KualiDecimal average;
    public final List<AwsAccountFixture> awsAccountFixtures;
    
    private CloudCheckrWrapperFixture(double total, double max, double min, double average, AwsAccountFixture[] accountFixtures) {
        this.total = new KualiDecimal(total);
        this.max = new KualiDecimal(max);
        this.min = new KualiDecimal(min);
        this.average = new KualiDecimal(average);
        awsAccountFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(accountFixtures);
    }
    
    public CloudCheckrWrapper toCloudCheckrWrapper() {
        CloudCheckrWrapper wrapper = new CloudCheckrWrapper();
        wrapper.setAverage(average);
        wrapper.setMaximum(max);
        wrapper.setMinimum(min);
        wrapper.setTotal(total);
        List<GroupLevel> accounts = new ArrayList<GroupLevel>();
        for (AwsAccountFixture awsAccount : awsAccountFixtures) {
            accounts.add(awsAccount.toGroupLevel());
        }
        wrapper.setCostsByAccounts(accounts);
        return wrapper;
    }
    
    
    private static AwsAccountFixture[] awsAccountFixtureBuilder(AwsAccountFixture...accountFixtures) {
        return accountFixtures;
    }

}
