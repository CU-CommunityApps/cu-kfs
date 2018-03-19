package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum ChartFixture {
    CHART_IT(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, true),
    CHART_CS(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, true);

    public final String chartOfAccountsCode;
    public final boolean active;

    private ChartFixture(String chartOfAccountsCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.active = active;
    }
}
