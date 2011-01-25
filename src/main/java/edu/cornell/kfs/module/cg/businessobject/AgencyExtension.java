package edu.cornell.kfs.module.cg.businessobject;

import java.sql.Date;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class AgencyExtension extends PersistableBusinessObjectExtensionBase {

	 private String agencyNumber;
	 private Date lastUpdated;
	
	 /**
	 * @return the agencyNumber
	 */
	public String getAgencyNumber() {
		return agencyNumber;
	}
	
	/**
	 * @param agencyNumber the agencyNumber to set
	 */
	public void setAgencyNumber(String agencyNumber) {
		this.agencyNumber = agencyNumber;
	}
	
	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}
	 
	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	 
	
}
