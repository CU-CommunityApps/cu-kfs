/**
 * 
 */
package edu.cornell.kfs.module.cg.businessobject;

import java.sql.Date;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

/**
 * @author kwk43
 *
 */
public class AwardExtendedAttribute extends PersistableBusinessObjectExtensionBase {

	private boolean costShareRequired;
	private Date finalFiscalReportDate;
	private String locAccountId;
	private Long proposalNumber;

	public boolean isCostShareRequired() {
		return costShareRequired;
	}

	public void setCostShareRequired(boolean costShareRequired) {
		this.costShareRequired = costShareRequired;
	}

	public Date getFinalFiscalReportDate() {
		return finalFiscalReportDate;
	}

	public void setFinalFiscalReportDate(Date finalFiscalReportDate) {
		this.finalFiscalReportDate = finalFiscalReportDate;
	}

	public String getLocAccountId() {
		return locAccountId;
	}

	public void setLocAccountId(String locAccountId) {
		this.locAccountId = locAccountId;
	}

	public Long getProposalNumber() {
		return proposalNumber;
	}

	public void setProposalNumber(Long proposalNumber) {
		this.proposalNumber = proposalNumber;
	}
	
	
	
	
}
