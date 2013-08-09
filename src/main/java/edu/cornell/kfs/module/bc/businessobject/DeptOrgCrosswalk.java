package edu.cornell.kfs.module.bc.businessobject;

import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;



public class DeptOrgCrosswalk extends PersistableBusinessObjectExtensionBase {
	private static final long serialVersionUID = 1L;

	
	private String hrDepartment;
	private String hrDepartmentName;
	private String orgCode;
	private String orgName;
	private boolean active;

	
	
	public String getHrDepartment() {
		return hrDepartment;
	}
	public void setHrDepartment(String hrDepartment) {
		this.hrDepartment = hrDepartment;
	}
	public String getHrDepartmentName() {
		return hrDepartmentName;
	}
	public void setHrDepartmentName(String hrDepartmentName) {
		this.hrDepartmentName = hrDepartmentName;
	}
	public String getOrgCode() {
		return orgCode;
	}
	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	
	
	
}
