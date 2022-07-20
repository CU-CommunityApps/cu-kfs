package edu.cornell.kfs.module.purap.service.impl.fixture;

import org.kuali.kfs.kim.api.identity.Person;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;

public enum JaggaerPersonFixture {
    JOHN_DOE("jd555", "John", "Doe", "IT", "7676", "jd555@llenroc.edu", "(123) 456-7890");

    public final String principalName;
    public final String firstName;
    public final String lastName;
    public final String campusCode;
    public final String departmentCode;
    public final String emailAddress;
    public final String phoneNumber;

    private JaggaerPersonFixture(String principalName, String firstName, String lastName, String campusCode,
            String departmentCode, String emailAddress, String phoneNumber) {
        this.principalName = principalName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.campusCode = campusCode;
        this.departmentCode = departmentCode;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public Person toKimPerson() {
        String fullName = lastName + CUKFSConstants.COMMA_AND_SPACE + firstName;
        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getPrincipalId()).thenReturn(String.valueOf(ordinal()));
        Mockito.when(person.getPrincipalName()).thenReturn(principalName);
        Mockito.when(person.getFirstName()).thenReturn(firstName);
        Mockito.when(person.getLastName()).thenReturn(lastName);
        Mockito.when(person.getName()).thenReturn(fullName);
        Mockito.when(person.getCampusCode()).thenReturn(campusCode);
        Mockito.when(person.getPrimaryDepartmentCode()).thenReturn(departmentCode);
        Mockito.when(person.getEmailAddressUnmasked()).thenReturn(emailAddress);
        Mockito.when(person.getPhoneNumberUnmasked()).thenReturn(phoneNumber);
        return person;
    }

}
