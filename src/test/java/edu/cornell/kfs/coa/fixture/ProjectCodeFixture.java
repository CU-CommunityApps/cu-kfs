package edu.cornell.kfs.coa.fixture;

import edu.cornell.kfs.fp.CuFPTestConstants;

public enum ProjectCodeFixture {
    PC_EB_PLGIFT(CuFPTestConstants.TEST_PROJECT_CODE_EB_PLGIFT, true);

    public final String projectCode;
    public final boolean active;

    private ProjectCodeFixture(String projectCode, boolean active) {
        this.projectCode = projectCode;
        this.active = active;
    }
}
