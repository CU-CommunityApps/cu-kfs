/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.framework.role.RoleTypeService;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeBo;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMemberBo;
import org.kuali.kfs.kim.impl.common.delegate.DelegateTypeBo;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalService;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimTypeBo;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.LookupService;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupService;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleContract;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeAttribute;
import org.kuali.rice.kim.api.type.KimTypeInfoService;

/**
 * CU Customization:
 * Updated this class to include improved KIM attribute handling,
 * similar to that from the Rice version of this class.
 */
abstract class RoleServiceBase {

    private static final Logger LOG = LogManager.getLogger(RoleServiceBase.class);
    private BusinessObjectService businessObjectService;
    private LookupService lookupService;
    private IdentityService identityService;
    private GroupService groupService;
    private ResponsibilityInternalService responsibilityInternalService;
    private RoleDao roleDao;
    protected CriteriaLookupService criteriaLookupService;
    protected KimTypeInfoService kimTypeInfoService;

    /**
     * Converts the Qualifier Name/Value Role qualification set into Qualifier AttributeID/Value set
     *
     * @param qualification The original role qualification attribute set
     * @param validAttributeIds The mapping of attribute names to their matching attribute ids
     * @return Converted Map<String, String> containing ID/value pairs
     */
    protected Map<String, String> convertQualifierKeys(
            Map<String, String> qualification, Map<String, String> validAttributeIds) {
        Map<String, String> convertedQualification = new HashMap<>();
        if (qualification != null && CollectionUtils.isNotEmpty(qualification.entrySet())) {
            for (Map.Entry<String, String> entry : qualification.entrySet()) {
                String kimAttributeId = validAttributeIds.get(entry.getKey());
                if (StringUtils.isNotEmpty(kimAttributeId)) {
                    convertedQualification.put(kimAttributeId, entry.getValue());
                }
            }
        }
        return convertedQualification;
    }

    protected void getNestedRoleTypeMemberIds(String roleId, Set<String> members) {
        ArrayList<String> roleList = new ArrayList<>(1);
        roleList.add(roleId);
        List<RoleMemberBo> firstLevelMembers = getStoredRoleMembersForRoleIds(roleList, MemberType.ROLE.getCode(),
                Collections.emptyMap());
        for (RoleMemberBo member : firstLevelMembers) {
            if (MemberType.ROLE.equals(member.getType())) {
                if (!members.contains(member.getMemberId())) {
                    members.add(member.getMemberId());
                    getNestedRoleTypeMemberIds(member.getMemberId(), members);
                }
            }
        }
    }

    protected List<RoleMemberBo> getRoleMembersForPrincipalId(String roleId, String principalId) {
        return roleDao.getRolePrincipalsForPrincipalIdAndRoleIds(Collections.singletonList(roleId), principalId, null);
    }

    protected List<RoleMemberBo> getRoleMembersForGroupIds(String roleId, List<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        return roleDao.getRoleMembersForGroupIds(roleId, groupIds);
    }

    /**
     * Retrieves a list of RoleMemberBo instances from the KimRoleDao.
     *
     * @param daoActionToTake An indicator for which KimRoleDao method should be used to get the results if the
     *                        desired RoleMemberBos are not cached.
     * @param roleIds         The role IDs to filter by; may get used as the IDs for members that are also roles,
     *                        depending on the daoActionToTake value.
     * @param principalId     The principal ID to filter by; may get ignored depending on the daoActionToTake value.
     * @param groupIds        The group IDs to filter by; may get ignored depending on the daoActionToTake value.
     * @param memberTypeCode  The member type code to filter by; may get overridden depending on the daoActionToTake
     *                       value.
     * @param qualification   The original role qualification attribute set
     * @return A list of RoleMemberBo instances based on the provided parameters.
     * @throws IllegalArgumentException if daoActionToTake refers to an enumeration constant that is not
     * role-member-related.
     */
    protected List<RoleMemberBo> getRoleMemberBoList(RoleDaoAction daoActionToTake, Collection<String> roleIds,
            String principalId, Collection<String> groupIds, String memberTypeCode, Map<String, String> qualification) {
        Map<String, String> validAttributeIds = getAttributeNameToAttributeIdMappings(roleIds, qualification);
        Map<String, String> convertedQualification = convertQualifierKeys(qualification, validAttributeIds);

        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = Collections.emptyList();
        }
        if (groupIds == null || groupIds.isEmpty()) {
            groupIds = Collections.emptyList();
        }

