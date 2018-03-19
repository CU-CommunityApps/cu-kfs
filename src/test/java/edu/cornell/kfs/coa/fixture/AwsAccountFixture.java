package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum AwsAccountFixture {
    ACCOUNT_1658328(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1658328),
    ACCOUNT_R583805(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R583805),
    ACCOUNT_R589966(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966),
    ACCOUNT_1023715(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715),
    ACCOUNT_CS_J801000(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, CuFPTestConstants.TEST_ACCOUNT_NUMBER_J801000);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final boolean expired;
    public final boolean active;

    private AwsAccountFixture(String chartOfAccountsCode, String accountNumber) {
        this(chartOfAccountsCode, accountNumber, false, true);
    }

    private AwsAccountFixture(String chartOfAccountsCode, String accountNumber, boolean expired, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.expired = expired;
        this.active = active;
    }
}
