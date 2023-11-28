package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;

public class CuDisbursementPayee extends DisbursementPayee {
    
    private static final long serialVersionUID = 1L;
    private String principalName;
    
    public CuDisbursementPayee () {
        super();
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }
    
}
