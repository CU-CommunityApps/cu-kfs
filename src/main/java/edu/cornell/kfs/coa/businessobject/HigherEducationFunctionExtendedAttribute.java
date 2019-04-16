package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

public class HigherEducationFunctionExtendedAttribute extends PersistableBusinessObjectExtensionBase {
	protected String financialHigherEdFunctionCd;
	protected String financialHigherEdFunctionDescription;

	public HigherEducationFunctionExtendedAttribute() {
		super();
	}

	public String getFinancialHigherEdFunctionCd() {
		return financialHigherEdFunctionCd;
	}

	public void setFinancialHigherEdFunctionCd(String financialHigherEdFunctionCd) {
		this.financialHigherEdFunctionCd = financialHigherEdFunctionCd;
	}

	public String getFinancialHigherEdFunctionDescription() {
		return financialHigherEdFunctionDescription;
	}

	public void setFinancialHigherEdFunctionDescription(String financialHigherEdFunctionDescription) {
		this.financialHigherEdFunctionDescription = financialHigherEdFunctionDescription;
	}

}
