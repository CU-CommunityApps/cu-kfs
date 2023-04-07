/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.impl.role;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.role.RoleContract;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.role.RoleTypeService;
import org.kuali.kfs.kim.impl.common.attribute.KimAttribute;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalService;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CU Customization:
 * Updated this class to include improved KIM attribute handling.
 */
abstract class RoleServiceBase {

    private static final Logger LOG = LogManager.getLogger();
    private BusinessObjectService businessObjectService;
    private GroupService groupService;
    private ResponsibilityInternalService responsibilityInternalService;
    private RoleDao roleDao;
    protected CriteriaLookupService criteriaLookupService;
    // CU Customization: Added kimTypeInfoService reference.
    protected KimTypeInfoService kimTypeInfoService;

    /**
     * Converts the Qualifier Name/Value Role qualification set into Qualifier AttributeID/Value set
     *
     * @param qualification The original role qualification attribute set
     * @param roleId        The role id used to find the kim attributes applicable to the specific role
     * @return Converted Map<String, String> containing ID/value pairs
     */
    protected Map<String, String> convertQualifierKeys(Map<String, String> qualification, String roleId) {
        Map<String, String> convertedQualification = new HashMap<>();

        if (StringUtils.isNotBlank(roleId)) {
            Role role = getBusinessObjectService().findBySinglePrimaryKey(Role.class, roleId);

            // resolve the KimAttributes: attributeName to Id from the role
            if (ObjectUtils.isNotNull(role) && ObjectUtils.isNotNull(role.getKimType())) {
                Map<String, String> attributeKeyMap = role.getKimType().getAttributeDefinitions().stream().collect(
                        Collectors.toMap(kimTypeAttribute -> kimTypeAttribute.getKimAttribute().getAttributeName(),
                                KimTypeAttribute::getKimAttributeId));

                if (qualification != null && CollectionUtils.isNotEmpty(qualification.entrySet())) {
                    qualification.forEach((key, value) -> {
                        String kimAttributeId = attributeKeyMap.get(key);
                        if (StringUtils.isNotEmpty(kimAttributeId)) {
                            convertedQualification.put(kimAttributeId, value);
                        }
                    });
                }
            }
        }
        return convertedQualification;
    }

    protected void getNestedRoleTypeMemberIds(String roleId, Set<String> members) {
        List<RoleMember> firstLevelMembers = getStoredRoleMembersForRoleId(roleId, MemberType.ROLE.getCode(),
                Collections.emptyMap());
        for (RoleMember member : firstLevelMembers) {
            if (MemberType.ROLE.equals(member.getType())) {
                if (!members.contains(member.getMemberId())) {
                    members.add(member.getMemberId());
                    getNestedRoleTypeMemberIds(member.getMemberId(), members);
                }
            }
        }
    }

    protected List<RoleMember> getRoleMembersForPrincipalId(String roleId, String principalId) {
        return roleDao.getRolePrincipalsForPrincipalIdAndRoleIds(Collections.singletonList(roleId), principalId, null);
    }

    protected List<RoleMember> getRoleMembersForGroupIds(String roleId, List<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        return roleDao.getRoleMembersForGroupIds(roleId, groupIds);
    }

