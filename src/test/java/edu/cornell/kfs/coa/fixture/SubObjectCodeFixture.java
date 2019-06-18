package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum SubObjectCodeFixture {
    SO_109(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715,
            CuFPTestConstants.TEST_OBJ_CODE_4020, CuFPTestConstants.TEST_SUB_OBJ_CODE_109, true),
    SO_10X(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966,
            CuFPTestConstants.TEST_OBJ_CODE_1000, CuFPTestConstants.TEST_SUB_OBJ_CODE_10X, true);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final boolean active;

    private SubObjectCodeFixture(String chartOfAccountsCode, String accountNumber,String objectCode,String subObjectCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.active = active;
    }
}
