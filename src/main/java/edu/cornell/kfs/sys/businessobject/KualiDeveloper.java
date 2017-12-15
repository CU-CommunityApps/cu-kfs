package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class KualiDeveloper extends PersistableBusinessObjectBase implements MutableInactivatable {

	private String firstName;
	private String lastName;
	private String positionName;
	private String employeeId;
    private String socialSecurityNumber;
	private Boolean active;
    private List<KualiAddress> kualiAddresses;

	public KualiDeveloper() {
		super();
		active = false;
        kualiAddresses = new ArrayList<KualiAddress>();
	}

    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
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

	public String getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

	public void setSocialSecurityNumber(String socialSecurityNumber) {
		this.socialSecurityNumber = socialSecurityNumber;
	}

    public List<KualiAddress> getKualiAddresses() {
        return kualiAddresses;
    }

    public void setKualiAddresses(List<KualiAddress> kualiAddresses) {
        this.kualiAddresses = kualiAddresses;
    }

	
}
