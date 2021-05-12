package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class AppropriationAccount extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 3305823730461886270L;

   
    private String subFundGroupCode;
    private String appropriationAccountNumber;
    private String appropriationAccountName;
    private String projectNumber;
    private boolean active;

    private SubFundGroup subFundGroup;

    public AppropriationAccount() {
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
	 * @return the appropriationAccountNumber
	 */
	public String getAppropriationAccountNumber() {
		return appropriationAccountNumber;
	}

	/**
	 * @param appropriationAccountNumber the appropriationAccountNumber to set
	 */
	public void setAppropriationAccountNumber(String appropriationAccountNumber) {
		this.appropriationAccountNumber = appropriationAccountNumber;
	}

	/**
	 * @return the appropriationAccountName
	 */
	public String getAppropriationAccountName() {
		return appropriationAccountName;
	}

	/**
	 * @param appropriationAccountName the appropriationAccountName to set
	 */
	public void setAppropriationAccountName(String appropriationAccountName) {
		this.appropriationAccountName = appropriationAccountName;
	}

	/**
	 * @return the projectNumber
	 */
	public String getProjectNumber() {
		return projectNumber;
	}

	/**
	 * @param projectNumber the projectNumber to set
	 */
	public void setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
	}

	/**
	 * @return the subFundGroupCode
	 */
	public String getSubFundGroupCode() {
		return subFundGroupCode;
	}

	/**
	 * @param subFundGroupCode the subFundGroupCode to set
	 */
	public void setSubFundGroupCode(String subFundGroupCode) {
		this.subFundGroupCode = subFundGroupCode;
	}

	/**
	 * @return the subFundGroup
	 */
	public SubFundGroup getSubFundGroup() {
		return subFundGroup;
	}

	/**
	 * @param subFundGroup the subFundGroup to set
	 */
	public void setSubFundGroup(SubFundGroup subFundGroup) {
		this.subFundGroup = subFundGroup;
	}

	/**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("appropriationAccountNumber", this.appropriationAccountNumber);
        m.put("subFundGroupCode", this.subFundGroupCode);
        return m;
    }

}