package edu.cornell.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PersonData extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = 1L;
	private String netID;
    private String personName;
    private String emailAddress;
    private String phoneNumber;
    private String campusAddress;
    

    @SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
        return null;
    }


    public String getNetID() {
        return netID;
    }


    public void setNetID(String netID) {
        this.netID = netID;
    }


    public String getPersonName() {
        return personName;
    }


    public void setPersonName(String personName) {
        this.personName = personName;
    }


    public String getEmailAddress() {
        return emailAddress;
    }


    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getCampusAddress() {
        return campusAddress;
    }


    public void setCampusAddress(String campusAddress) {
        this.campusAddress = campusAddress;
    }

}
