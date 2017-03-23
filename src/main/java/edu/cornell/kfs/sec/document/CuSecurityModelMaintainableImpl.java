package edu.cornell.kfs.sec.document;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sec.document.SecurityModelMaintainableImpl;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

public class CuSecurityModelMaintainableImpl extends SecurityModelMaintainableImpl {

    /**
     * This is a bit of a hack, but we are getting OptimisticLockExceptions when inactivating
     * Security Models because it does one update with the model active so it can then update
     * model membership and model members (an inactive role would generate an NPE because it
     * wouldn't be retrieved) and then a second update to inactivate the role.
     *
     * The OLEs started after our upgrade to KFS 7.x/Rice 2.5.x.
     *
     * This workaround manually increments the version number to avoid the exception.
     *
     * @param modelRole
     */
    @Override
    protected void inactivateModelRole(Role modelRole) {
        if(modelRole != null) {
            Role.Builder updatedRole = Role.Builder.create(modelRole);
            if (ObjectUtils.isNull(modelRole.getVersionNumber())) {
                updatedRole.setVersionNumber(1L);
            } else {
                updatedRole.setVersionNumber(modelRole.getVersionNumber() + 1);
            }
            updatedRole.setActive(false);
            KimApiServiceLocator.getRoleService().updateRole(updatedRole.build());
        }
    }

}
