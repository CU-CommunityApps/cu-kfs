package org.kuali.kfs.ksr.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.service.SecurityRequestPostProcessingService;
import org.kuali.kfs.ksr.util.KsrUtil;
import org.kuali.rice.kim.api.role.RoleService;

/**
 * ====
 * CU Customization:
 * Copied over the version of this class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also remediated this class as needed for Rice 2.x compatibility, and to simplify the RoleService usage.
 * ====
 * 
 * Implementation of <code>SecurityRequestPostProcessingService</code>
 * 
 * @author rSmart Development Team
 */
public class SecurityRequestPostProcessingServiceImpl implements SecurityRequestPostProcessingService {
    private static Logger LOG = LogManager.getLogger(SecurityRequestPostProcessingServiceImpl.class);

    private RoleService roleService;

    /**
     * For each <code>SecurityRequestRole</code> on the security request, processes the requested update to KIM
     * 
     * <p>
     * If role is marked inactive which is currently active for the principal, the principal is removed from the given
     * role (and all qualifications). If a non-qualified role is marked active that is currently inactive, the principal
     * is granted the role. If a qualified role is marked active then updates are made to the principals qualifications
     * </p>
     * 
     * @see org.kuali.kfs.ksr.service.SecurityRequestPostProcessingService#postProcessSecurityRequest(org.kuali.kfs.ksr.document.SecurityRequestDocument)
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
                roleService.removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), existingQualification);
            }
        }
        else {
            roleService.removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                    requestRole.getRoleInfo().getName(), Collections.emptyMap());
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
        
        roleService.assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
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
            Map<String,String> requestedQualification = roleQualification.buildQualificationMap();

            // check whether principal already has this qualification
            boolean qualificationExists = findInQualificationsListAndRemove(principalQualifications, requestedQualification);
            if (!qualificationExists) {
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("Assigning role: " + requestRole.getRoleId() + " to principal Id: " + principalId
            				+ " with qualifications: " + requestedQualification.toString());
            	}
                
                roleService.assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), requestedQualification);
            }
        }

        // remove any existing qualifications that were not requested (still remaining in list)
        for (Map<String,String> existingQualification : principalQualifications) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Removing assignment for role: " + requestRole.getRoleId() + " to principal Id: " + principalId
        				+ " with qualifications: " + existingQualification.toString());
        	}
            
            roleService.removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
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

        return roleService.getRoleQualifersForPrincipalByRoleIds(principalId, roleIds, Collections.emptyMap());
    }

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

            if (KsrUtil.doQualificationsMatch(existingQualification, requestedQualification)) {
                matchedIndex = i;
                found = true;
            }
        }

        if (found) {
            principalQualifications.remove(matchedIndex);
        }

        return found;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

}
