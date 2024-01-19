package edu.cornell.kfs.kim.businessobject;

import java.text.MessageFormat;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class EdwPerson extends TransientBusinessObjectBase {
    private static final long serialVersionUID = 1L;

    private static final String TO_STRING_FORMAT =
            "EdwPerson[[NetID:''{0}'', CU Person ID:''{1}'', Employee ID:''{2}'']]";

    private String cuPersonId;
    private String employeeId;
    private String netId;
    private String nationalId;
    private String academicAffil;
    private String staffAffil;
    private String facultyAffil;
    private String studentAffil;
    private String alumniAffil;
    private String affiliateAffil;
    private String exceptionAffil;
    private String primaryAffiliation;
    private String suppressAddress;
    private String ldapSuppress;
    private String name;
    private String namePrefix;
    private String nameSuffix;
    private String lastName;
    private String firstName;
    private String middleName;
    private String preferredName;
    private String homeAddressLine1;
    private String homeAddressLine2;
    private String homeAddressLine3;
    private String homeCity;
    private String homeState;
    private String homePostalCode;
    private String homeCountryCode;
    private String homePhone;
    private String emailAddress;
    private String campusAddress;
    private String campusCity;
    private String campusState;
    private String campusPostalCode;
    private String campusPhone;
    private String primaryJobCode;
    private String primaryDepartmentId;
    private String primaryUnitId;
    private String primaryOrgCode;
    private String primaryEmploymentStatus;
    private String active;

    public String getCuPersonId() {
        return cuPersonId;
    }

    public void setCuPersonId(String cuPersonId) {
        this.cuPersonId = cuPersonId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getAcademicAffil() {
        return academicAffil;
    }

    public void setAcademicAffil(String academicAffil) {
        this.academicAffil = academicAffil;
    }

    public String getStaffAffil() {
        return staffAffil;
    }

    public void setStaffAffil(String staffAffil) {
        this.staffAffil = staffAffil;
    }

    public String getFacultyAffil() {
        return facultyAffil;
    }

    public void setFacultyAffil(String facultyAffil) {
        this.facultyAffil = facultyAffil;
    }

    public String getStudentAffil() {
        return studentAffil;
    }

    public void setStudentAffil(String studentAffil) {
        this.studentAffil = studentAffil;
    }

    public String getAlumniAffil() {
        return alumniAffil;
    }

    public void setAlumniAffil(String alumniAffil) {
        this.alumniAffil = alumniAffil;
    }

    public String getAffiliateAffil() {
        return affiliateAffil;
    }

    public void setAffiliateAffil(String affiliateAffil) {
        this.affiliateAffil = affiliateAffil;
    }

    public String getExceptionAffil() {
        return exceptionAffil;
    }

    public void setExceptionAffil(String exceptionAffil) {
        this.exceptionAffil = exceptionAffil;
    }

    public String getPrimaryAffiliation() {
        return primaryAffiliation;
    }

    public void setPrimaryAffiliation(String primaryAffiliation) {
        this.primaryAffiliation = primaryAffiliation;
    }

    public String getSuppressAddress() {
        return suppressAddress;
    }

    public void setSuppressAddress(String suppressAddress) {
        this.suppressAddress = suppressAddress;
    }

    public String getLdapSuppress() {
        return ldapSuppress;
    }

    public void setLdapSuppress(String ldapSuppress) {
        this.ldapSuppress = ldapSuppress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getHomeAddressLine1() {
        return homeAddressLine1;
    }

    public void setHomeAddressLine1(String homeAddressLine1) {
        this.homeAddressLine1 = homeAddressLine1;
    }

    public String getHomeAddressLine2() {
        return homeAddressLine2;
    }

    public void setHomeAddressLine2(String homeAddressLine2) {
        this.homeAddressLine2 = homeAddressLine2;
    }

    public String getHomeAddressLine3() {
        return homeAddressLine3;
    }

    public void setHomeAddressLine3(String homeAddressLine3) {
        this.homeAddressLine3 = homeAddressLine3;
    }

    public String getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(String homeCity) {
        this.homeCity = homeCity;
    }

    public String getHomeState() {
        return homeState;
    }

    public void setHomeState(String homeState) {
        this.homeState = homeState;
    }

    public String getHomePostalCode() {
        return homePostalCode;
    }

    public void setHomePostalCode(String homePostalCode) {
        this.homePostalCode = homePostalCode;
    }

    public String getHomeCountryCode() {
        return homeCountryCode;
    }

    public void setHomeCountryCode(String homeCountryCode) {
        this.homeCountryCode = homeCountryCode;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCampusAddress() {
        return campusAddress;
    }

    public void setCampusAddress(String campusAddress) {
        this.campusAddress = campusAddress;
    }

    public String getCampusCity() {
        return campusCity;
    }

    public void setCampusCity(String campusCity) {
        this.campusCity = campusCity;
    }

    public String getCampusState() {
        return campusState;
    }

    public void setCampusState(String campusState) {
        this.campusState = campusState;
    }

    public String getCampusPostalCode() {
        return campusPostalCode;
    }

    public void setCampusPostalCode(String campusPostalCode) {
        this.campusPostalCode = campusPostalCode;
    }

    public String getCampusPhone() {
        return campusPhone;
    }

    public void setCampusPhone(String campusPhone) {
        this.campusPhone = campusPhone;
    }

    public String getPrimaryJobCode() {
        return primaryJobCode;
    }

    public void setPrimaryJobCode(String primaryJobCode) {
        this.primaryJobCode = primaryJobCode;
    }

    public String getPrimaryDepartmentId() {
        return primaryDepartmentId;
    }

    public void setPrimaryDepartmentId(String primaryDepartmentId) {
        this.primaryDepartmentId = primaryDepartmentId;
    }

    public String getPrimaryUnitId() {
        return primaryUnitId;
    }

    public void setPrimaryUnitId(String primaryUnitId) {
        this.primaryUnitId = primaryUnitId;
    }

    public String getPrimaryOrgCode() {
        return primaryOrgCode;
    }

    public void setPrimaryOrgCode(String primaryOrgCode) {
        this.primaryOrgCode = primaryOrgCode;
    }

    public String getPrimaryEmploymentStatus() {
        return primaryEmploymentStatus;
    }

    public void setPrimaryEmploymentStatus(String primaryEmploymentStatus) {
        this.primaryEmploymentStatus = primaryEmploymentStatus;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return MessageFormat.format(TO_STRING_FORMAT, netId, cuPersonId, employeeId);
    }

}