    /**
     * Retrieves a list of RoleMember instances from the KimRoleDao.
     *
     * @param daoActionToTake An indicator for which KimRoleDao method should be used to get the results if the
     *                        desired RoleMembers are not cached.
     * @param roleId          The role ID to filter by; may get used as the ID for members that are also roles,
     *                        depending on the daoActionToTake value.
     * @param principalId     The principal ID to filter by; may get ignored depending on the daoActionToTake value.
     * @param groupIds        The group IDs to filter by; may get ignored depending on the daoActionToTake value.
     * @param memberTypeCode  The member type code to filter by; may get overridden depending on the daoActionToTake
     *                       value.
     * @param qualification   The original role qualification attribute set
     * @return A list of RoleMember instances based on the provided parameters.
     * @throws IllegalArgumentException if daoActionToTake refers to an enumeration constant that is not
     * role-member-related.
     */
    protected List<RoleMember> getRoleMemberList(RoleDaoAction daoActionToTake, String roleId,
                                                 String principalId, Collection<String> groupIds, String memberTypeCode, Map<String, String> qualification) {
        Map<String, String> convertedQualification = convertQualifierKeys(qualification, roleId);

        if (groupIds == null || groupIds.isEmpty()) {
            groupIds = Collections.emptyList();
        }

        final Collection<String> roleIds = StringUtils.isNotBlank(roleId) ? List.of(roleId) : Collections.emptyList();
        switch (daoActionToTake) {
            case ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS:
                // Search for principal role members only.
                return roleDao.getRolePrincipalsForPrincipalIdAndRoleIds(roleIds, principalId, convertedQualification);
            case ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS:
                // Search for group role members only.
                return roleDao.getRoleGroupsForGroupIdsAndRoleIds(roleIds, groupIds, convertedQualification);
            case ROLE_MEMBERS_FOR_ROLE_IDS:
                // Search for role members with the given member type code.
                return roleDao.getRoleMembersForRoleId(roleId, memberTypeCode, convertedQualification);
            case ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS:
                // Search for role members who are also roles.
                return roleDao.getRoleMembershipsForRoleIdsAsMembers(roleIds, convertedQualification);
            case ROLE_MEMBERS_FOR_ROLE_IDS_WITH_FILTERS:
                // Search for role members that might be roles, principals, or groups.
                return roleDao.getRoleMembersForRoleIdsWithFilters(roleIds, principalId, groupIds,
                        convertedQualification);
            default:
                // This should never happen, since the previous switch block should handle this case appropriately.
                throw new IllegalArgumentException("The 'daoActionToTake' parameter cannot refer to a" +
                        " non-role-member-related value!");
        }
    }

