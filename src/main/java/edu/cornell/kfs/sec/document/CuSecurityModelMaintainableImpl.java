package edu.cornell.kfs.sec.document;

import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.sec.businessobject.SecurityModel;
import org.kuali.kfs.sec.document.SecurityModelMaintainableImpl;
import org.kuali.kfs.sys.context.SpringContext;

public class CuSecurityModelMaintainableImpl extends SecurityModelMaintainableImpl {
    private static final String KRIM_ROLE_ID_SEQ = "KRIM_ROLE_ID_S"; 

    protected String buildModelRoleId(SecurityModel securityModel) {
        return String.valueOf(SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(KRIM_ROLE_ID_SEQ));
    }
    
}
