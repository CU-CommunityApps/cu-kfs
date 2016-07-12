package edu.cornell.kfs.coa.businessobject;

import java.math.BigDecimal;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.springframework.beans.BeanUtils;

public class IndirectCostRecoveryAccountChange extends GlobalBusinessObjectDetailBase {
	
	private String documentNumber;
	private Integer indirectCostRecoveryAccountGeneratedIdentifier;
    private String indirectCostRecoveryFinCoaCode;
    private String indirectCostRecoveryAccountNumber;
    private BigDecimal accountLinePercent;
    private boolean active;

    //BO Reference
    private Account indirectCostRecoveryAccount;
    private Chart indirectCostRecoveryChartOfAccounts;
    
    public IndirectCostRecoveryAccountChange() {
    	super();
	}
    

    public IndirectCostRecoveryAccountChange(IndirectCostRecoveryAccountChange icr) {
        BeanUtils.copyProperties(icr, this);
    }

    /**
     * static instantiate an ICRAccount from an ICRAccount
     *
     * @param icrAccount
     * @return
     */
    public static IndirectCostRecoveryAccountChange copyICRAccount(IndirectCostRecoveryAccountChange icrAccount) {
        return new IndirectCostRecoveryAccountChange(icrAccount);
    }

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getIndirectCostRecoveryFinCoaCode() {
		return indirectCostRecoveryFinCoaCode;
	}

	public void setIndirectCostRecoveryFinCoaCode(
			String indirectCostRecoveryFinCoaCode) {
		this.indirectCostRecoveryFinCoaCode = indirectCostRecoveryFinCoaCode;
	}

	public String getIndirectCostRecoveryAccountNumber() {
		return indirectCostRecoveryAccountNumber;
	}

	public void setIndirectCostRecoveryAccountNumber(
			String indirectCostRecoveryAccountNumber) {
		this.indirectCostRecoveryAccountNumber = indirectCostRecoveryAccountNumber;
	}

	public BigDecimal getAccountLinePercent() {
		return accountLinePercent;
	}

	public void setAccountLinePercent(BigDecimal accountLinePercent) {
		this.accountLinePercent = accountLinePercent;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Account getIndirectCostRecoveryAccount() {
		return indirectCostRecoveryAccount;
	}

	public void setIndirectCostRecoveryAccount(Account indirectCostRecoveryAccount) {
		this.indirectCostRecoveryAccount = indirectCostRecoveryAccount;
	}

	public Chart getIndirectCostRecoveryChartOfAccounts() {
		return indirectCostRecoveryChartOfAccounts;
	}

	public void setIndirectCostRecoveryChartOfAccounts(
			Chart indirectCostRecoveryChartOfAccounts) {
		this.indirectCostRecoveryChartOfAccounts = indirectCostRecoveryChartOfAccounts;
	}

	public Integer getIndirectCostRecoveryAccountGeneratedIdentifier() {
		return indirectCostRecoveryAccountGeneratedIdentifier;
	}

	public void setIndirectCostRecoveryAccountGeneratedIdentifier(
			Integer indirectCostRecoveryAccountGeneratedIdentifier) {
		this.indirectCostRecoveryAccountGeneratedIdentifier = indirectCostRecoveryAccountGeneratedIdentifier;
	}
	
	public boolean matchesICRAccount(IndirectCostRecoveryAccount icrAccount) {
		return this.getIndirectCostRecoveryFinCoaCode().equalsIgnoreCase(icrAccount.getIndirectCostRecoveryFinCoaCode())
				&& this.getIndirectCostRecoveryAccountNumber().equalsIgnoreCase(icrAccount.getIndirectCostRecoveryAccountNumber())
				&& this.getAccountLinePercent().equals(icrAccount.getAccountLinePercent());
	}

}