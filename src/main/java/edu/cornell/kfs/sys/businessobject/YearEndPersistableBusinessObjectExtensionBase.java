/**
 * 
 */
package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;



/**
 * @author kwk43
 *
 */
public class YearEndPersistableBusinessObjectExtensionBase extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension, FiscalYearBasedBusinessObject {

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
