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

    protected void removePrincipalFromRole(SecurityRequestRole requestRole, String principalId) {
        LOG.debug("Removing principal: " + principalId + " from role: " + requestRole.getRoleId());
        
        if (requestRole.isQualifiedRole()) {
            List<Map<String,String>> principalQualifications = getPrincipalsCurrentQualifications(requestRole, principalId);

            for (Map<String,String> existingQualification : principalQualifications) {
                getRoleService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), existingQualification);
            }
        }
        else {
            getRoleService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                    requestRole.getRoleInfo().getName(), Collections.<String,String>emptyMap());
        }
    }

    protected void assignPrincipalToRole(SecurityRequestRole requestRole, String principalId) {
        LOG.debug("Assigning principal: " + principalId + " to role: " + requestRole.getRoleId());
        
        getRoleService().assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                requestRole.getRoleInfo().getName(), new HashMap<String,String>());
    }

    protected void assignUpdatePrincipalRoleQualifications(SecurityRequestRole requestRole, String principalId) {
        List<Map<String,String>> principalQualifications = getPrincipalsCurrentQualifications(requestRole, principalId);

        principalQualifications = new ArrayList<Map<String,String>>(principalQualifications);

        for (SecurityRequestRoleQualification roleQualification : requestRole.getRequestRoleQualifications()) {
            Map<String,String> requestedQualification = roleQualification.buildQualificationAttributeSet();

            boolean qualificationExists = findInQualificationsListAndRemove(principalQualifications, requestedQualification);
            if (!qualificationExists) {
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("Assigning role: " + requestRole.getRoleId() + " to principal Id: " + principalId
            				+ " with qualifications: " + requestedQualification.toString());
            	}
                
                getRoleService().assignPrincipalToRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                        requestRole.getRoleInfo().getName(), requestedQualification);
            }
        }

        for (Map<String,String> existingQualification : principalQualifications) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Removing assignment for role: " + requestRole.getRoleId() + " to principal Id: " + principalId
        				+ " with qualifications: " + existingQualification.toString());
        	}
            
            getRoleService().removePrincipalFromRole(principalId, requestRole.getRoleInfo().getNamespaceCode(),
                    requestRole.getRoleInfo().getName(), existingQualification);
        }
    }


    protected List<Map<String,String>> getPrincipalsCurrentQualifications(SecurityRequestRole requestRole, String principalId) {
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(requestRole.getRoleId());

        return getRoleService().getNestedRoleQualifiersForPrincipalByRoleIds(principalId, roleIds, Collections.<String,String>emptyMap());    }

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

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

}
