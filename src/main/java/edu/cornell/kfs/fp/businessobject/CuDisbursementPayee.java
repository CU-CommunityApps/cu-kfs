package edu.cornell.kfs.fp.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.sys.KFSPropertyConstants;



public class CuDisbursementPayee extends DisbursementPayee {

    public CuDisbursementPayee () {
        super();
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String principalName;
    
  //KFSPTS-1737 -- added
    /**
     * Gets the principalName attribute. 
     * @return Returns the principalName.
     */
    public String getPrincipalName() {
        return principalName;
    }

    //KFSPTS-1737 -- added
    /**
     * Sets the principalName attribute value.
     * @param principalName The principalId to set.
     */
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
    
   

}
