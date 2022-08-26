package edu.cornell.kfs.concur.batch.service.impl.fixture;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.api.identity.Person;

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
    }

    @Override
    public String getPrincipalId() {
        return null;
    }

    @Override
    public String getPrincipalName() {
        return null;
    }

    @Override
    public String getEntityId() {
        return null;
    }

    @Override
    public String getEntityTypeCode() {
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
        return null;
    }

    @Override
    public String getMiddleNameUnmasked() {
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
        return getFirstName() + StringUtils.SPACE + getLastName();
    }

    @Override
    public String getNameUnmasked() {
        return getName();
    }

    @Override
    public String getEmailAddress() {
        return null;
    }

    @Override
    public String getEmailAddressUnmasked() {
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
    
    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
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
        return null;
    }

    @Override
    public String getPhoneNumberUnmasked() {
        return null;
    }

    @Override
    public String getCampusCode() {
        return null;
    }

    @Override
    public Map<String, String> getExternalIdentifiers() {
        return null;
    }

    @Override
    public String getEmployeeStatusCode() {
        return null;
    }

    @Override
    public String getEmployeeTypeCode() {
        return null;
    }

    @Override
    public KualiDecimal getBaseSalaryAmount() {
        return null;
    }

    @Override
    public String getExternalId(String externalIdentifierTypeCode) {
        return null;
    }

    @Override
    public String getPrimaryDepartmentCode() {
        return null;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public String getLookupRoleNamespaceCode() {
        return null;
    }

    @Override
    public String getLookupRoleName() {
        return null;
    }

}
