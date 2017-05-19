package edu.cornell.kfs.sec.document;

import java.util.List;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sec.businessobject.SecurityModel;
import org.kuali.kfs.sec.document.SecurityModelMaintainableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

@SuppressWarnings("deprecation")
public class CuSecurityModelMaintainableImpl extends SecurityModelMaintainableImpl {
    private static final long serialVersionUID = -3165627933802985623L;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SecurityModelMaintainableImpl.class);

    /**
     * Overridden to contain a modified copy the superclass's code,
     * which will properly inactivate existing model roles without
     * manually reactivating them first, and will properly retrieve
     * existing inactive model roles so that they can be reactivated.
     * These changes are necessary to prevent the associated KIM role(s)
     * from being updated in ways that can cause optimistic-locking
     * or unique-constraint exceptions.
     * 
     * To implement this fix, the following methods have been overridden
     * to be no-ops, and the superclass ones will be called as needed.
     * This allows for calling the superclass's doRouteStatusChange() method
     * without having it perform the problematic KIM role updates.
     * 
     * createOrUpdateModelRole, assignOrUpdateModelMembershipToDefinitionRoles,
     * assignOrUpdateModelMembers, inactivateModelRole
     * 
     * @see org.kuali.kfs.sec.document.SecurityModelMaintainableImpl#doRouteStatusChange(org.kuali.kfs.krad.bo.DocumentHeader)
     */
    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);

        if (documentHeader.getWorkflowDocument().isProcessed()) {
            DocumentService documentService = SpringContext.getBean(DocumentService.class);
            try {
                MaintenanceDocument document = (MaintenanceDocument) documentService.getByDocumentHeaderId(documentHeader.getDocumentNumber());
                SecurityModel oldSecurityModel = (SecurityModel) document.getOldMaintainableObject().getBusinessObject();
                SecurityModel newSecurityModel = (SecurityModel) document.getNewMaintainableObject().getBusinessObject();

                boolean newMaintenanceAction = getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)
                        || getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION);
                boolean inactivatingExistingSecurityModel = isInactivatingExistingSecurityModel(oldSecurityModel, newSecurityModel);

                if (!newMaintenanceAction && inactivatingExistingSecurityModel) {
                    inactivateOldModelRole(oldSecurityModel, newMaintenanceAction);
                }

                if (newSecurityModel.isActive()) {
                    initializeModelRole(oldSecurityModel, newSecurityModel, newMaintenanceAction);
                }
            } catch (WorkflowException e) {
                LOG.error("caught exception while handling handleRouteStatusChange -> documentService.getByDocumentHeaderId("
                        + documentHeader.getDocumentNumber() + "). ", e);
                throw new RuntimeException("caught exception while handling handleRouteStatusChange -> documentService.getByDocumentHeaderId("
                        + documentHeader.getDocumentNumber() + "). ", e);
            }
        }
    }

    protected boolean isInactivatingExistingSecurityModel(SecurityModel oldSecurityModel, SecurityModel newSecurityModel) {
        return oldSecurityModel != null && oldSecurityModel.isActive() && !newSecurityModel.isActive();
    }

    protected void inactivateOldModelRole(SecurityModel oldSecurityModel, boolean newMaintenanceAction) {
        Role oldModelRole = getExistingActiveModelRole(oldSecurityModel);
        boolean oldSecurityModelIsActive = oldSecurityModel.isActive();
        
        try {
            oldSecurityModel.setActive(false);
            super.assignOrUpdateModelMembershipToDefinitionRoles(oldModelRole, oldSecurityModel, oldSecurityModel, newMaintenanceAction);
            super.assignOrUpdateModelMembers(oldModelRole, oldSecurityModel);
            super.inactivateModelRole(oldModelRole);
        } finally {
            oldSecurityModel.setActive(oldSecurityModelIsActive);
        }
    }

    protected Role getExistingActiveModelRole(SecurityModel securityModel) {
        return getRoleService().getRoleByNamespaceCodeAndName(KFSConstants.CoreModuleNamespaces.ACCESS_SECURITY, securityModel.getName());
    }

    protected void initializeModelRole(SecurityModel oldSecurityModel, SecurityModel newSecurityModel, boolean newMaintenanceAction) {
        Role modelRole = createOrUpdatePotentiallyInactiveModelRole(newSecurityModel);
        super.assignOrUpdateModelMembershipToDefinitionRoles(modelRole, oldSecurityModel, newSecurityModel, newMaintenanceAction);
        super.assignOrUpdateModelMembers(modelRole, newSecurityModel);
    }

    /**
     * This method copies most of the code from the superclass's createOrUpdateModelRole() method,
     * except that this version is able to properly retrieve existing inactive roles.
     * It also forcibly updates the model role's name, in case the security model was renamed.
     */
    protected Role createOrUpdatePotentiallyInactiveModelRole(SecurityModel newSecurityModel) {
        Role modelRole = getPotentiallyInactiveModelRole(newSecurityModel);
        
        if (modelRole != null) {
            Role.Builder updatedRole = Role.Builder.create(modelRole);
            updatedRole.setActive(true);
            updatedRole.setName(newSecurityModel.getName());
            updatedRole.setDescription(newSecurityModel.getDescription());
            modelRole = getRoleService().updateRole(updatedRole.build());
        } else {
            String roleId = buildModelRoleId(newSecurityModel);
            Role.Builder newRole = Role.Builder.create();
            newRole.setId(roleId);
            newRole.setName(newSecurityModel.getName());
            newRole.setNamespaceCode(KFSConstants.CoreModuleNamespaces.ACCESS_SECURITY);
            newRole.setDescription(newSecurityModel.getDescription());
            newRole.setKimTypeId(getDefaultRoleTypeId());
            newRole.setActive(true);
            modelRole = getRoleService().createRole(newRole.build());
        }
        newSecurityModel.setRoleId(modelRole.getId());
        return modelRole;
    }

    protected Role getPotentiallyInactiveModelRole(SecurityModel securityModel) {
        String roleId = buildModelRoleId(securityModel);
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KimConstants.PrimaryKeyConstants.ROLE_ID, roleId),
                PredicateFactory.equal(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, KFSConstants.CoreModuleNamespaces.ACCESS_SECURITY));
        
        List<Role> results = getRoleService().findRoles(criteria).getResults();
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    protected String buildModelRoleId(SecurityModel securityModel) {
        return KFSConstants.CoreModuleNamespaces.ACCESS_SECURITY + "-" + securityModel.getId();
    }

    /**
     * Overridden to be a no-op method that only returns null,
     * to prevent the superclass from performing processing
     * when its doRouteStatusChange() method runs.
     * 
     * @see org.kuali.kfs.sec.document.SecurityModelMaintainableImpl#createOrUpdateModelRole(org.kuali.kfs.sec.businessobject.SecurityModel)
     */
    @Override
    protected Role createOrUpdateModelRole(SecurityModel newSecurityModel) {
        return null;
    }

    /**
     * Overridden to be a no-op method,
     * to prevent the superclass from performing processing
     * when its doRouteStatusChange() method runs.
     * 
     * @see org.kuali.kfs.sec.document.SecurityModelMaintainableImpl#assignOrUpdateModelMembershipToDefinitionRoles(
     * org.kuali.rice.kim.api.role.Role, org.kuali.kfs.sec.businessobject.SecurityModel, org.kuali.kfs.sec.businessobject.SecurityModel, boolean)
     */
    @Override
    protected void assignOrUpdateModelMembershipToDefinitionRoles(
            Role modelRole, SecurityModel oldSecurityModel, SecurityModel newSecurityModel, boolean newMaintenanceAction) {

    }

    /**
     * Overridden to be a no-op method,
     * to prevent the superclass from performing processing
     * when its doRouteStatusChange() method runs.
     * 
     * @see org.kuali.kfs.sec.document.SecurityModelMaintainableImpl#assignOrUpdateModelMembers(
     * org.kuali.rice.kim.api.role.Role, org.kuali.kfs.sec.businessobject.SecurityModel)
     */
    @Override
    protected void assignOrUpdateModelMembers(Role modelRole, SecurityModel securityModel) {

    }

    /**
     * Overridden to be a no-op method,
     * to prevent the superclass from performing processing
     * when its doRouteStatusChange() method runs.
     * 
     * @see org.kuali.kfs.sec.document.SecurityModelMaintainableImpl#inactivateModelRole(org.kuali.rice.kim.api.role.Role)
     */
    @Override
    protected void inactivateModelRole(Role modelRole) {

    }

    protected RoleService getRoleService() {
        return KimApiServiceLocator.getRoleService();
    }

}
