
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "firstName", "lastName", "uniqueName", "userPrintableName", "userOrganizationName",
        "userOrganizationId", "userBusinessUnit", "userOrganizationDepartment", "uniqueUserName", "userEmail", "user",
        "userPhone" })
@XmlRootElement(name = "ExtrinsicInfo")
public class ExtrinsicInfo {

    @XmlElement(name = "FirstName")
    protected FirstName firstName;
    @XmlElement(name = "LastName")
    protected LastName lastName;
    @XmlElement(name = "UniqueName")
    protected UniqueName uniqueName;
    @XmlElement(name = "UserPrintableName")
    protected UserPrintableName userPrintableName;
    @XmlElement(name = "UserOrganizationName")
    protected UserOrganizationName userOrganizationName;
    @XmlElement(name = "UserOrganizationId")
    protected UserOrganizationId userOrganizationId;
    @XmlElement(name = "UserBusinessUnit")
    protected UserBusinessUnit userBusinessUnit;
    @XmlElement(name = "UserOrganizationDepartment")
    protected UserOrganizationDepartment userOrganizationDepartment;
    @XmlElement(name = "UniqueUserName")
    protected UniqueUserName uniqueUserName;
    @XmlElement(name = "UserEmail")
    protected UserEmail userEmail;
    @XmlElement(name = "User")
    protected User user;
    @XmlElement(name = "UserPhone")
    protected UserPhone userPhone;

    public FirstName getFirstName() {
        return firstName;
    }

    public void setFirstName(FirstName value) {
        this.firstName = value;
    }

    public LastName getLastName() {
        return lastName;
    }

    public void setLastName(LastName value) {
        this.lastName = value;
    }

    public UniqueName getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(UniqueName value) {
        this.uniqueName = value;
    }

    public UserPrintableName getUserPrintableName() {
        return userPrintableName;
    }

    public void setUserPrintableName(UserPrintableName value) {
        this.userPrintableName = value;
    }

    public UserOrganizationName getUserOrganizationName() {
        return userOrganizationName;
    }

    public void setUserOrganizationName(UserOrganizationName value) {
        this.userOrganizationName = value;
    }

    public UserOrganizationId getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(UserOrganizationId value) {
        this.userOrganizationId = value;
    }

    public UserBusinessUnit getUserBusinessUnit() {
        return userBusinessUnit;
    }

    public void setUserBusinessUnit(UserBusinessUnit value) {
        this.userBusinessUnit = value;
    }

    public UserOrganizationDepartment getUserOrganizationDepartment() {
        return userOrganizationDepartment;
    }

    public void setUserOrganizationDepartment(UserOrganizationDepartment value) {
        this.userOrganizationDepartment = value;
    }

    public UniqueUserName getUniqueUserName() {
        return uniqueUserName;
    }

    public void setUniqueUserName(UniqueUserName value) {
        this.uniqueUserName = value;
    }

    public UserEmail getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(UserEmail value) {
        this.userEmail = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        this.user = value;
    }

    public UserPhone getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(UserPhone value) {
        this.userPhone = value;
    }

}
