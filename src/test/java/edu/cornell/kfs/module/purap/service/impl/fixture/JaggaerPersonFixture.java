package edu.cornell.kfs.module.purap.service.impl.fixture;

import org.kuali.kfs.kim.api.identity.Person;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;

public enum JaggaerPersonFixture {
    JOHN_DOE("jd555", "John", "Doe", "IT", "7676", "jd555@llenroc.edu", "(123) 456-7890"),
    JANE_JILL("jj123", "Jane", "Jill", "IT", "4444", "jj123@llenroc.edu", "(555) 666-7777"),
    BOB_SMITH("bqs88", "Bob", "Smith", "IT", "1909", "bqs88@llenroc.edu", "(432) 432-4322"),
    MARY_SMITH("ms4", "Mary", "Smith", "IT", "1357", "ms4@llenroc.edu", "(444) 333-2222"),
    JACK_PAUL("jp95", "Jack", "Paul", "IT", "5577", "jp95@llenroc.edu", "(987) 654-3210");

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
