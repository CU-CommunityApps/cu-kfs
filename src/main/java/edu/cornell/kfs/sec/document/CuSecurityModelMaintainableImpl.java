package edu.cornell.kfs.sec.document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sec.businessobject.SecurityModel;
import org.kuali.kfs.sec.document.SecurityModelMaintainableImpl;

public class CuSecurityModelMaintainableImpl extends SecurityModelMaintainableImpl {
    private static final Logger LOG = LogManager.getLogger(CuSecurityModelMaintainableImpl.class);
    
    @Override
    protected String buildModelRoleId(SecurityModel securityModel) {
        String roleId = String.valueOf(securityModel.getId());
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildModelRoleId, returning roleId: " + roleId);
        }
        return roleId;
    }

}
