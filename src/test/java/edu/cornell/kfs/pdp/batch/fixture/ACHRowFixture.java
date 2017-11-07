package edu.cornell.kfs.pdp.batch.fixture;

public enum ACHRowFixture {
    ROW_WITH_VALIDATION_FAILURE(ACHUpdateFixture.NO_UPDATE, ACHUpdateFixture.NO_UPDATE, false),
    VALID_ROW_NO_UPDATES(ACHUpdateFixture.NO_UPDATE, ACHUpdateFixture.NO_UPDATE, true),
    JOHN_DOE_CREATE_EMPLOYEE_CREATE_ENTITY(ACHUpdateFixture.CREATE_JOHN_DOE_EMPLOYEE_ACCOUNT, ACHUpdateFixture.CREATE_JOHN_DOE_ENTITY_ACCOUNT),
    JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY(ACHUpdateFixture.UPDATE_JANE_DOE_EMPLOYEE_ACCOUNT, ACHUpdateFixture.CREATE_JANE_DOE_ENTITY_ACCOUNT),
    ROBERT_SMITH_CREATE_EMPLOYEE_UPDATE_ENTITY(ACHUpdateFixture.CREATE_ROBERT_SMITH_EMPLOYEE_ACCOUNT, ACHUpdateFixture.UPDATE_ROBERT_SMITH_ENTITY_ACCOUNT),
    MARY_SMITH_UPDATE_EMPLOYEE_UPDATE_ENTITY(ACHUpdateFixture.UPDATE_MARY_SMITH_EMPLOYEE_ACCOUNT, ACHUpdateFixture.UPDATE_MARY_SMITH_ENTITY_ACCOUNT),
    ROBERT_SMITH_CREATE_EMPLOYEE_ONLY(ACHUpdateFixture.CREATE_ROBERT_SMITH_EMPLOYEE_ALT_ACCOUNT, ACHUpdateFixture.NO_UPDATE),
    JANE_DOE_CREATE_ENTITY_ONLY(ACHUpdateFixture.NO_UPDATE, ACHUpdateFixture.CREATE_JANE_DOE_ENTITY_ALT_ACCOUNT);

    public final ACHUpdateFixture employeeUpdateResult;
    public final ACHUpdateFixture entityUpdateResult;
    public final boolean validRow;

    private ACHRowFixture(ACHUpdateFixture employeeUpdateResult, ACHUpdateFixture entityUpdateResult) {
        this(employeeUpdateResult, entityUpdateResult, true);
    }

    private ACHRowFixture(ACHUpdateFixture employeeUpdateResult, ACHUpdateFixture entityUpdateResult, boolean validRow) {
        this.employeeUpdateResult = employeeUpdateResult;
        this.entityUpdateResult = entityUpdateResult;
        this.validRow = validRow;
    }

}
