package edu.cornell.kfs.coa.businessobject;

import edu.cornell.kfs.sys.businessobject.YearEndPersistableBusinessObjectExtensionBase;

/**
 * @author kco26
 *
 */
public class SubFundGroupExtendedAttribute extends YearEndPersistableBusinessObjectExtensionBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sub-Fund Group Code
	 */
    private String subFundGroupCode;
	
	/**
	 * Sub-Fund Group Detailed Description (see KFSPTS-3923)
	 */
    private String subFundGroupDetailedDescr;
    
    
    /*
     * Class constructor
     */
    public SubFundGroupExtendedAttribute() {
    	
    }
       
	/**
	 * @return the subFundGroupCode Sub-Fund Group Code
	 */
	public String getSubFundGroupCode() {
		return subFundGroupCode;
	}

	/**
	 * @param subFundGroupCode Sub-Fund Group Code
	 */
	public void setSubFundGroupCode(String subFundGroupCode) {
		this.subFundGroupCode = subFundGroupCode;
	}

	/**
	 * @return Sub-Fund Group Detailed Description 
	 */
	public String getSubFundGroupDetailedDescr() {
		return subFundGroupDetailedDescr;
	}

	/**
	 * @param subFundGroupDetailedDescr Sub-Fund Group Detailed Description
	 */
	public void setSubFundGroupDetailedDescr(String subFundGroupDetailedDescr) {
		this.subFundGroupDetailedDescr = subFundGroupDetailedDescr;
	}
	
}
