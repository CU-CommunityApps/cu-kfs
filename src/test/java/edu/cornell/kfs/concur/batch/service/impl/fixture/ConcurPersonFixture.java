package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kim.impl.identity.Person;

public enum ConcurPersonFixture {
    JOHN_STATE_COUNTRY("101", "John", "Doe", "NY", "US"),
    JANE_STATE("102", "Janee", "Doe", "NY", StringUtils.EMPTY),
    TOM_COUNTRY("103", "Jane", "Doe", StringUtils.EMPTY, "US"),
    TINA_INVALID_ADDRESS("104", "Tina", "Doe", StringUtils.EMPTY, StringUtils.EMPTY);
    
    public final String employeeId;
    public final String firstName;
    public final String lastName;
    public final String addressLine1;
    public final String addressLine2;
    public final String addressLine3;
    public final String city;
    public final String state;
    public final String postalCode;
    public final String countryCode;
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String state, String countryCode) {
        this(employeeId, firstName, lastName, "120 Maple Ave", StringUtils.EMPTY, StringUtils.EMPTY, "Ithaca", state, "13068", countryCode);
    }
    
    private ConcurPersonFixture(String employeeId, String firstName, String lastName, String addressLine1, String addressLine2, String addressLine3,
            String city, String state, String postalCode, String countryCode) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
    }

    public Person toPerson() {
        Person person = new Person();
        person.setPrincipalId(String.valueOf(ordinal()));
        person.setEmployeeId(employeeId);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setAddressLine1(addressLine1);
        person.setAddressLine2(addressLine2);
        person.setAddressLine3(addressLine3);
        person.setAddressCity(city);
        person.setAddressStateProvinceCode(state);
        person.setAddressPostalCode(postalCode);
        person.setAddressCountryCode(countryCode);
        person.setActive(true);
        return person;
    }

    public static Person personFromEmployeeId(String employeeId) {
        for (ConcurPersonFixture currentFixture : ConcurPersonFixture.values()) {
            if (StringUtils.equalsIgnoreCase(currentFixture.employeeId, employeeId)) {
                return currentFixture.toPerson();
            }
        }
        throw new IllegalArgumentException("Unknown employee ID: " + employeeId);
    }
}
