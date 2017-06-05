package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.api.identity.Person;

public enum ConcurPersonFixture {
    JOHN_ITHACA("101", "John", "Doe", "120 Maple"),
    JANE_NO_ADDRESS("102", "John", "Doe", StringUtils.EMPTY),
    TOM_NO_CITY("103", "Tom", "Doe", "120 Maple", StringUtils.EMPTY),
    DICK_NO_STATE("104", "Dick", "Doe", "120 Maple", "Ithaca", StringUtils.EMPTY),
    HARRY_NO_POSTAL("105", "Harry", "Doe", "120 Maple", "Ithaca", "NY", StringUtils.EMPTY),
    FRANK_NO_COUNTRY("106", "Frank", "Doe", "120 Maple", "Ithaca", "NY", "14850", StringUtils.EMPTY),
    ALLIE_NO_COUNTRY_STATE_ZIP("107", "Frank", "Doe", "120 Maple", "Ithaca", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY),
    TANYA_THIRD_ADDRESS("108", "Tanyaa", "Doe", StringUtils.EMPTY);
    
    public final Person person;
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1) {
        this(employeeId, firstName, lastName, addressLine1, "Ithaca");
        if (StringUtils.equals(employeeId, "108")) {
            ConcurTestablePerson concurPerson = (ConcurTestablePerson) this.person;
            concurPerson.setAddressLine3("somewhere");
        }
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String city) {
        this(employeeId, firstName, lastName, addressLine1, city, "NY");
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String city, String state) {
        this(employeeId, firstName, lastName, addressLine1, city, state, "14850");
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String city, String state, String postalCode) {
        this(employeeId, firstName, lastName, addressLine1, city, state, postalCode, "US");
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String city, String state, String postalCode, String countyCode) {
        this(employeeId, firstName, lastName, addressLine1, StringUtils.EMPTY, StringUtils.EMPTY, city, state, postalCode, countyCode);
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
