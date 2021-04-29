package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SubFundProgram extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 3304324942061886270L;

    
    private String programCode;
    private String programDescription;
    private String programName;
    private String subFundGroupCode;
    
    private boolean active;

    private SubFundGroup subFundGroup;
    
    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public SubFundGroup getSubFundGroup() {
        return subFundGroup;
    }

    public void setSubFundGroup(SubFundGroup subFundGroup) {
        this.subFundGroup = subFundGroup;
    }

    public SubFundProgram() {
    }
    
    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    /**
     * Gets the programDescription attribute. 
     * @return Returns the programDescription.
     */
    public String getProgramDescription() {
        return programDescription;
    }

    /**
     * Sets the programDescription attribute value.
     * @param programDescription The programDescription to set.
     */
    public void setProgramDescription(String programDescription) {
        this.programDescription = programDescription;
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
    
    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }
    
    /**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("programCode", this.programCode);
        return m;
    }

}