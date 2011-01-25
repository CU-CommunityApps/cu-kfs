package edu.cornell.kfs.module.cg.businessobject;

import java.sql.Date;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class AwardExtension extends PersistableBusinessObjectExtensionBase {

	private Long proposalNumber;
	private Date lastUpdated;
	
	 /**
	 * @return the proposalNumber
	 */
	public Long getProposalNumber() {
		return proposalNumber;
	}
	
	/**
	 * @param proposalNumber the proposalNumber to set
	 */
	public void setProposalNumber(Long proposalNumber) {
		this.proposalNumber = proposalNumber;
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
