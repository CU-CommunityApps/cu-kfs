/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * @author kwk43
 *
 */
public class ProjectInvestigator extends PersistableBusinessObjectBase {

	private Long projInvId;
	private String projectId;
	private String awardProposalId;
	private String investigatorId;
	private Date lastUpdated;
	
	private EzraProposal proposal;
	
	
	/**
	 * @return the projInvId
	 */
	public Long getProjInvId() {
		return projInvId;
	}

	/**
	 * @param projInvId the projInvId to set
	 */
	public void setProjInvId(Long projInvId) {
		this.projInvId = projInvId;
	}

	

	/**
	 * @return the investigatorId
	 */
	public String getInvestigatorId() {
		return investigatorId;
	}

	/**
	 * @param investigatorId the investigatorId to set
	 */
	public void setInvestigatorId(String investigatorId) {
		this.investigatorId = investigatorId;
	}

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
	 * @return the proposal
	 */
	public EzraProposal getProposal() {
		return proposal;
	}

	/**
	 * @param proposal the proposal to set
	 */
	public void setProposal(EzraProposal proposal) {
		this.proposal = proposal;
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

        m.put("projInvId", projInvId);
	    return m;
	}

}
