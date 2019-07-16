/**
 * 
 */
package edu.cornell.kfs.module.cg.businessobject;

import java.sql.Date;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;


/**
 * @author kwk43
 *
 */
public class AwardExtendedAttribute extends PersistableBusinessObjectExtensionBase {

	private static final long serialVersionUID = 1L;
	private boolean costShareRequired;
	private boolean finalFinancialReportRequired;
	private Date finalFiscalReportDate;
	private String locAccountId;
	private String proposalNumber;
	private Date budgetBeginningDate;
	private Date budgetEndingDate;
	private KualiDecimal budgetTotalAmount;
	private boolean everify;
	private String primeAgreementNumber;
	private String autoApproveReason;
	private String purchasOrderNumber;

	public boolean isCostShareRequired() {
		return costShareRequired;
	}

	public void setCostShareRequired(boolean costShareRequired) {
		this.costShareRequired = costShareRequired;
	}
		
	public boolean isFinalFinancialReportRequired() {
		return finalFinancialReportRequired;
	}

	public void setFinalFinancialReportRequired(boolean finalFinancialReportRequired) {
		this.finalFinancialReportRequired = finalFinancialReportRequired;
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

	public String getProposalNumber() {
		return proposalNumber;
	}

	public void setProposalNumber(String proposalNumber) {
		this.proposalNumber = proposalNumber;
	}

	public Date getBudgetBeginningDate() {
		return budgetBeginningDate;
	}

	public void setBudgetBeginningDate(Date budgetBeginningDate) {
		this.budgetBeginningDate = budgetBeginningDate;
	}

	public Date getBudgetEndingDate() {
		return budgetEndingDate;
	}

	public void setBudgetEndingDate(Date budgetEndingDate) {
		this.budgetEndingDate = budgetEndingDate;
	}

	public KualiDecimal getBudgetTotalAmount() {
		return budgetTotalAmount;
	}

	public void setBudgetTotalAmount(KualiDecimal budgetTotalAmount) {
		this.budgetTotalAmount = budgetTotalAmount;
	}
	
	public boolean isEverify() {
	    return everify;
	}
	
	public void setEverify(boolean everify) {
	    this.everify = everify;
	}

    public String getPrimeAgreementNumber() {
        return primeAgreementNumber;
    }

    public void setPrimeAgreementNumber(String primeAgreementNumber) {
        this.primeAgreementNumber = primeAgreementNumber;
    }

    public String getAutoApproveReason() {
        return autoApproveReason;
    }

    public void setAutoApproveReason(String autoApproveReason) {
        this.autoApproveReason = autoApproveReason;
    }

    public String getPurchasOrderNumber() {
        return purchasOrderNumber;
    }

    public void setPurchasOrderNumber(String purchasOrderNumber) {
        this.purchasOrderNumber = purchasOrderNumber;
    }

}
