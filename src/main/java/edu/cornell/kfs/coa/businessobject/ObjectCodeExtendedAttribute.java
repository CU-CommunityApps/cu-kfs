/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

/**
 * @author kwk43
 *
 */
public class ObjectCodeExtendedAttribute extends PersistableBusinessObjectExtensionBase {

	private Integer universityFiscalYear;
    private String chartOfAccountsCode;
    private String financialObjectCode;
    private String sunyObjectCode;
	/**
	 * @return the universityFiscalYear
	 */
	public Integer getUniversityFiscalYear() {
		return universityFiscalYear;
	}
	/**
	 * @param universityFiscalYear the universityFiscalYear to set
	 */
	public void setUniversityFiscalYear(Integer universityFiscalYear) {
		this.universityFiscalYear = universityFiscalYear;
	}
	/**
	 * @return the chartOfAccountsCode
	 */
	public String getChartOfAccountsCode() {
		return chartOfAccountsCode;
	}
	/**
	 * @param chartOfAccountsCode the chartOfAccountsCode to set
	 */
	public void setChartOfAccountsCode(String chartOfAccountsCode) {
		this.chartOfAccountsCode = chartOfAccountsCode;
	}
	/**
	 * @return the financialObjectCode
	 */
	public String getFinancialObjectCode() {
		return financialObjectCode;
	}
	/**
	 * @param financialObjectCode the financialObjectCode to set
	 */
	public void setFinancialObjectCode(String financialObjectCode) {
		this.financialObjectCode = financialObjectCode;
	}
	/**
	 * @return the sunyObjectCode
	 */
	public String getSunyObjectCode() {
		return sunyObjectCode;
	}
	/**
	 * @param sunyObjectCode the sunyObjectCode to set
	 */
	public void setSunyObjectCode(String sunyObjectCode) {
		this.sunyObjectCode = sunyObjectCode;
	}
    
    
	
}