        switch (daoActionToTake) {
            case ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS:
                // Search for principal role members only.
                return roleDao.getRolePrincipalsForPrincipalIdAndRoleIds(roleIds, principalId, convertedQualification);
            case ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS:
                // Search for group role members only.
                return roleDao.getRoleGroupsForGroupIdsAndRoleIds(roleIds, groupIds, convertedQualification);
            case ROLE_MEMBERS_FOR_ROLE_IDS:
                // Search for role members with the given member type code.
                return roleDao.getRoleMembersForRoleIds(roleIds, memberTypeCode, convertedQualification);
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

    /*
     * CU Customization: Create mappings from attribute names to attribute IDs,
     * similarly to the Map-creation logic from Rice's RoleServiceBase class.
     */
    protected Map<String, String> getAttributeNameToAttributeIdMappings(
            Collection<String> roleIds, Map<String, String> qualification) {
        if (CollectionUtils.isEmpty(roleIds) || qualification == null || qualification.isEmpty()) {
            return Collections.emptyMap();
        }

        KimTypeInfoService typeInfoService = getKimTypeInfoService();
        Set<String> attributeNames = qualification.keySet();
        Map<String, String> validAttributeIds = new HashMap<>();

        roleIds.stream()
                .map(this::getRoleBoLite)
                .filter(ObjectUtils::isNotNull)
                .map(RoleBoLite::getKimTypeId)
                .distinct()
                .map(typeInfoService::getKimType)
                .filter(ObjectUtils::isNotNull)
                .flatMap(kimType -> kimType.getAttributeDefinitions().stream())
                .map(KimTypeAttribute::getKimAttribute)
                .filter(attribute -> ObjectUtils.isNotNull(attribute)
                        && attributeNames.contains(attribute.getAttributeName()))
                .forEach(attribute ->
                        validAttributeIds.put(attribute.getAttributeName(), attribute.getId()));

        for (String attributeName : attributeNames) {
            validAttributeIds.computeIfAbsent(attributeName, this::getAttributeIdByName);
        }

        return validAttributeIds;
    }

    /**
     * Calls the KimRoleDao's "getRolePrincipalsForPrincipalIdAndRoleIds" method and/or retrieves any corresponding
     * members from the cache.
     */
    protected List<RoleMemberBo> getStoredRolePrincipalsForPrincipalIdAndRoleIds(Collection<String> roleIds,
            String principalId, Map<String, String> qualification) {
        return getRoleMemberBoList(RoleDaoAction.ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS, roleIds, principalId,
                Collections.emptyList(), null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleGroupsForGroupIdsAndRoleIds" method and/or retrieves any corresponding members
     * from the cache.
     */
    protected List<RoleMemberBo> getStoredRoleGroupsForGroupIdsAndRoleIds(Collection<String> roleIds,
            Collection<String> groupIds, Map<String, String> qualification) {
        return getRoleMemberBoList(RoleDaoAction.ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS, roleIds, null, groupIds,
                null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembersForRoleIds" method and/or retrieves any corresponding members from the
     * cache.
     */
    protected List<RoleMemberBo> getStoredRoleMembersForRoleIds(Collection<String> roleIds, String memberTypeCode,
            Map<String, String> qualification) {
        return getRoleMemberBoList(RoleDaoAction.ROLE_MEMBERS_FOR_ROLE_IDS, roleIds, null, Collections.emptyList(),
                memberTypeCode, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembershipsForRoleIdsAsMembers" method and/or retrieves any corresponding
     * members from the cache.
     */
    protected List<RoleMemberBo> getStoredRoleMembershipsForRoleIdsAsMembers(Collection<String> roleIds,
            Map<String, String> qualification) {
        return getRoleMemberBoList(RoleDaoAction.ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS, roleIds, null,
                Collections.emptyList(), null, qualification);
    }

    /**
     * Calls the KimRoleDao's "getRoleMembersForRoleIdsWithFilters" method and/or retrieves any corresponding members
     * from the cache.
     */
    protected List<RoleMemberBo> getStoredRoleMembersForRoleIdsWithFilters(Collection<String> roleIds,
            String principalId, List<String> groupIds, Map<String, String> qualification) {
        return getRoleMemberBoList(RoleDaoAction.ROLE_MEMBERS_FOR_ROLE_IDS_WITH_FILTERS, roleIds, principalId,
                groupIds, null, qualification);
    }

    /**
     * @return a RoleMemberBo object by its ID. If the role member already exists in the cache, this method will
     *         return the cached version; otherwise, it will retrieve the uncached version from the database and then
     *         cache it (if it belongs to a role that allows its members to be cached) before returning it.
     */
    protected RoleMemberBo getRoleMemberBo(String roleMemberId) {
        if (StringUtils.isBlank(roleMemberId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(RoleMemberBo.class, Collections.singletonMap(
                KimConstants.PrimaryKeyConstants.ID, roleMemberId));
    }

    /**
     * Retrieves a RoleResponsibilityActionBo object by its ID.
     */
    protected RoleResponsibilityActionBo getRoleResponsibilityActionBo(String roleResponsibilityActionId) {
        if (StringUtils.isBlank(roleResponsibilityActionId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(RoleResponsibilityActionBo.class, Collections.singletonMap(
                KimConstants.PrimaryKeyConstants.ID, roleResponsibilityActionId));
    }

    /**
     * Calls the KimRoleDao's "getDelegationImplMapFromRoleIds" method and/or retrieves any corresponding delegations
     * from the cache.
     */
    protected Map<String, DelegateTypeBo> getStoredDelegationImplMapFromRoleIds(Collection<String> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            return roleDao.getDelegationImplMapFromRoleIds(roleIds);
        }

        return Collections.emptyMap();
    }

    /**
     * Calls the KimRoleDao's "getDelegationBosForRoleIds" method and/or retrieves any corresponding delegations from
     * the cache.
     */
    protected List<DelegateTypeBo> getStoredDelegationImplsForRoleIds(Collection<String> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            return roleDao.getDelegationBosForRoleIds(roleIds);
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves a List of delegation members from the KimRoleDao as appropriate.
     *
     * @param daoActionToTake An indicator for which KimRoleDao method to use for retrieving results.
     * @param delegationIds   The IDs of the delegations that the members belong to.
     * @param principalId     The principal ID of the principal delegation members; may get ignored depending on the
     *                        RoleDaoAction value.
     * @param groupIds        The group IDs of the group delegation members; may get ignored depending on the
     *                        RoleDaoAction value.
     * @return A List of DelegateMemberBo objects based on the provided parameters.
     * @throws IllegalArgumentException if daoActionToTake does not represent a delegation-member-list-related
     * enumeration value.
     */
    protected List<DelegateMemberBo> getDelegationMemberBoList(RoleDaoAction daoActionToTake,
            Collection<String> delegationIds, String principalId, List<String> groupIds) {
        if (delegationIds == null || delegationIds.isEmpty()) {
            delegationIds = Collections.emptyList();
        }
        if (groupIds == null || groupIds.isEmpty()) {
            groupIds = Collections.emptyList();
        }

        switch (daoActionToTake) {
            case DELEGATION_PRINCIPALS_FOR_PRINCIPAL_ID_AND_DELEGATION_IDS:
                // Search for principal delegation members.
                return roleDao.getDelegationPrincipalsForPrincipalIdAndDelegationIds(delegationIds, principalId);
            case DELEGATION_GROUPS_FOR_GROUP_IDS_AND_DELEGATION_IDS:
                // Search for group delegation members.
                return roleDao.getDelegationGroupsForGroupIdsAndDelegationIds(delegationIds, groupIds);
            default:
                // This should never happen since the previous switch block should handle this case appropriately.
                throw new IllegalArgumentException("The 'daoActionToTake' parameter cannot refer to a " +
                        "non-delegation-member-list-related value!");
        }
    }

    /**
     * Calls the KimRoleDao's "getDelegationPrincipalsForPrincipalIdAndDelegationIds" method and/or retrieves any
     * corresponding members from the cache.
     */
    protected List<DelegateMemberBo> getStoredDelegationPrincipalsForPrincipalIdAndDelegationIds(
            Collection<String> delegationIds, String principalId) {
        return getDelegationMemberBoList(RoleDaoAction.DELEGATION_PRINCIPALS_FOR_PRINCIPAL_ID_AND_DELEGATION_IDS,
                delegationIds, principalId, null);
    }

    /**
     * @return a DelegateMemberBo object by its ID. If the delegation member already exists in the cache, this method
     *         will return the cached version; otherwise, it will retrieve the uncached version from the database and
     *         then cache it before returning it.
     */
    protected DelegateMemberBo getDelegateMemberBo(String delegationMemberId) {
        if (StringUtils.isBlank(delegationMemberId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(DelegateMemberBo.class,
                Collections.singletonMap(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMemberId));
    }

    /**
     * @return a DelegateMemberBo List by (principal/group/role) member ID and delegation ID. If the List already
     *         exists in the cache, this method will return the cached one; otherwise, it will retrieve the uncached
     *         version from the database and then cache it before returning it.
     */
    protected List<DelegateMemberBo> getDelegationMemberBoListByMemberAndDelegationId(String memberId,
            String delegationId) {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put(KimConstants.PrimaryKeyConstants.MEMBER_ID, memberId);
        searchCriteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegationId);
        return new ArrayList<>(getBusinessObjectService().findMatching(DelegateMemberBo.class, searchCriteria));
    }

    protected Object getMember(String memberTypeCode, String memberId) {
        if (StringUtils.isBlank(memberId)) {
            return null;
        }
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            return getIdentityService().getPrincipal(memberId);
        } else if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
            return getGroupService().getGroup(memberId);
        } else if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            return getRoleBo(memberId);
        }
        return null;
    }

    protected String getMemberName(Object member) {
        if (member == null) {
            return "";
        }
        if (member instanceof Principal) {
            return ((Principal) member).getPrincipalName();
        }
        if (member instanceof Group) {
            return ((Group) member).getName();
        }
        if (member instanceof Role) {
            return ((Role) member).getName();
        }
        return member.toString();
    }

    protected RoleBo getRoleBo(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return null;
        }
        return getBusinessObjectService().findBySinglePrimaryKey(RoleBo.class, roleId);
    }

    protected RoleBoLite getRoleBoLite(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return null;
        }
        return getBusinessObjectService().findBySinglePrimaryKey(RoleBoLite.class, roleId);
    }

    protected DelegateTypeBo getDelegationOfType(String roleId, DelegationType delegationType) {
        List<DelegateTypeBo> roleDelegates = getRoleDelegations(roleId);
        if (isDelegationPrimary(delegationType)) {
            return getPrimaryDelegation(roleId, roleDelegates);
        } else {
            return getSecondaryDelegation(roleId, roleDelegates);
        }
    }

    private DelegateTypeBo getSecondaryDelegation(String roleId, List<DelegateTypeBo> roleDelegates) {
        DelegateTypeBo secondaryDelegate = null;
        RoleBoLite roleBo = getRoleBoLite(roleId);
        for (DelegateTypeBo delegate : roleDelegates) {
            if (isDelegationSecondary(delegate.getDelegationType())) {
                secondaryDelegate = delegate;
            }
        }
        if (secondaryDelegate == null) {
            secondaryDelegate = new DelegateTypeBo();
            secondaryDelegate.setRoleId(roleId);
            secondaryDelegate.setDelegationType(DelegationType.SECONDARY);
            secondaryDelegate.setKimTypeId(roleBo.getKimTypeId());
        }
        return secondaryDelegate;
    }

    protected DelegateTypeBo getPrimaryDelegation(String roleId, List<DelegateTypeBo> roleDelegates) {
        DelegateTypeBo primaryDelegate = null;
        RoleBoLite roleBo = getRoleBoLite(roleId);
        for (DelegateTypeBo delegate : roleDelegates) {
            if (isDelegationPrimary(delegate.getDelegationType())) {
                primaryDelegate = delegate;
            }
        }
        if (primaryDelegate == null) {
            primaryDelegate = new DelegateTypeBo();
            primaryDelegate.setRoleId(roleId);
            primaryDelegate.setDelegationType(DelegationType.PRIMARY);
            primaryDelegate.setKimTypeId(roleBo.getKimTypeId());
        }
        return primaryDelegate;
    }

    protected RoleMemberBo matchingMemberRecord(List<RoleMemberBo> roleMembers, String memberId,
            String memberTypeCode, Map<String, String> qualifier) {
        for (RoleMemberBo rm : roleMembers) {
            if (doesMemberMatch(rm, memberId, memberTypeCode, qualifier)) {
                return rm;
            }
        }
        return null;
    }

    protected boolean isDelegationPrimary(DelegationType delegationType) {
        return DelegationType.PRIMARY.equals(delegationType);
    }

    protected boolean isDelegationSecondary(DelegationType delegationType) {
        return DelegationType.SECONDARY.equals(delegationType);
    }

    private List<DelegateTypeBo> getRoleDelegations(String roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return getStoredDelegationImplsForRoleIds(Collections.singletonList(roleId));

    }

    protected RoleBo getRoleBoByName(String namespaceCode, String roleName) {
        if (StringUtils.isBlank(namespaceCode) || StringUtils.isBlank(roleName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, roleName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return getBusinessObjectService().findByPrimaryKey(RoleBo.class, criteria);
    }

    protected RoleBoLite getRoleBoLiteByName(String namespaceCode, String roleName) {
        if (StringUtils.isBlank(namespaceCode) || StringUtils.isBlank(roleName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, roleName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return getBusinessObjectService().findByPrimaryKey(RoleBoLite.class, criteria);
    }

    protected List<RoleMember> doAnyMemberRecordsMatchByExactQualifier(RoleContract role, String memberId,
            RoleDaoAction daoActionToTake, Map<String, String> qualifier) {
        List<RoleMemberBo> roleMemberBos = getRoleMembersByExactQualifierMatch(role, memberId, daoActionToTake,
                qualifier);
        List<RoleMember> roleMembers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(roleMemberBos)) {
            for (RoleMemberBo bo : roleMemberBos) {
                roleMembers.add(RoleMemberBo.to(bo));
            }
            return roleMembers;
        }

        return Collections.emptyList();
    }

    protected List<RoleMemberBo> getRoleMembersByExactQualifierMatch(RoleContract role, String memberId,
            RoleDaoAction daoActionToTake, Map<String, String> qualifier) {
        List<RoleMemberBo> rms = new ArrayList<>();
        RoleTypeService roleTypeService = getRoleTypeService(role.getId());
        if (roleTypeService != null) {
            List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
            if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                switch (daoActionToTake) {
                    case ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS:
                        // Search for group role members only.
                        rms = getStoredRoleGroupsForGroupIdsAndRoleIds(Collections.singletonList(role.getId()),
                                Collections.singletonList(memberId), populateQualifiersForExactMatch(qualifier,
                                        attributesForExactMatch));
                        break;
                    case ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS:
                        // Search for principal role members only.
                        rms = getStoredRolePrincipalsForPrincipalIdAndRoleIds(Collections.singletonList(role.getId()),
                                memberId, populateQualifiersForExactMatch(qualifier, attributesForExactMatch));
                        break;
                    case ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS:
                        // Search for roles as role members only.
                        List<RoleMemberBo> allRoleMembers = getStoredRoleMembershipsForRoleIdsAsMembers(
                                Collections.singletonList(role.getId()), populateQualifiersForExactMatch(qualifier,
                                        attributesForExactMatch));
                        for (RoleMemberBo rm : allRoleMembers) {
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
    protected RoleMember doAnyMemberRecordsMatch(List<RoleMemberBo> roleMembers, String memberId,
            String memberTypeCode, Map<String, String> qualifier) {
        for (RoleMemberBo rm : roleMembers) {
            if (rm.isActive() && doesMemberMatch(rm, memberId, memberTypeCode, qualifier)) {
                return RoleMemberBo.to(rm);
            }
        }
        return null;
    }

    protected boolean doesMemberMatch(RoleMemberBo roleMember, String memberId, String memberTypeCode,
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
            KimTypeService service = GlobalResourceLoader.getService(QName.valueOf(serviceName));
            if (service instanceof RoleTypeService) {
                return (RoleTypeService) service;
            }
            return (RoleTypeService) KimImplServiceLocator.getService("kimNoMembersRoleTypeService");
        } catch (Exception ex) {
            LOG.warn("Unable to find role type service with name: " + serviceName, ex);
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
        RoleBoLite roleBo = getRoleBoLite(roleId);
        if (roleBo != null) {
            KimType roleType = KimTypeBo.to(roleBo.getKimRoleType());
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
                KimTypeService service = GlobalResourceLoader.getService(QName.valueOf(serviceName));
                if (service instanceof RoleTypeService) {
                    return (RoleTypeService) service;
                }
                return (RoleTypeService) KimImplServiceLocator.getService("kimNoMembersRoleTypeService");
            } catch (Exception ex) {
                LOG.error("Unable to find role type service with name: " + serviceName, ex);
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
     * This is a modified version of similar code and logic from Rice's RoleServiceBase class.
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
     * This is a modified version of the base getKimAttributeId() code.
     */
    protected String getAttributeIdByName(String attributeName) {
        Map<String, Object> critieria = new HashMap<>(1);
        critieria.put("attributeName", attributeName);
        Collection<KimAttributeBo> defs = getBusinessObjectService().findMatching(KimAttributeBo.class, critieria);
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

    // For testability.
    void setBusinessObjectService(BusinessObjectService bos) {
        businessObjectService = bos;
    }

    protected LookupService getLookupService() {
        if (lookupService == null) {
            lookupService = KRADServiceLocatorWeb.getLookupService();
        }
        return lookupService;
    }

    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }

        return identityService;
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

    protected KimTypeInfoService getKimTypeInfoService() {
        if (kimTypeInfoService == null) {
            kimTypeInfoService = KimApiServiceLocator.getKimTypeInfoService();
        }
        return kimTypeInfoService;
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
        ROLE_MEMBERS_FOR_ROLE_IDS_WITH_FILTERS,
        DELEGATION_PRINCIPALS_FOR_PRINCIPAL_ID_AND_DELEGATION_IDS,
        DELEGATION_GROUPS_FOR_GROUP_IDS_AND_DELEGATION_IDS,
        DELEGATION_MEMBERS_FOR_DELEGATION_IDS
    }
}
