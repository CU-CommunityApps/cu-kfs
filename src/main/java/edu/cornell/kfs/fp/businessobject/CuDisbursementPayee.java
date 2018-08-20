package edu.cornell.kfs.fp.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.sys.KFSPropertyConstants;



public class CuDisbursementPayee extends DisbursementPayee {
    
    private static final long serialVersionUID = 1L;
    private String principalName;
    
    public CuDisbursementPayee () {
        super();
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
    
}
