package edu.cornell.kfs.concur.batch.fixture;

import edu.cornell.kfs.concur.ConcurTestConstants;

/**
 * Helper fixture to simplify the setup of the SAE Detail Line fixture.
 */
public enum ConcurEmployeeFixture {

    JOHN_DOE("1111111", "Doe", "John", "J", ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS),
    JANE_DOE("1234567", "Doe", "Jane", "M", ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS),
    DAN_SMITH("9876543", "Smith", "Dan", "K", ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS),
    LONG_FIRSTNAME("2323232", "Jones", "VeryLongFirstName", "A",
            ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS),
    LONG_LASTNAME("3434343", "VeryLongLastName", "Jack", "A",
            ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS),
    LONG_FULLNAME("4545454", "VeryLongLastName", "VeryLongFirstName", "A",
            ConcurTestConstants.EMPLOYEE_GROUP_ID, ConcurTestConstants.EMPLOYEE_DEFAULT_STATUS);

    public final String employeeId;
    public final String lastName;
    public final String firstName;
    public final String middleInitial;
    public final String groupId;
    public final String status;

    private ConcurEmployeeFixture(String employeeId, String lastName, String firstName, String middleInitial, String groupId, String status) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.groupId = groupId;
        this.status = status;
    }

}
