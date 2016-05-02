/**
 * 
 */
package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;



/**
 * @author kwk43
 *
 */
public class YearEndPersistableBusinessObjectExtensionBase extends PersistableBusinessObjectExtensionBase implements FiscalYearBasedBusinessObject {

	private Integer universityFiscalYear;

	
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
	
}
