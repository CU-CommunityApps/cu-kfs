/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

/**
 * @author kwk43
 *
 */
public class EzraProposal extends PersistableBusinessObjectBase {

	private String projectId; //PROJ_ID
	private String awardProposalId; //AWARD_PROP_ID
	private String projectTitle; //TITLE
	private Long sponsorNumber; //SPONSOR_ID
	private String sponsorProjectId; //SPONSOR_PROJ_ID
	private String cfdaNumber; //CFDA_NMBR
	private String status; //STATUS_CD
	private String purpose; //PROJ_FUNCTION_CD
	private Date startDate; //AWD_PROP_START_DT
	private Date stopDate; //AWD_PROP_END_DT
	private KualiDecimal totalAmt; //AWD_PROP_TOTAL
	private String federalPassThrough; //FED_FLOW_THROUGH
	private Long federalPassThroughAgencyNumber; //FLOWTHROUGH_SPONSOR_ID
	private Long departmentId; //DEPT_ID
	private Date lastUpdated;
	
	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}


	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}


	/**
	 * @return the awardProposalId
	 */
	public String getAwardProposalId() {
		return awardProposalId;
	}


	/**
	 * @param awardProposalId the awardProposalId to set
	 */
	public void setAwardProposalId(String awardProposalId) {
		this.awardProposalId = awardProposalId;
	}


	/**
	 * @return the projectTitle
	 */
	public String getProjectTitle() {
		return projectTitle;
	}


	/**
	 * @param projectTitle the projectTitle to set
	 */
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}


	/**
	 * @return the sponsorNumber
	 */
	public Long getSponsorNumber() {
		return sponsorNumber;
	}


	/**
	 * @param sponsorNumber the sponsorNumber to set
	 */
	public void setSponsorNumber(Long sponsorNumber) {
		this.sponsorNumber = sponsorNumber;
	}


	/**
	 * @return the sponsorProjectId
	 */
	public String getSponsorProjectId() {
		return sponsorProjectId;
	}


	/**
	 * @param sponsorProjectId the sponsorProjectId to set
	 */
	public void setSponsorProjectId(String sponsorProjectId) {
		this.sponsorProjectId = sponsorProjectId;
	}


	/**
	 * @return the cfdaNumber
	 */
	public String getCfdaNumber() {
		return cfdaNumber;
	}


	/**
	 * @param cfdaNumber the cfdaNumber to set
	 */
	public void setCfdaNumber(String cfdaNumber) {
		this.cfdaNumber = cfdaNumber;
	}


	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}


	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}


	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}


	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}


	/**
	 * @return the stopDate
	 */
	public Date getStopDate() {
		return stopDate;
	}


	/**
	 * @param stopDate the stopDate to set
	 */
	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}


	/**
	 * @return the totalAmt
	 */
	public KualiDecimal getTotalAmt() {
		return totalAmt;
	}


	/**
	 * @param totalAmt the totalAmt to set
	 */
	public void setTotalAmt(KualiDecimal totalAmt) {
		this.totalAmt = totalAmt;
	}


	/**
	 * @return the federalPassThrough
	 */
	public String getFederalPassThrough() {
		return federalPassThrough;
	}
	/**
	 * @return the federalPassThrough
	 */
	public boolean getFederalPassThroughBoolean() {
		if (federalPassThrough == null || federalPassThrough.equalsIgnoreCase("N")) {
			return false;
		} else
			return true;
	}


	/**
	 * @param federalPassThrough the federalPassThrough to set
	 */
	public void setFederalPassThrough(String federalPassThrough) {
		this.federalPassThrough = federalPassThrough;
	}


	/**
	 * @return the federalPassThroughAgencyNumber
	 */
	public Long getFederalPassThroughAgencyNumber() {
		return federalPassThroughAgencyNumber;
	}


	/**
	 * @param federalPassThroughAgencyNumber the federalPassThroughAgencyNumber to set
	 */
	public void setFederalPassThroughAgencyNumber(
			Long federalPassThroughAgencyNumber) {
		this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
	}

	/**
	 * @return the departmentId
	 */
	public Long getDepartmentId() {
		return departmentId;
	}


	/**
	 * @param departmentId the departmentId to set
	 */
	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
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

	/* (non-Javadoc)
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@Override
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();

        m.put("projectId", projectId);
        m.put("awardProposalId", awardProposalId);
        return m;
	}

}
