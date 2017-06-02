package edu.cornell.kfs.concur.batch.service.impl.fixture;

import java.util.List;
import java.util.Map;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;

public class ConcurTestablePerson implements Person {
    private static final long serialVersionUID = 156464309384726378L;
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String state;
    private String postalCode;
    private String countryCode;
    
    ConcurTestablePerson(String employeeId, String firstName, String lastName, String addressLine1, String addressLine2, String addressLine3,
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

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPrincipalId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPrincipalName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEntityId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEntityTypeCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getFirstNameUnmasked() {
        return firstName;
    }

    @Override
    public String getMiddleName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMiddleNameUnmasked() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getLastNameUnmasked() {
        return lastName;
    }

    @Override
    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public String getNameUnmasked() {
        return getName();
    }

    @Override
    public String getEmailAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEmailAddressUnmasked() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAddressLine1() {
        return addressLine1;
    }

    @Override
    public String getAddressLine1Unmasked() {
        return addressLine1;
    }

    @Override
    public String getAddressLine2() {
        return addressLine2;
    }

    @Override
    public String getAddressLine2Unmasked() {
        return addressLine2;
    }

    @Override
    public String getAddressLine3() {
        return addressLine3;
    }

    @Override
    public String getAddressLine3Unmasked() {
        return addressLine3;
    }

    @Override
    public String getAddressCity() {
        return city;
    }

    @Override
    public String getAddressCityUnmasked() {
        return city;
    }

    @Override
    public String getAddressStateProvinceCode() {
        return state;
    }

    @Override
    public String getAddressStateProvinceCodeUnmasked() {
        return state;
    }

    @Override
    public String getAddressPostalCode() {
        return postalCode;
    }

    @Override
    public String getAddressPostalCodeUnmasked() {
        return postalCode;
    }

    @Override
    public String getAddressCountryCode() {
        return countryCode;
    }

    @Override
    public String getAddressCountryCodeUnmasked() {
        return countryCode;
    }

    @Override
    public String getPhoneNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPhoneNumberUnmasked() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCampusCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getExternalIdentifiers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasAffiliationOfType(String affiliationTypeCode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getCampusCodesForAffiliationOfType(String affiliationTypeCode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEmployeeStatusCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEmployeeTypeCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KualiDecimal getBaseSalaryAmount() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExternalId(String externalIdentifierTypeCode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPrimaryDepartmentCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return false;
    }

}
