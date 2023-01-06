package edu.cornell.kfs.ksr.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.KimConstants.AttributeConstants;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kns.kim.role.DerivedRoleTypeServiceBase;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.KRADPropertyConstants;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.util.KSRUtil;

public class SecurityRequestDerivedRoleTypeServiceImpl extends DerivedRoleTypeServiceBase {

    private static final Logger LOG = LogManager.getLogger(SecurityRequestDerivedRoleTypeServiceImpl.class);

    protected String getAuthorizerRoleId(final String roleName, final SecurityRequestDocument document, final SecurityRequestRole requestRole) {
        final SecurityProvisioningGroup provisioningGroup = getProvisioningGroupForRequest(document.getSecurityGroupId(), requestRole);
        if (KSRConstants.SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME.equals(roleName)) {
            return provisioningGroup.getDistributedAuthorizerRoleId();
        } else if (KSRConstants.SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME.equals(roleName)) {
            return provisioningGroup.getAdditionalAuthorizerRoleId();
        } else if (KSRConstants.SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME.equals(roleName)) {
            return provisioningGroup.getCentralAuthorizerRoleId();
        }

        return null;
    }

    protected List<RoleMembership> getRoleMembersFromDistributedAuthorizerRole(final String authorizerRoleId, final SecurityRequestDocument document,
            final SecurityRequestRole requestRole) {
        final List<RoleMembership> members = new ArrayList<RoleMembership>();

        Map<String, List<Map<String, String>>> addedMembers = new HashMap<String, List<Map<String, String>>>();

        for (final SecurityRequestRoleQualification roleQualification : requestRole.getRequestRoleQualifications()) {
            Map<String, String> roleQualifier = roleQualification.buildQualificationAttributeSet();

            boolean hasNonBlankQual = false;

            for (Iterator<String> qualValueIter = roleQualifier.values().iterator(); !hasNonBlankQual && qualValueIter.hasNext();) {
                if (StringUtils.isNotBlank(qualValueIter.next())) {
                    hasNonBlankQual = true;
                }
            }

            if (hasNonBlankQual) {
                boolean addMember = addMemberIfNotPreviouselyAdded(addedMembers, authorizerRoleId, roleQualifier);

                if (addMember) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("getRoleMembersFromDistributedAuthorizerRole() Adding member role id: " + authorizerRoleId + " with qualification: " + roleQualifier.toString());
                    }

                    List<RoleMembership> authorizerRoleMembers = getAuthorizerRoleMembers(authorizerRoleId, roleQualifier);
                    members.addAll(authorizerRoleMembers);
                }
            }
        }

        String primaryDepartmentCode = document.getPrimaryDepartmentCode();
        if (StringUtils.isNotBlank(primaryDepartmentCode) && StringUtils.contains(primaryDepartmentCode, "-")) {
            String[] primaryDepartment = StringUtils.split(primaryDepartmentCode, "-");

            Map<String, String> roleQualifier = new HashMap<String, String>();
            roleQualifier.put("chartOfAccountsCode", primaryDepartment[0]);
            roleQualifier.put("organizationCode", primaryDepartment[1]);

            boolean addMember = addMemberIfNotPreviouselyAdded(addedMembers, authorizerRoleId, roleQualifier);

            if (addMember) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("getRoleMembersFromDistributedAuthorizerRole() Adding member role id: " + authorizerRoleId + " with qualification: " + roleQualifier.toString());
                }

                List<RoleMembership> authorizerRoleMembers = getAuthorizerRoleMembers(authorizerRoleId, roleQualifier);
                members.addAll(authorizerRoleMembers);
            }
        }
        return members;
    }

    public List<RoleMembership> getRoleMembersFromDerivedRole(String roleName, SecurityRequestDocument document, SecurityRequestRole requestRole) {

        final List<RoleMembership> members = new ArrayList<RoleMembership>();

        boolean changeRequested = false;
        if (requestRole.isActive() != requestRole.isCurrentActive()) {
            changeRequested = true;
        } else if (requestRole.isQualifiedRole()) {
            changeRequested = KSRUtil.isQualificationChangeRequested(requestRole);
        }

        if (!changeRequested) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("getRoleMembersFromDerivedRole() No change requested for security request role: " + requestRole.getRoleId());
            }
            return members;
        }

        final String authorizerRoleId = getAuthorizerRoleId(roleName, document, requestRole);

        if (StringUtils.isBlank(authorizerRoleId)) {
            LOG.info("getRoleMembersFromDerivedRole() No authorizer role id found for security request role: " + requestRole.getRoleId());
            return members;
        }

        LOG.info("getRoleMembersFromDerivedRole() Found authorizer role id: " + authorizerRoleId + " for security request role: " + requestRole.getRoleId());

        Map<String, List<Map<String, String>>> addedMembers = new HashMap<String, List<Map<String, String>>>();

        if (KSRConstants.SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME.equals(roleName)) {
            members.addAll(getRoleMembersFromDistributedAuthorizerRole(authorizerRoleId, document, requestRole));
        } else if (isQualifiedAuthorizerRole(authorizerRoleId)) {
            members.addAll(getRoleMembersFromDistributedAuthorizerRole(authorizerRoleId, document, requestRole));
        } else {
            boolean addMember = addMemberIfNotPreviouselyAdded(addedMembers, authorizerRoleId, null);

            if (addMember) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("getRoleMembersFromDerivedRole() Adding member role id: " + authorizerRoleId);
                }

                List<RoleMembership> authorizerRoleMembers = getAuthorizerRoleMembers(authorizerRoleId, new HashMap<String, String>());
                members.addAll(authorizerRoleMembers);
            }
        }

        return members;
    }

    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(String namespaceCode, String roleName, Map<String, String> qualification) {
        final List<RoleMembership> members = new ArrayList<RoleMembership>();

        LOG.info("getRoleMembersFromDerivedRole() Generating role membership for role: " + roleName + " with qualification " + qualification);

        String documentNumber = qualification.get(AttributeConstants.DOCUMENT_NUMBER);
        SecurityRequestDocument document = (SecurityRequestDocument) KRADServiceLocatorWeb.getDocumentService().getByDocumentHeaderId(documentNumber);

        for (final SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
            members.addAll(getRoleMembersFromDerivedRole(roleName, document, requestRole));
        }
        LOG.info("getRoleMembersFromDerivedRole() Returning " + members.size() + " members");

        return members;
    }

    protected boolean addMemberIfNotPreviouselyAdded(Map<String, List<Map<String, String>>> addedMembers, String roleId, Map<String, String> qualification) {
        boolean addMember = true;

        List<Map<String, String>> memberQualifications = new ArrayList<Map<String, String>>();

        if (addedMembers.containsKey(roleId)) {
            if (qualification == null) {
                addMember = false;
            } else {
                memberQualifications = addedMembers.get(roleId);
                for (Map<String, String> memberQualification : memberQualifications) {
                    if (KSRUtil.doQualificationsMatch(memberQualification, qualification)) {
                        addMember = false;
                        break;
                    }
                }
            }
        }

        if (addMember) {
            if (qualification != null) {
                memberQualifications.add(qualification);
            }
            addedMembers.put(roleId, memberQualifications);
        }

        return addMember;
    }

    protected SecurityProvisioningGroup getProvisioningGroupForRequest(Long securityGroupId, SecurityRequestRole requestRole) {
        SecurityProvisioningGroup provisioningGroup = null;
        Map<String, Object> provisioningSearch = new HashMap<String, Object>();
        provisioningSearch.put(KSRPropertyConstants.SECURITY_GROUP_ID, securityGroupId);
        provisioningSearch.put(KSRPropertyConstants.PROVISIONING_ROLE_ID, requestRole.getRoleId());
        provisioningSearch.put(KRADPropertyConstants.ACTIVE, Boolean.TRUE);

        List<SecurityProvisioningGroup> provisioningGroups = (List<SecurityProvisioningGroup>) KRADServiceLocator.getBusinessObjectService()
                .findMatching(SecurityProvisioningGroup.class, provisioningSearch);

        if ((provisioningGroups != null) && !provisioningGroups.isEmpty()) {
            provisioningGroup = provisioningGroups.get(0);
        }

        return provisioningGroup;
    }

    protected List<RoleMembership> getAuthorizerRoleMembers(String authorizerRoleId, Map<String, String> qualification) {
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(authorizerRoleId);

        return KimApiServiceLocator.getRoleService().getRoleMembers(roleIds, qualification);
    }

    protected boolean isQualifiedAuthorizerRole(String authorizerRoleId) {
        RoleLite role = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(authorizerRoleId);
        if (role != null) {
            KimType type = KimApiServiceLocator.getKimTypeInfoService().getKimType(role.getKimTypeId());

            if (type != null) {
                return (type.getAttributeDefinitions() != null) && !type.getAttributeDefinitions().isEmpty();
            }
        }

        return false;
    }
    
    @Override
    public boolean dynamicRoleMembership(String namespaceCode, String roleName) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("dynamicRoleMembership, returning true, namespaceCode: " + namespaceCode + " roleName: " + roleName);
    	}
    	//prevent KFS from attempting to cache its derived memberships.
    	return true;
    }

}