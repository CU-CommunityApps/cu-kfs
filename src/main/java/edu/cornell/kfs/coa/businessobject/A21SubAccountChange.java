package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryType;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;

public class A21SubAccountChange extends GlobalBusinessObjectDetailBase {
	
	protected String documentNumber;
	
    protected String indirectCostRecoveryTypeCode;
    protected String financialIcrSeriesIdentifier;
    protected boolean offCampusCode;
    protected String costShareChartOfAccountCode;
    protected String costShareSourceAccountNumber;
    protected String costShareSourceSubAccountNumber;
    
    protected Chart costShareChartOfAccount;
    protected Account costShareAccount;
    protected SubAccount costShareSourceSubAccount;
    protected IndirectCostRecoveryType indirectCostRecoveryType;

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getIndirectCostRecoveryTypeCode() {
		return indirectCostRecoveryTypeCode;
	}

	public void setIndirectCostRecoveryTypeCode(String indirectCostRecoveryTypeCode) {
		this.indirectCostRecoveryTypeCode = indirectCostRecoveryTypeCode;
	}

	public String getFinancialIcrSeriesIdentifier() {
		return financialIcrSeriesIdentifier;
	}

	public void setFinancialIcrSeriesIdentifier(String financialIcrSeriesIdentifier) {
		this.financialIcrSeriesIdentifier = financialIcrSeriesIdentifier;
	}

	public boolean isOffCampusCode() {
		return offCampusCode;
	}

	public void setOffCampusCode(boolean offCampusCode) {
		this.offCampusCode = offCampusCode;
	}

	public String getCostShareChartOfAccountCode() {
		return costShareChartOfAccountCode;
	}

	public void setCostShareChartOfAccountCode(String costShareChartOfAccountCode) {
		this.costShareChartOfAccountCode = costShareChartOfAccountCode;
	}

	public String getCostShareSourceAccountNumber() {
		return costShareSourceAccountNumber;
	}

	public void setCostShareSourceAccountNumber(String costShareSourceAccountNumber) {
		this.costShareSourceAccountNumber = costShareSourceAccountNumber;
	}

	public String getCostShareSourceSubAccountNumber() {
		return costShareSourceSubAccountNumber;
	}

	public void setCostShareSourceSubAccountNumber(
			String costShareSourceSubAccountNumber) {
		this.costShareSourceSubAccountNumber = costShareSourceSubAccountNumber;
	}

	public Chart getCostShareChartOfAccount() {
		return costShareChartOfAccount;
	}

	public void setCostShareChartOfAccount(Chart costShareChartOfAccount) {
		this.costShareChartOfAccount = costShareChartOfAccount;
	}

	public Account getCostShareAccount() {
		return costShareAccount;
	}

	public void setCostShareAccount(Account costShareAccount) {
		this.costShareAccount = costShareAccount;
	}

	public SubAccount getCostShareSourceSubAccount() {
		return costShareSourceSubAccount;
	}

	public void setCostShareSourceSubAccount(SubAccount costShareSourceSubAccount) {
		this.costShareSourceSubAccount = costShareSourceSubAccount;
	}

	public IndirectCostRecoveryType getIndirectCostRecoveryType() {
		return indirectCostRecoveryType;
	}

	public void setIndirectCostRecoveryType(
			IndirectCostRecoveryType indirectCostRecoveryType) {
		this.indirectCostRecoveryType = indirectCostRecoveryType;
	}

}
