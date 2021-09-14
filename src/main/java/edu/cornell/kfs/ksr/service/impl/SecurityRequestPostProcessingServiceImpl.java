package edu.cornell.kfs.ksr.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.service.SecurityRequestPostProcessingService;
import edu.cornell.kfs.ksr.util.KSRUtil;

public class SecurityRequestPostProcessingServiceImpl implements SecurityRequestPostProcessingService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SecurityRequestPostProcessingServiceImpl.class);

    private RoleService roleService;
    private RoleService roleUpdateService;

    /**
     * For each <code>SecurityRequestRole</code> on the security request, processes the requested update to KIM
     * 
     * <p>
     * If role is marked inactive which is currently active for the principal, the principal is removed from the given
     * role (and all qualifications). If a non-qualified role is marked active that is currently inactive, the principal
     * is granted the role. If a qualified role is marked active then updates are made to the principals qualifications
     * </p>
     * 
     * @see org.kuali.rice.ksr.service.SecurityRequestPostProcessingService#postProcessSecurityRequest(org.kuali.rice.ksr.document.SecurityRequestDocument)
     */
    public void postProcessSecurityRequest(SecurityRequestDocument document) {
        String principalId = document.getPrincipalId();

        LOG.info("Processing security request document: " + document.getDocumentNumber() + " for principal id: " + principalId);

        for (SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
            if (requestRole.isActive()) {
                if (requestRole.isQualifiedRole()) {
                    assignUpdatePrincipalRoleQualifications(requestRole, principalId);
                }
                else if (!requestRole.isCurrentActive()) {
                    assignPrincipalToRole(requestRole, principalId);
                }
            }
            else if (requestRole.isCurrentActive()) {
                removePrincipalFromRole(requestRole, principalId);
            }
        }
    }

    /**
     * Removes the given principal from the role given by the security request role record
     * 
     * @param requestRole
     *            - request role record containing the role to remove principal from
     * @param principalId
     *            - id for the principal to remove
     */
    protected void removePrincipalFromRole(SecurityRequestRole requestRole, String principalId) {
        LOG.debug("Removing principal: " + principalId + " from role: " + requestRole.getRoleId());
        
        if (requestRole.isQualifiedRole()) {
            List<Map<String,String>> principalQualifications = getPrincipalsCurrentQualifications(requestRole, principalId);

            for (Map<String,String> existingQualification : principalQualifications) {
                getRoleUpdateService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), existingQualification);
            }
        }
        else {
        	// ==== CU Customization: We now have to pass in an empty qualifications map instead of null. ====
            getRoleUpdateService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                    requestRole.getRoleInfo().getName(), Collections.<String,String>emptyMap());
        }
    }

    /**
     * Assigns the given principal to the given non-qualified role given by the security request record
     * 
     * @param requestRole
     *            - request role record containing the role to be assigned
     * @param principalId
     *            - id for the principal to assign
     */
    protected void assignPrincipalToRole(SecurityRequestRole requestRole, String principalId) {
        LOG.debug("Assigning principal: " + principalId + " to role: " + requestRole.getRoleId());
        
        getRoleUpdateService().assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                requestRole.getRoleInfo().getName(), new HashMap<String,String>());
    }

    /**
     * Add/Removes/Updates the given principal's qualifications to the role given by the security request record
     * 
     * @param requestRole
     *            - request role record containing the role to update principals qualifications for
     * @param principalId
     *            - id for the principal whose qualifications should be updated
     */
    protected void assignUpdatePrincipalRoleQualifications(SecurityRequestRole requestRole, String principalId) {
        List<Map<String,String>> principalQualifications = getPrincipalsCurrentQualifications(requestRole, principalId);
        // ==== CU Customization: Insert qualifications into a mutable list, since the role service now returns immutable lists. ====
        principalQualifications = new ArrayList<Map<String,String>>(principalQualifications);

        // iterate through requested qualifications and add if new
        for (SecurityRequestRoleQualification roleQualification : requestRole.getRequestRoleQualifications()) {
            Map<String,String> requestedQualification = roleQualification.buildQualificationAttributeSet();

            // check whether principal already has this qualification
            boolean qualificationExists = findInQualificationsListAndRemove(principalQualifications, requestedQualification);
            if (!qualificationExists) {
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("Assigning role: " + requestRole.getRoleId() + " to principal Id: " + principalId
            				+ " with qualifications: " + requestedQualification.toString());
            	}
                
                getRoleUpdateService().assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), requestedQualification);
            }
        }

        // remove any existing qualifications that were not requested (still remaining in list)
        for (Map<String,String> existingQualification : principalQualifications) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Removing assignment for role: " + requestRole.getRoleId() + " to principal Id: " + principalId
        				+ " with qualifications: " + existingQualification.toString());
        	}
            
            getRoleUpdateService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                    requestRole.getRoleInfo().getName(), existingQualification);
        }
    }

    /**
     * Retrieves all qualification sets for the given principal for the role given by the request role instance
     * 
     * <p>
     * Does not return qualifications obtained by nested role/group memberships
     * </p>
     * 
     * @param requestRole
     *            - request role instance to pull role from
     * @param principalId
     *            - id for principal to pull qualifications for
     * @return List<AttributeSet> principals qualifications
     */
    protected List<Map<String,String>> getPrincipalsCurrentQualifications(SecurityRequestRole requestRole, String principalId) {
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(requestRole.getRoleId());

        // We now have to pass in an empty qualifications map instead of null. ====
        return getRoleService().getNestedRoleQualifiersForPrincipalByRoleIds(principalId, roleIds, Collections.<String,String>emptyMap());    }

    /**
     * Iterates through the List of principal qualifications and looks for matches against the given requested
     * qualification. If a match if found, the attribute set is removed from the given list
     * 
     * @param principalQualifications
     *            - list of qualification sets to search
     * @param requestedQualification
     *            - qualification set to match
     * @return boolean true if qualification was found, false if it was not found
     */
    protected boolean findInQualificationsListAndRemove(List<Map<String,String>> principalQualifications,
            Map<String,String> requestedQualification) {
        boolean found = false;

        int matchedIndex = -1;
        for (int i = 0; i < principalQualifications.size(); i++) {
            Map<String,String> existingQualification = principalQualifications.get(i);

            if (KSRUtil.doQualificationsMatch(existingQualification, requestedQualification)) {
                matchedIndex = i;
                found = true;
            }
        }

        if (found) {
            principalQualifications.remove(matchedIndex);
        }

        return found;
    }

    public RoleService getRoleService() {
        if (roleService == null) {
            roleService = KimApiServiceLocator.getRoleService();
        }

        return roleService;
    }

    public RoleService getRoleUpdateService() {
        if (roleUpdateService == null) {
            roleUpdateService = KimApiServiceLocator.getRoleService();
        }

        return roleUpdateService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setRoleUpdateService(RoleService roleUpdateService) {
        this.roleUpdateService = roleUpdateService;
    }

}
