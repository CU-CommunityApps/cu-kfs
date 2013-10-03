package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.coa.businessobject.HigherEducationFunction;


public class CuHigherEducationFunction extends HigherEducationFunction { 
	private static final long serialVersionUID = 1L;
	protected String financialHigherEdFunctionDescription;

	public String getFinancialHigherEdFunctionDescription() {
		return financialHigherEdFunctionDescription;
	}

	public void setFinancialHigherEdFunctionDescription(
			String financialHigherEdFunctionDescription) {
		this.financialHigherEdFunctionDescription = financialHigherEdFunctionDescription;
	}
	
}
