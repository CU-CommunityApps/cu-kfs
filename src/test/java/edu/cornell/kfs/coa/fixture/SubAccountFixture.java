package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum SubAccountFixture {
    SA_70170(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R583805, CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_70170),
    SA_NONCA(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966, CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_NONCA),
    SA_97601(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715, CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_97601),
    SA_SHAN(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, CuFPTestConstants.TEST_ACCOUNT_NUMBER_J801000, CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_SHAN);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final boolean active;

    private SubAccountFixture(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        this(chartOfAccountsCode, accountNumber, subAccountNumber, true);
    }
    private SubAccountFixture(String chartOfAccountsCode, String accountNumber, String subAccountNumber, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.active = active;
    }
}
