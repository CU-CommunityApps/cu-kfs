package edu.cornell.kfs.sys.util.fixture;

public enum TestUserFixture {
    TEST_USER("tstUser", "Test User", "IT", "6666");
    
    public final String principleId;
    public final String principleName;
    public final String campusCode;
    public final String employeeId;
    
    private TestUserFixture(String principleId, String principleName, String campusCode, String employeeId) {
        this.principleId = principleId;
        this.principleName = principleName;
        this.campusCode = campusCode;
        this.employeeId = employeeId;
    }
}
