package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;;

public class KualiDeveloper extends PersistableBusinessObjectBase implements MutableInactivatable {

	private static final long serialVersionUID = 1L;

	private String firstName;
	private String lastName;
	private String positionName;
	private String employeeId;
	//private List<Address> addresses; TODO
	private String address;
	private String socialSecurityNumber; //TODO: mask
	private Boolean active;

	public KualiDeveloper() {
		super();
		active = false;
	}
	
    public boolean isActive() {
        return active;
    }

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

	public void setSocialSecurityNumber(String socialSecurityNumber) {
		this.socialSecurityNumber = socialSecurityNumber;
	}

	public Boolean getActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;		
	}
	
}