    /**
     * Calls the KimRoleDao's "getRolePrincipalsForPrincipalIdAndRoleIds" method and/or retrieves any corresponding
     * members from the cache.
     */
    protected List<RoleMember> getStoredRolePrincipalsForPrincipalIdAndRoleId(String roleId, String principalId,
            Map<String, String> qualification) {
        return getRoleMemberList(RoleDaoAction.ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS, roleId, principalId,
                Collections.emptyList(), null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleGroupsForGroupIdsAndRoleIds" method and/or retrieves any corresponding members
     * from the cache.
     */
    protected List<RoleMember> getStoredRoleGroupsForGroupIdsAndRoleId(String roleId, Collection<String> groupIds,
            Map<String, String> qualification) {
        return getRoleMemberList(RoleDaoAction.ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS, roleId, null, groupIds,
                null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembersForRoleId" method and/or retrieves any corresponding members from the
     * cache.
     */
    protected List<RoleMember> getStoredRoleMembersForRoleId(String roleId, String memberTypeCode,
            Map<String, String> qualification) {
        return getRoleMemberList(RoleDaoAction.ROLE_MEMBERS_FOR_ROLE_IDS, roleId, null, Collections.emptyList(),
                memberTypeCode, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembershipsForRoleIdsAsMembers" method and/or retrieves any corresponding
     * members from the cache.
     */
    protected List<RoleMember> getStoredRoleMembershipsForRoleIdAsMembers(String roleId,
            Map<String, String> qualification) {
        return getRoleMemberList(RoleDaoAction.ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS, roleId, null,
                Collections.emptyList(), null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembersForRoleIdsWithFilters" method and/or retrieves any corresponding members
     * from the cache.
     */
    protected List<RoleMember> getStoredRoleMembersForRoleIdWithFilters(String roleId, String principalId,
            List<String> groupIds, Map<String, String> qualification) {
        return getRoleMemberList(RoleDaoAction.ROLE_MEMBERS_FOR_ROLE_IDS_WITH_FILTERS, roleId, principalId,
                groupIds, null, qualification);
    }

    /**
     * @return a RoleMember object by its ID. If the role member already exists in the cache, this method will
     *         return the cached version; otherwise, it will retrieve the uncached version from the database and then
     *         cache it (if it belongs to a role that allows its members to be cached) before returning it.
     */
    protected RoleMember getRoleMember(String roleMemberId) {
        if (StringUtils.isBlank(roleMemberId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(RoleMember.class, Collections.singletonMap(
                KimConstants.PrimaryKeyConstants.ID, roleMemberId));
    }

    /**
     * Retrieves a RoleResponsibilityAction object by its ID.
     */
    protected RoleResponsibilityAction getRoleResponsibilityAction(String roleResponsibilityActionId) {
        if (StringUtils.isBlank(roleResponsibilityActionId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(RoleResponsibilityAction.class, Collections.singletonMap(
                KimConstants.PrimaryKeyConstants.ID, roleResponsibilityActionId));
    }

    /**
     * Calls the KimRoleDao's "getDelegationImplMapFromRoleIds" method and/or retrieves any corresponding delegations
     * from the cache.
     */
    protected Map<String, DelegateType> getStoredDelegationImplMapFromRoleIds(Collection<String> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            return roleDao.getDelegationImplMapFromRoleIds(roleIds);
        }

        return Collections.emptyMap();
    }

    /**
     * Calls the KimRoleDao's "getDelegationBosForRoleIds" method and/or retrieves any corresponding delegations from
     * the cache.
     * @param roleId roleId used to find delegations
     */
    protected List<DelegateType> getStoredDelegationImplsForRoleIds(String roleId) {
        return roleDao.getDelegationBosForRoleId(roleId);
    }

    protected List<DelegateMember> getStoredDelegationPrincipalsForPrincipalId(String principalId) {
        return roleDao.getDelegationPrincipalsForPrincipalId(principalId);
    }

    /**
     * @return a DelegateMember object by its ID. If the delegation member already exists in the cache, this method
     *         will return the cached version; otherwise, it will retrieve the uncached version from the database and
     *         then cache it before returning it.
     */
    protected DelegateMember getDelegateMember(String delegationMemberId) {
        if (StringUtils.isBlank(delegationMemberId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(DelegateMember.class,
                Collections.singletonMap(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMemberId));
    }

    protected Role getRole(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return null;
        }
        return getBusinessObjectService().findBySinglePrimaryKey(Role.class, roleId);
    }

    protected RoleLite getRoleLite(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return null;
        }
        return getBusinessObjectService().findBySinglePrimaryKey(RoleLite.class, roleId);
    }

    protected DelegateType getDelegationOfType(String roleId, DelegationType delegationType) {
        List<DelegateType> roleDelegates = getRoleDelegations(roleId);
        if (isDelegationPrimary(delegationType)) {
            return getPrimaryDelegation(roleId, roleDelegates);
        } else {
            return getSecondaryDelegation(roleId, roleDelegates);
        }
    }

    private DelegateType getSecondaryDelegation(String roleId, List<DelegateType> roleDelegates) {
        DelegateType secondaryDelegate = null;
        RoleLite roleLite = getRoleLite(roleId);
        for (DelegateType delegate : roleDelegates) {
            if (isDelegationSecondary(delegate.getDelegationType())) {
                secondaryDelegate = delegate;
            }
        }
        if (secondaryDelegate == null) {
            secondaryDelegate = new DelegateType();
            secondaryDelegate.setRoleId(roleId);
            secondaryDelegate.setDelegationType(DelegationType.SECONDARY);
            secondaryDelegate.setKimTypeId(roleLite.getKimTypeId());
        }
        return secondaryDelegate;
    }

    protected DelegateType getPrimaryDelegation(String roleId, List<DelegateType> roleDelegates) {
        DelegateType primaryDelegate = null;
        RoleLite roleLite = getRoleLite(roleId);
        for (DelegateType delegate : roleDelegates) {
            if (isDelegationPrimary(delegate.getDelegationType())) {
                primaryDelegate = delegate;
            }
        }
        if (primaryDelegate == null) {
            primaryDelegate = new DelegateType();
            primaryDelegate.setRoleId(roleId);
            primaryDelegate.setDelegationType(DelegationType.PRIMARY);
            primaryDelegate.setKimTypeId(roleLite.getKimTypeId());
        }
        return primaryDelegate;
    }

    protected boolean isDelegationPrimary(DelegationType delegationType) {
        return DelegationType.PRIMARY.equals(delegationType);
    }

    protected boolean isDelegationSecondary(DelegationType delegationType) {
        return DelegationType.SECONDARY.equals(delegationType);
    }

    private List<DelegateType> getRoleDelegations(String roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return getStoredDelegationImplsForRoleIds(roleId);

    }

    protected Role getRoleByName(String namespaceCode, String roleName) {
        if (StringUtils.isBlank(namespaceCode) || StringUtils.isBlank(roleName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, roleName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return getBusinessObjectService().findByPrimaryKey(Role.class, criteria);
    }

    protected RoleLite getRoleLiteByName(String namespaceCode, String roleName) {
        if (StringUtils.isBlank(namespaceCode) || StringUtils.isBlank(roleName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, roleName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return getBusinessObjectService().findByPrimaryKey(RoleLite.class, criteria);
    }

    protected List<RoleMember> doAnyMemberRecordsMatchByExactQualifier(RoleContract role, String memberId,
                                                                       RoleDaoAction daoActionToTake, Map<String, String> qualifier) {
        return getRoleMembersByExactQualifierMatch(role, memberId, daoActionToTake, qualifier);
    }

    protected List<RoleMember> getRoleMembersByExactQualifierMatch(RoleContract role, String memberId,
                                                                   RoleDaoAction daoActionToTake, Map<String, String> qualifier) {
        List<RoleMember> rms = new ArrayList<>();
        RoleTypeService roleTypeService = getRoleTypeService(role.getId());
        if (roleTypeService != null) {
            List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
            if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                switch (daoActionToTake) {
                    case ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS:
                        // Search for group role members only.
                        rms = getStoredRoleGroupsForGroupIdsAndRoleId(role.getId(),
                                Collections.singletonList(memberId), populateQualifiersForExactMatch(qualifier,
                                        attributesForExactMatch));
                        break;
                    case ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS:
                        // Search for principal role members only.
                        rms = getStoredRolePrincipalsForPrincipalIdAndRoleId(role.getId(), memberId,
                                populateQualifiersForExactMatch(qualifier, attributesForExactMatch));
                        break;
                    case ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS:
                        // Search for roles as role members only.
                        List<RoleMember> allRoleMembers = getStoredRoleMembershipsForRoleIdAsMembers(role.getId(),
                                populateQualifiersForExactMatch(qualifier, attributesForExactMatch));
                        for (RoleMember rm : allRoleMembers) {
                            if (rm.getMemberId().equals(memberId)) {
                                rms.add(rm);
                            }
                        }
                        break;
                    default:
                        // The daoActionToTake parameter is invalid; throw an exception.
                        throw new IllegalArgumentException("The 'daoActionToTake' parameter cannot refer to a " +
                                "non-role-member-related value!");
                }
            }
        }
        return rms;
    }

    //return roleMemberId of match or null if no match
    protected RoleMember doAnyMemberRecordsMatch(List<RoleMember> roleMembers, String memberId,
                                                 String memberTypeCode, Map<String, String> qualifier) {
        for (RoleMember rm : roleMembers) {
            if (rm.isActive() && doesMemberMatch(rm, memberId, memberTypeCode, qualifier)) {
                return rm;
            }
        }
        return null;
    }

    protected boolean doesMemberMatch(RoleMember roleMember, String memberId, String memberTypeCode,
                                      Map<String, String> qualifier) {
        if (roleMember.getMemberId().equals(memberId) && roleMember.getType().getCode().equals(memberTypeCode)) {
            // member ID/type match
            Map<String, String> roleQualifier = roleMember.getAttributes();
            if ((qualifier == null || qualifier.isEmpty())
                    && (roleQualifier == null || roleQualifier.isEmpty())) {
                return true;
            } else {
                return qualifier != null && qualifier.equals(roleQualifier);
            }
        }
        return false;
    }

    /**
     * Retrieves the role type service for the given service name.
     *
     * @param serviceName the name of the service to retrieve
     * @return the Role Type Service
     */
    protected RoleTypeService getRoleTypeServiceByName(String serviceName) {
        try {
            return (RoleTypeService) KimImplServiceLocator.getService(serviceName);
        } catch (Exception ex) {
            LOG.warn("Unable to find role type service with name: {}", serviceName, ex);
            return (RoleTypeService) KimImplServiceLocator.getService("kimNoMembersRoleTypeService");
        }
    }

    /**
     * Retrieves the role type service associated with the given role ID
     *
     * @param roleId the role ID to get the role type service for
     * @return the Role Type Service
     */
    protected RoleTypeService getRoleTypeService(String roleId) {
        RoleLite roleLite = getRoleLite(roleId);
        if (roleLite != null) {
            KimType roleType = roleLite.getKimRoleType();
            if (roleType != null) {
                return getRoleTypeService(roleType);
            }
        }
        return KimImplServiceLocator.getDefaultRoleTypeService();
    }

    protected RoleTypeService getRoleTypeService(KimType typeInfo) {
        String serviceName = typeInfo.getServiceName();
        if (serviceName != null) {
            try {
                return (RoleTypeService) KimImplServiceLocator.getService(serviceName);
            } catch (Exception ex) {
                LOG.error("Unable to find role type service with name: {}", serviceName, ex);
                return (RoleTypeService) KimImplServiceLocator.getService("kimNoMembersRoleTypeService");
            }
        }
        return KimImplServiceLocator.getDefaultRoleTypeService();
    }

    protected Map<String, String> populateQualifiersForExactMatch(Map<String, String> defaultQualification,
            List<String> attributes) {
        Map<String, String> qualifiersForExactMatch = new HashMap<>();
        if (defaultQualification != null && CollectionUtils.isNotEmpty(defaultQualification.keySet())) {
            for (String attributeName : attributes) {
                if (StringUtils.isNotEmpty(defaultQualification.get(attributeName))) {
                    qualifiersForExactMatch.put(attributeName, defaultQualification.get(attributeName));
                }
            }
        }
        return qualifiersForExactMatch;
    }

    /*
     * CU Customization:
     * 
     * Modified this method to include kimTypeId as an argument,
     * and to also retrieve the attribute ID from the KimType if possible.
     */
    protected String getKimAttributeId(String kimTypeId, String attributeName) {
        String attributeId = getAttributeIdFromKimType(kimTypeId, attributeName);
        if (StringUtils.isBlank(attributeId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getKimAttributeId, Could not find attribute '" + attributeName + "' for KIM type '"
                        + kimTypeId + "', will attempt to find the attribute by name only");
            }
            attributeId = getAttributeIdByName(attributeName);
        }
        return attributeId;
    }

    /*
     * CU Customization:
     */
    protected String getAttributeIdFromKimType(String kimTypeId, String attributeName) {
        KimType kimType = getKimTypeInfoService().getKimType(kimTypeId);
        if (ObjectUtils.isNull(kimType)) {
            return null;
        }
        
        KimTypeAttribute attribute = kimType.getAttributeDefinitionByName(attributeName);
        if (ObjectUtils.isNotNull(attribute) && ObjectUtils.isNotNull(attribute.getKimAttribute())) {
            return attribute.getKimAttribute().getId();
        } else {
            return null;
        }
    }

    /*
     * CU Customization:
     *
     * This is a modified version of the base getKimAttributeId() code.
     */
    protected String getAttributeIdByName(String attributeName) {
        Map<String, Object> critieria = new HashMap<>(1);
        critieria.put("attributeName", attributeName);
        Collection<KimAttribute> defs = getBusinessObjectService().findMatching(KimAttribute.class, critieria);
        String result = null;
        if (CollectionUtils.isNotEmpty(defs)) {
            result = defs.iterator().next().getId();
            if (CollectionUtils.size(defs) > 1) {
                LOG.warn("getAttributeIdByName, Found multiple attributes named '" + attributeName
                        + "', will return only the first match with ID: " + result);
            }
        }
        return result;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    protected GroupService getGroupService() {
        if (groupService == null) {
            groupService = KimApiServiceLocator.getGroupService();
        }

        return groupService;
    }

    protected ResponsibilityInternalService getResponsibilityInternalService() {
        if (responsibilityInternalService == null) {
            responsibilityInternalService = KimImplServiceLocator.getResponsibilityInternalService();
        }
        return responsibilityInternalService;
    }

    protected RoleDao getRoleDao() {
        return this.roleDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public CriteriaLookupService getCriteriaLookupService() {
        return criteriaLookupService;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    // CU Customization: Add KimTypeInfoService getter and setter.
    public KimTypeInfoService getKimTypeInfoService() {
        if (kimTypeInfoService == null) {
            LOG.error("getKimTypeInfoService: Detected service as null. Attempting to obtain service with call to KimApiServiceLocator.");
            setKimTypeInfoService(KimApiServiceLocator.getKimTypeInfoService());
            if (kimTypeInfoService == null) {
                LOG.error("getKimTypeInfoService: KimApiServiceLocator returned a null for the service. Investigate adding call to extended class to set this service value in the abstract super class.");
            }
        }
        return kimTypeInfoService;
    }

    public void setKimTypeInfoService(KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
    }

    /**
     * A helper enumeration for indicating which KimRoleDao method to use when attempting to get
     * role/delegation-related lists that are not in the cache.
     */
    protected enum RoleDaoAction {
        ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS,
        ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS,
        ROLE_MEMBERS_FOR_ROLE_IDS,
        ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS,
        ROLE_MEMBERS_FOR_ROLE_IDS_WITH_FILTERS
    }
}
