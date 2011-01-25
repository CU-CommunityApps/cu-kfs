package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

public class Sponsor extends PersistableBusinessObjectBase {

	private Long sponsorId;
	private String sponsorName;
	private String sponsorLabel;
	private Long parentSponsor;
	private Long sourceCode;
	private Date lastUpdated;
	
	/**
	 * @return the sponsorId
	 */
	public Long getSponsorId() {
		return sponsorId;
	}

	/**
	 * @param sponsorId the sponsorId to set
	 */
	public void setSponsorId(Long sponsorId) {
		this.sponsorId = sponsorId;
	}

	/**
	 * @return the sponsorName
	 */
	public String getSponsorName() {
		return sponsorName;
	}

	/**
	 * @param sponsorName the sponsorName to set
	 */
	public void setSponsorName(String sponsorName) {
		this.sponsorName = sponsorName;
	}

	/**
	 * @return the sponsorLabel
	 */
	public String getSponsorLabel() {
		return sponsorLabel;
	}

	/**
	 * @param sponsorLabel the sponsorLabel to set
	 */
	public void setSponsorLabel(String sponsorLabel) {
		this.sponsorLabel = sponsorLabel;
	}

	/**
	 * @return the parentSponsor
	 */
	public Long getParentSponsor() {
		return parentSponsor;
	}

	/**
	 * @param parentSponsor the parentSponsor to set
	 */
	public void setParentSponsor(Long parentSponsor) {
		this.parentSponsor = parentSponsor;
	}

	/**
	 * @return the sourceCode
	 */
	public Long getSourceCode() {
		return sourceCode;
	}

	/**
	 * @param sourceCode the sourceCode to set
	 */
	public void setSourceCode(Long sourceCode) {
		this.sourceCode = sourceCode;
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
	
	@Override
	protected LinkedHashMap toStringMapper() {
		  LinkedHashMap m = new LinkedHashMap();

	        m.put("sponsorId", sponsorId);
	        return m;
	}

//	public Agency createAgencyFromSponsor(Sponsor sponsor) {
//		Agency agency = new Agency();
//		
//		agency.setAgencyNumber(sponsorId.toString());
//		agency.setFullName(sponsorName);
//		agency.setReportingName(sponsor.getSponsorLabel());
//		agency.setReportsToAgencyNumber(sponsor.getParentSponsor().toString());
//		agency.setAgencyTypeCode(sponsor.sourceCode.toString());
//		
//		return agency;
//	}
//	
}
