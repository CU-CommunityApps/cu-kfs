package org.kuali.kfs.module.ld.businessobject;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class BenefitsCalculationExtendedAttribute extends PersistableBusinessObjectExtensionBase{

	private Integer universityFiscalYear;
	private String chartOfAccountsCode;
	private String positionBenefitTypeCode;
	private String laborBenefitRateCategoryCode;

	private LaborBenefitRateCategory laborBenefitRateCategory;



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
	 * @return the positionBenefitTypeCode
	 */
	public String getPositionBenefitTypeCode() {
		return positionBenefitTypeCode;
	}

	/**
	 * @param positionBenefitTypeCode the positionBenefitTypeCode to set
	 */
	public void setPositionBenefitTypeCode(String positionBenefitTypeCode) {
		this.positionBenefitTypeCode = positionBenefitTypeCode;
	}

	/**
	 * Gets the laborBenefitRateCategoryCode attribute. 
	 * @return Returns the laborBenefitRateCategoryCode.
	 */
	public String getLaborBenefitRateCategoryCode() {
		return laborBenefitRateCategoryCode;
	}

	/**
	 * Sets the laborBenefitRateCategoryCode attribute value.
	 * @param laborBenefitRateCategoryCode The laborBenefitRateCategoryCode to set.
	 */
	public void setLaborBenefitRateCategoryCode(String laborBenefitRateCategoryCode) {
		this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
	}

	/**
	 * Gets the laborBenefitRateCategory attribute. 
	 * @return Returns the laborBenefitRateCategory.
	 */
	public LaborBenefitRateCategory getLaborBenefitRateCategory() {
		return laborBenefitRateCategory;
	}

	/**
	 * Sets the laborBenefitRateCategory attribute value.
	 * @param laborBenefitRateCategory The laborBenefitRateCategory to set.
	 */
	public void setLaborBenefitRateCategory(LaborBenefitRateCategory laborBenefitRateCategory) {
		this.laborBenefitRateCategory = laborBenefitRateCategory;
	}
}
