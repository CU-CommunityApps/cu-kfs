package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kim.impl.identity.Person;

public enum ConcurPersonFixture {
    JOHN_STATE_COUNTRY("101", "John", "Doe", "NY", "US"),
    JANE_STATE("102", "Janee", "Doe", "NY", StringUtils.EMPTY),
    TOM_COUNTRY("103", "Jane", "Doe", StringUtils.EMPTY, "US"),
    TINA_INVALID_ADDRESS("104", "Tina", "Doe", StringUtils.EMPTY, StringUtils.EMPTY);
    
    public final Person person;
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String state, String countryCode) {
        this(employeeId, firstName, lastName, "120 Maple Ave", StringUtils.EMPTY, StringUtils.EMPTY, "Ithaca", state, "13068", countryCode);
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String addressLine2, String addressLine3,
            String city, String state, String postalCode, String countryCode) {
        this.person = new ConcurTestablePerson(employeeId, firstName, lastName, addressLine1, addressLine2, addressLine3, city, state, postalCode, countryCode);
    }
    
    public static Person personFromEmployeeId(String employeeId) {
        for (ConcurPersonFixture currentPerson : ConcurPersonFixture.values()) {
            if (StringUtils.equalsIgnoreCase(currentPerson.person.getEmployeeId(), employeeId)) {
                return currentPerson.person;
            }
        }
        throw new IllegalArgumentException("Unknown employee ID: " + employeeId);
    }
}
