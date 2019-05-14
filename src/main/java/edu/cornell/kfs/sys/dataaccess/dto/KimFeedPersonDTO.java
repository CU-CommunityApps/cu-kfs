package edu.cornell.kfs.sys.dataaccess.dto;

/*
 * TODO: Refactor this DTO to conform to our current coding standards!
 */
public class KimFeedPersonDTO {

    //Person Identifiers
    public String CU_PERSON_SID;
    public String NETID;
    public String EMPLID;

    //Name Info
    public String FIRST_NAME;
    public String LAST_NAME;
    public String MIDDLE_NAME;
    public String NAME_SUFFIX;

    //Home Address
    public String HOME_ADDRESS1;
    public String HOME_ADDRESS2;
    public String HOME_ADDRESS3;
    public String HOME_CITY;
    public String HOME_STATE;
    public String HOME_POSTAL;

    //Campus Address
    public String CAMPUS_ADDRESS1;
    public String CAMPUS_ADDRESS2;
    public String CAMPUS_ADDRESS3;
    public String CAMPUS_CITY;
    public String CAMPUS_STATE;
    public String CAMPUS_POSTAL;

    //Email
    public String EMAIL_ADDRESS;

    //Home Phone
    //public String HOME_PHONE;

    //Campus Phone
    public String CAMPUS_PHONE;

    //is person an academic
    public char ACADEMIC;

    //is person a faculty member
    public char FACULTY;

    //is person an affiliate 
    public char AFFILIATE;

    //is person an exception 
    public char DCEXP1;

    //is person a staff member
    public char STAFF;

    //is person a student member
    public char STUDENT;

    //is person a alumni
    public char ALUMNI;

    //Organization
    public String PRIMARY_ORG_CODE;

    // The indicator of which affiliation is primary.
    public char PRIMARY_AFFILIATION_VALUE;

    //Primary AFFLIATION
    public String PRIMARY_AFFILIATION_ACADEMIC;
    public String PRIMARY_AFFILIATION_FACULTY;
    public String PRIMARY_AFFILIATION_AFFILIATE;
    public String PRIMARY_AFFILIATION_EXCEPTION;
    public String PRIMARY_AFFILIATION_STAFF;
    public String PRIMARY_AFFILIATION_STUDENT;
    public String PRIMARY_AFFILIATION_ALUMNI;

    //Primary EMPLOYMENT (in case primary/default affiliation is not supposed to have employment info)
    public String PRIMARY_EMPLOYMENT_FACULTY;
    public String PRIMARY_EMPLOYMENT_AFFILIATE;
    public String PRIMARY_EMPLOYMENT_STAFF;

    //FERPA Flag
    public String LDAP_SUPPRESS;

    //Active indicator
    public String ACTIVE;

}
