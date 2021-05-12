package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class MajorReportingCategory extends PersistableBusinessObjectBase implements MutableInactivatable {

	private static final long serialVersionUID = -6542680440925293438L;
	
	
	private String majorReportingCategoryCode;
    private String majorReportingCategoryName;
    private String majorReportingCategoryDescription;
    private boolean active;


    public MajorReportingCategory() {
    }
    
    
    /**
     * Gets the active attribute.
     * 
     * @return Returns the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
	/**
	 * @return the majorReportingCategoryCode
	 */
	public String getMajorReportingCategoryCode() {
		return majorReportingCategoryCode;
	}
	
	/**
	 * @param majorReportingCategoryCode the majorReportingCategoryCode to set
	 */
	public void setMajorReportingCategoryCode(String majorReportingCategoryCode) {
		this.majorReportingCategoryCode = majorReportingCategoryCode;
	}
	
	/**
	 * @return the majorReportingCategoryName
	 */
	public String getMajorReportingCategoryName() {
		return majorReportingCategoryName;
	}
	
	/**
	 * @param majorReportingCategoryName the majorReportingCategoryName to set
	 */
	public void setMajorReportingCategoryName(String majorReportingCategoryName) {
		this.majorReportingCategoryName = majorReportingCategoryName;
	}
	
	/**
	 * @return the majorReportingCategoryDescription
	 */
	public String getMajorReportingCategoryDescription() {
		return majorReportingCategoryDescription;
	}
	
	/**
	 * @param majorReportingCategoryDescription the majorReportingCategoryDescription to set
	 */
	public void setMajorReportingCategoryDescription(String majorReportingCategoryDescription) {
		this.majorReportingCategoryDescription = majorReportingCategoryDescription;
	}

	/**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("majorReportingCategoryCode", this.majorReportingCategoryCode);
        return m;
    }

}