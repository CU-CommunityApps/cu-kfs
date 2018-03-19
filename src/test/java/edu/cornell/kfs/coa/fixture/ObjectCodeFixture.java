package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum ObjectCodeFixture {
    OC_IT_6600(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_AWS_DEFAULT_OBJ_CODE, true),
    OC_CS_6600(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, CuFPTestConstants.TEST_AWS_DEFAULT_OBJ_CODE, false),
    OC_IT_1000(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_OBJ_CODE_1000, true),
    OC_IT_4020(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_OBJ_CODE_4020, true);

    public final String chartOfAccountsCode;
    public final String objectCode;
    public final boolean active;

    private ObjectCodeFixture(String chartOfAccountsCode, String objectCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.objectCode = objectCode;
        this.active = active;
    }
}
