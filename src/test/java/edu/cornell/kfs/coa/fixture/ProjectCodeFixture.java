package edu.cornell.kfs.coa.fixture;

public enum ProjectCodeFixture {
    PC_EB_PLGIFT("EB-PLGIFTT", true);

    public final String projectCode;
    public final boolean active;

    private ProjectCodeFixture(String projectCode, boolean active) {
        this.projectCode = projectCode;
        this.active = active;
    }
}
