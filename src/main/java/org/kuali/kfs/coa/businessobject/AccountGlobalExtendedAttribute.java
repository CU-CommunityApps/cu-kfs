/**
 * 
 */
package org.kuali.kfs.coa.businessobject;

import org.kuali.kfs.module.ld.businessobject.LaborBenefitRateCategory;
import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

/**
 * @author kwk43
 *
 */
public class AccountGlobalExtendedAttribute extends PersistableBusinessObjectExtensionBase {
	
    private String documentNumber;

    private String laborBenefitRateCategoryCode;
    private LaborBenefitRateCategory laborBenefitRateCategory;
	/**
	 * @return the documentNumber
	 */
	public String getDocumentNumber() {
		return documentNumber;
	}
	/**
	 * @param documentNumber the documentNumber to set
	 */
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
	/**
	 * @return the laborBenefitRateCategoryCode
	 */
	public String getLaborBenefitRateCategoryCode() {
		return laborBenefitRateCategoryCode;
	}
	/**
	 * @param laborBenefitRateCategoryCode the laborBenefitRateCategoryCode to set
	 */
	public void setLaborBenefitRateCategoryCode(String laborBenefitRateCategoryCode) {
		this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
	}
	/**
	 * @return the laborBenefitRateCategory
	 */
	public LaborBenefitRateCategory getLaborBenefitRateCategory() {
		return laborBenefitRateCategory;
	}
	/**
	 * @param laborBenefitRateCategory the laborBenefitRateCategory to set
	 */
	public void setLaborBenefitRateCategory(
			LaborBenefitRateCategory laborBenefitRateCategory) {
		this.laborBenefitRateCategory = laborBenefitRateCategory;
	}
    
    
	

}
