/**
 * 
 */
package edu.cornell.kfs.module.cg.businessobject;

import java.sql.Date;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;
import org.kuali.kfs.module.cg.businessobject.LetterOfCreditFundGroup;


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

    private String letterOfCreditFundGroupCode;
    private LetterOfCreditFundGroup letterOfCreditFundGroup;
    private transient String invoiceLink;

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

    public String getLetterOfCreditFundGroupCode() {
        return letterOfCreditFundGroupCode;
    }

    public void setLetterOfCreditFundGroupCode(String letterOfCreditFundGroupCode) {
        this.letterOfCreditFundGroupCode = letterOfCreditFundGroupCode;
    }

    public LetterOfCreditFundGroup getLetterOfCreditFundGroup() {
        return letterOfCreditFundGroup;
    }

    public void setLetterOfCreditFundGroup(LetterOfCreditFundGroup letterOfCreditFundGroup) {
        this.letterOfCreditFundGroup = letterOfCreditFundGroup;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public void setInvoiceLink(String invoiceLink) {
        this.invoiceLink = invoiceLink;
    }

}
