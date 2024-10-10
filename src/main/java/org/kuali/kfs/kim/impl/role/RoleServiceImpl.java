/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.cache.CacheKeyUtils;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.LookupCustomizer;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.common.delegate.DelegationTypeService;
import org.kuali.kfs.kim.framework.role.RoleTypeService;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMemberAttributeData;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.permission.Permission;
import org.kuali.kfs.kim.impl.responsibility.Responsibility;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.util.KimCommonUtilsInternal;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;

/**
 * CU Customization:
 * Updated the member-removal methods to properly convert the qualifiers where needed.
 */
public class RoleServiceImpl extends RoleServiceBase implements RoleService {

    private static final Logger LOG = LogManager.getLogger();

    private static final Map<String, RoleDaoAction> memberTypeToRoleDaoActionMap = populateMemberTypeToRoleDaoActionMap();
    private RoleService proxiedRoleService;
    private CacheManager cacheManager;
    private PersonService personService;
    private DateTimeService dateTimeService;

    private KimTypeInfoService kimTypeInfoService;

    public RoleServiceImpl() {
        cacheManager = new NoOpCacheManager();
    }

    private static Map<String, RoleDaoAction> populateMemberTypeToRoleDaoActionMap() {
        // TODO: Consider making this a constant field.
        return Map.ofEntries(
                entry(MemberType.GROUP.getCode(), RoleDaoAction.ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS),
                entry(MemberType.PRINCIPAL.getCode(), RoleDaoAction.ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS),
                entry(MemberType.ROLE.getCode(), RoleDaoAction.ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS)
        );
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleLite createRole(final RoleLite role) throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(role, "role");

        if (StringUtils.isNotBlank(role.getId()) && getRoleWithoutMembers(role.getId()) != null) {
            throw new IllegalStateException("the role to create already exists: " + role);
        }
        return getBusinessObjectService().save(role);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleLite updateRole(final RoleLite role) throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(role, "role");

        final RoleLite originalRole = getRoleLite(role.getId());
        if (StringUtils.isBlank(role.getId()) || originalRole == null) {
            throw new IllegalStateException("the role does not exist: " + role);
        }

        final RoleLite updatedRole = getBusinessObjectService().save(role);
        if (originalRole.isActive() && !updatedRole.isActive()) {
            KimImplServiceLocator.getRoleInternalService().roleInactivated(updatedRole.getId());
        }
        return updatedRole;
    }

    /**
     * This method tests to see if assigning a role to another role will create a circular reference.
     * The Role is checked to see if it is a member (direct or nested) of the role to be assigned as a member.
     *
     * @param newMemberId
     * @param role
     * @return true  - assignment is allowed, no circular reference will be created. false - illegal assignment, it
     *         will create a circular membership
     */
    protected boolean checkForCircularRoleMembership(final String newMemberId, final Role role) {
        // get all nested role members that are of type role
        final Set<String> newRoleMemberIds = getRoleTypeRoleMemberIds(newMemberId);
        return !newRoleMemberIds.contains(role.getId());
    }

    @Override
    public GenericQueryResults<RoleMember> findRoleMembers(final QueryByCriteria queryByCriteria) throws IllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        //KULRICE-8972 lookup customizer for attribute transform
        final LookupCustomizer.Builder<RoleMember> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(org.kuali.kfs.kim.impl.common.attribute.AttributeTransform.getInstance());

        return getCriteriaLookupService().lookup(RoleMember.class, queryByCriteria, lc.build());
    }

    @Cacheable(cacheNames = RoleMember.CACHE_NAME, key = "'{getRoleTypeRoleMemberIds}' + 'roleId=' + #p0")
    @Override
    public Set<String> getRoleTypeRoleMemberIds(final String roleId) throws IllegalArgumentException {
        incomingParamCheck(roleId, "roleId");

        final Set<String> results = new HashSet<>();
        getNestedRoleTypeMemberIds(roleId, results);
        return Collections.unmodifiableSet(results);
    }

    @Cacheable(cacheNames = RoleMembership.CACHE_NAME, key = "'memberType=' + #p0 + '|' + 'memberId=' + #p1")
    @Override
    public List<String> getMemberParentRoleIds(final String memberType, final String memberId) throws IllegalStateException {
        incomingParamCheck(memberType, "memberType");
        incomingParamCheck(memberId, "memberId");

        final List<RoleMember> parentRoleMembers = getRoleDao().getRoleMembershipsForMemberId(memberType, memberId,
                Collections.emptyMap());

        final List<String> parentRoleIds = new ArrayList<>(parentRoleMembers.size());
        for (final RoleMember parentRoleMember : parentRoleMembers) {
            parentRoleIds.add(parentRoleMember.getRoleId());
        }

        return parentRoleIds;
    }

    @Cacheable(cacheNames = RoleResponsibility.CACHE_NAME, key = "'roleMemberId=' + #p0")
    @Override
    public List<RoleResponsibilityAction> getRoleMemberResponsibilityActions(final String roleMemberId) throws
            IllegalStateException {
        incomingParamCheck(roleMemberId, "roleMemberId");

        final Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMemberId);

        return (List<RoleResponsibilityAction>) getBusinessObjectService()
                .findMatching(RoleResponsibilityAction.class, criteria);
    }

    @Override
    public GenericQueryResults<DelegateMember> findDelegateMembers(final QueryByCriteria queryByCriteria) throws
            IllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        //KULRICE-8972 lookup customizer for attribute transform
        final LookupCustomizer.Builder<DelegateMember> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(org.kuali.kfs.kim.impl.common.attribute.AttributeTransform.getInstance());

        return getCriteriaLookupService().lookup(DelegateMember.class, queryByCriteria, lc.build());
    }

    @Cacheable(cacheNames = Role.CACHE_NAME, key = "'{" + Role.CACHE_NAME + "}id=' + #p0")
    @Override
    public RoleLite getRoleWithoutMembers(final String roleId) throws IllegalStateException {
        incomingParamCheck(roleId, "roleId");
        return loadRole(roleId);
    }

    /**
     * Loads the role with the given id, leveraging the cache where possible and querying the database if role not
     * already in the cache. If the role is not in the cache, then it will be placed in the cache once it is loaded.
     */
    protected RoleLite loadRole(final String roleId) {
        RoleLite role = getRoleFromCache(roleId);
        if (role == null) {
            final RoleLite roleLite = getRoleLite(roleId);
            if (roleLite != null) {
                role = roleLite;
                putRoleInCache(role);
            }
        }
        return role;
    }

    protected RoleLite getRoleFromCache(final String id) {
        final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
        final Cache.ValueWrapper cachedValue = cache.get("{" + Role.CACHE_NAME + "}id=" + id);
        if (cachedValue != null) {
            return (RoleLite) cachedValue.get();
        }
        return null;
    }

    protected RoleLite getRoleFromCache(final String namespaceCode, final String name) {
        final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
        final String key = "{" + Role.CACHE_NAME + "}namespaceCode=" + namespaceCode + "|name=" + name;
        final Cache.ValueWrapper cachedValue = cache.get(key);
        if (cachedValue != null) {
            return (RoleLite) cachedValue.get();
        }
        return null;
    }

    protected List<RoleLite> getRolesFromCache(final String namespaceCode, final String name) {
        final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
        final String key = "{" + Role.CACHE_NAME + "s}namespaceCode=" + namespaceCode + "|name=" + name;
        final Cache.ValueWrapper cachedValue = cache.get(key);
        if (cachedValue != null) {
            return (List<RoleLite>) cachedValue.get();
        }
        return List.of();
    }

    protected void putRoleInCache(final RoleLite role) {
        if (role != null) {
            final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
            final String idKey = "{" + Role.CACHE_NAME + "}id=" + role.getId();
            final String nameKey = "{" + Role.CACHE_NAME + "}namespaceCode=" + role.getNamespaceCode() + "|name="
                                   + role.getName();
            cache.put(idKey, role);
            cache.put(nameKey, role);
        }
    }

    protected void putRolesInCache(final List<RoleLite> roles, final String namespaceCode, final String name) {
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(this::putRoleInCache);
            final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
            final String nameKey = "{" + Role.CACHE_NAME + "s}namespaceCode=" + namespaceCode + "|name="
                                   + name;
            cache.put(nameKey, roles);
        }
    }

    protected Map<String, RoleLite> getRoleLiteMap(final Collection<String> roleIds) {
        final Map<String, RoleLite> result;
        // check for a non-null result in the cache, return it if found
        if (roleIds.size() == 1) {
            final String roleId = roleIds.iterator().next();
            final RoleLite bo = getRoleLite(roleId);
            if (bo == null) {
                return Collections.emptyMap();
            }
            result = bo.isActive() ? Collections.singletonMap(roleId, bo) : Collections.emptyMap();
        } else {
            result = new HashMap<>(roleIds.size());
            for (final String roleId : roleIds) {
                final RoleLite bo = getRoleLite(roleId);
                if (bo != null && bo.isActive()) {
                    result.put(roleId, bo);
                }
            }
        }
        return result;
    }

    @Cacheable(cacheNames = Role.CACHE_NAME, key = "'ids=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0)")
    @Override
    public List<RoleLite> getRoles(final List<String> roleIds) throws IllegalStateException {
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new IllegalArgumentException("roleIds is null or empty");
        }
        return Collections.unmodifiableList(loadRoles(roleIds));
    }

    /**
     * Loads the roles with the given ids, leveraging the cache where possible and querying the database for role ids
     * not already in the cache. If the role is not in the cache, then it will be placed in the cache once it is
     * loaded.
     */
    protected List<RoleLite> loadRoles(final List<String> roleIds) {
        final List<String> remainingRoleIds = new ArrayList<>();
        final Map<String, RoleLite> roleMap = new HashMap<>(roleIds.size());
        for (final String roleId : roleIds) {
            final RoleLite role = getRoleFromCache(roleId);
            if (role != null) {
                roleMap.put(roleId, role);
            } else {
                remainingRoleIds.add(roleId);
            }
        }
        if (!remainingRoleIds.isEmpty()) {
            final Map<String, RoleLite> roleLiteMap = getRoleLiteMap(remainingRoleIds);
            for (final String roleId : roleLiteMap.keySet()) {
                final RoleLite roleLite = roleLiteMap.get(roleId);
                if (roleLite != null) {
                    roleMap.put(roleId, roleLite);
                    putRoleInCache(roleLite);
                }
            }
        }
        return new ArrayList<>(roleMap.values());
    }

    @Cacheable(cacheNames = Role.CACHE_NAME,
            key = "'{" + Role.CACHE_NAME + "}-namespaceCode=' + #p0 + '|' + 'name='+ #p1")
    @Override
    public RoleLite getRoleByNamespaceCodeAndName(final String namespaceCode, final String roleName) throws IllegalStateException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        return loadRoleByName(namespaceCode, roleName);
    }

    @Cacheable(cacheNames = Role.CACHE_NAME,
            key = "'{" + Role.CACHE_NAME + "}-namespaceCode=' + #p0 + '|' + 'name='+ #p1")
    @Override
    public List<RoleLite> getAllRolesByNamespaceCodeAndName(final String namespaceCode,
            final String roleName) throws IllegalStateException {
        return loadAllRolesByName(namespaceCode, roleName);
    }

    /**
     * Loads the role with the given name, leveraging the cache where possible and querying the database if role not
     * already in the cache. If the role is not in the cache, then it will be placed in the cache once it is loaded.
     */
    protected RoleLite loadRoleByName(final String namespaceCode, final String roleName) {
        RoleLite role = getRoleFromCache(namespaceCode, roleName);
        if (role == null) {
            final RoleLite roleLite = getRoleLiteByName(namespaceCode, roleName);
            if (roleLite != null) {
                role = getRoleFromCache(roleLite.getId());
                if (role == null) {
                    role = roleLite;
                }
                putRoleInCache(role);
            }
        }
        return role;
    }

    /**
     * Loads the role with the given name, leveraging the cache where possible and querying the database if role not
     * already in the cache. If the role is not in the cache, then it will be placed in the cache once it is loaded.
     */
    protected List<RoleLite> loadAllRolesByName(final String namespaceCode, final String roleName) {
        List<RoleLite> roles = getRolesFromCache(namespaceCode, roleName);
        if (roles.isEmpty()) {
            final List<RoleLite> roleLites = getRoleLitesByName(namespaceCode, roleName);
            if (!roleLites.isEmpty()) {
                roles = getRolesFromCache(namespaceCode, roleName);
                if (roles.isEmpty()) {
                    roles = roleLites;
                }
                putRolesInCache(roles, namespaceCode, roleName);
            }
        }
        return roles;
    }

    @Cacheable(cacheNames = Role.CACHE_NAME,
            key = "'{getRoleIdByNamespaceCodeAndName}' + 'namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public String getRoleIdByNamespaceCodeAndName(final String namespaceCode, final String roleName) throws
            IllegalStateException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        final RoleLite role = getRoleByNamespaceCodeAndName(namespaceCode, roleName);
        if (role != null) {
            return role.getId();
        } else {
            return null;
        }
    }

    @Cacheable(cacheNames = Role.CACHE_NAME,
            key = "'{getAllRoleIdsByNamespaceCodeAndName}' + 'namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public List<String> getAllRoleIdsByNamespaceCodeAndName(final String namespaceCode, final String roleName) throws
            IllegalStateException {
        final List<RoleLite> roles = getAllRolesByNamespaceCodeAndName(namespaceCode, roleName);
        if (!roles.isEmpty()) {
            return roles.stream().map(RoleLite::getId).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @Cacheable(cacheNames = Role.CACHE_NAME, key = "'{isRoleActive}' + 'id=' + #p0")
    @Override
    public boolean isRoleActive(final String roleId) throws IllegalStateException {
        incomingParamCheck(roleId, "roleId");
        final RoleLite role = getRoleWithoutMembers(roleId);
        return role != null && role.isActive();
    }

    @Override
    public List<Map<String, String>> getRoleQualifersForPrincipalByNamespaceAndRolename(
            final String principalId,
            final String namespaceCode, final String roleName, final Map<String, String> qualification)
            throws IllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        final String roleId = getRoleIdByNamespaceCodeAndName(namespaceCode, roleName);
        if (roleId == null) {
            return Collections.emptyList();
        }
        return getNestedRoleQualifiersForPrincipalByRoleIds(principalId, Collections.singletonList(roleId),
                qualification);
    }

    @Override
    public List<Map<String, String>> getNestedRoleQualifiersForPrincipalByRoleIds(
            final String principalId,
            final List<String> roleIds, final Map<String, String> qualification) throws IllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(roleIds, "roleIds");

        final List<Map<String, String>> results = new ArrayList<>();

        final Map<String, RoleLite> rolesById = getRoleLiteMap(roleIds);

        // get the person's groups
        final List<String> groupIds = getGroupService().getGroupIdsByPrincipalId(principalId);
        final List<RoleMember> roleMembers = getStoredRoleMembersUsingExactMatchOnQualification(principalId, groupIds,
                roleIds, qualification);

        final Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<>();
        for (final RoleMember roleMember : roleMembers) {
            final RoleTypeService roleTypeService = getRoleTypeService(roleMember.getRoleId());
            // gather up the qualifier sets and the service they go with
            if (MemberType.PRINCIPAL.equals(roleMember.getType())
                    || MemberType.GROUP.equals(roleMember.getType())) {
                if (roleTypeService != null) {
                    final List<RoleMembership> las =
                            roleIdToMembershipMap.computeIfAbsent(roleMember.getRoleId(), k -> new ArrayList<>());
                    final RoleMembership mi = RoleMembership.Builder.create(roleMember.getRoleId(), roleMember.getId(),
                            roleMember.getMemberId(), roleMember.getType(), roleMember.getAttributes()).build();

                    las.add(mi);
                } else {
                    results.add(roleMember.getAttributes());
                }
            } else if (MemberType.ROLE.equals(roleMember.getType())) {
                // find out if the user has the role
                // need to convert qualification using this role's service
                Map<String, String> nestedQualification = qualification;
                if (roleTypeService != null) {
                    final RoleLite roleLite = rolesById.get(roleMember.getRoleId());
                    // pulling from here as the nested role is not necessarily (and likely is not)
                    // in the rolesById Map created earlier
                    final RoleLite nestedRole = getRoleLite(roleMember.getMemberId());
                    // it is possible that the the roleTypeService is coming from a remote application and therefore
                    // it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {
                        nestedQualification = roleTypeService.convertQualificationForMemberRoles(
                                roleLite.getNamespaceCode(), roleLite.getName(), nestedRole.getNamespaceCode(),
                                nestedRole.getName(), qualification);
                    } catch (final Exception ex) {
                        LOG.warn(
                                "Not able to retrieve RoleTypeService from remote system for role Id: {}",
                                roleLite::getId,
                                () -> ex
                        );
                    }
                }
                final List<String> nestedRoleId = new ArrayList<>(1);
                nestedRoleId.add(roleMember.getMemberId());
                // if the user has the given role, add the qualifier the *nested role* has with the
                // originally queries role
                if (getProxiedRoleService().principalHasRole(principalId, nestedRoleId, nestedQualification,
                        false)) {
                    results.add(roleMember.getAttributes());
                }
            }
        }
        for (final Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
            final RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
            // it is possible that the the roleTypeService is coming from a remote and therefore it can't be
            // guaranteed that it is up and working, so using a try/catch to catch this possibility.
            try {
                final List<RoleMembership> matchingMembers = roleTypeService.getMatchingRoleMemberships(qualification,
                        entry.getValue());
                for (final RoleMembership roleMembership : matchingMembers) {
                    results.add(roleMembership.getQualifier());
                }
            } catch (final Exception ex) {
                LOG.warn(
                        "Not able to retrieve RoleTypeService from remote system for role Id: {}",
                        entry::getKey,
                        () -> ex
                );
            }
        }
        return Collections.unmodifiableList(results);
    }

    @Cacheable(cacheNames = RoleMember.CACHE_NAME,
            key = "'namespaceCode=' + #p0 + '|' + 'roleName=' + #p1 + '|' + 'qualification=' +" +
                    "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p2)",
            condition = "!T(org.kuali.kfs.kim.api.cache.KimCacheUtils).isDynamicMembshipRoleByNamespaceAndName(#p0, #p1)")
    @Override
    public Collection<String> getRoleMemberPrincipalIds(
            final String namespaceCode, final String roleName,
            final Map<String, String> qualification) throws IllegalStateException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        final Set<String> principalIds = new HashSet<>();
        final Set<String> foundRoleTypeMembers = new HashSet<>();
        final List<String> roleIds = Collections.singletonList(getRoleIdByNamespaceCodeAndName(namespaceCode, roleName));
        for (final RoleMembership roleMembership : getRoleMembers(roleIds, qualification, false, foundRoleTypeMembers)) {
            if (MemberType.GROUP.equals(roleMembership.getType())) {
                principalIds.addAll(getGroupService().getMemberPrincipalIds(roleMembership.getMemberId()));
            } else {
                principalIds.add(roleMembership.getMemberId());
            }
        }

        return Collections.unmodifiableSet(principalIds);
    }

    @Cacheable(cacheNames = RoleMember.CACHE_NAME,
            key = "'namespaceCode=' + #p0 + '|' + 'roleName=' + #p1 + '|' + 'qualification=' +" +
                  "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p2)",
            condition = "!T(org.kuali.kfs.kim.api.cache.KimCacheUtils).isDynamicMembshipAllRolesByNamespaceAndName(#p0, #p1)")
    @Override
    public Collection<String> getRoleMemberPrincipalIdsAllowNull(
            final String namespaceCode, final String roleName,
            final Map<String, String> qualification) throws IllegalStateException {
        final Set<String> principalIds = new HashSet<>();
        final Set<String> foundRoleTypeMembers = new HashSet<>();
        final List<String> roleIds = getAllRoleIdsByNamespaceCodeAndName(namespaceCode, roleName);
        for (final RoleMembership roleMembership : getRoleMembers(roleIds, qualification, false, foundRoleTypeMembers)) {
            if (MemberType.GROUP.equals(roleMembership.getType())) {
                principalIds.addAll(getGroupService().getMemberPrincipalIds(roleMembership.getMemberId()));
            } else {
                principalIds.add(roleMembership.getMemberId());
            }
        }

        return Collections.unmodifiableSet(principalIds);
    }

    @Cacheable(cacheNames = RoleMember.CACHE_NAME,
            key = "'getPrincipalIdSubListWithRole' + 'principalIds=' + " +
                "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0) + '|' + 'roleNamespaceCode=' + #p1 + " +
                "'|' + 'roleName=' + #p2 + '|' + 'qualification=' + " +
                "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p3)",
            condition = "!T(org.kuali.kfs.kim.api.cache.KimCacheUtils).isDynamicMembshipRoleByNamespaceAndName(#p1, #p2)")
    @Override
    public List<String> getPrincipalIdSubListWithRole(
            final List<String> principalIds, final String roleNamespaceCode,
            final String roleName, final Map<String, String> qualification) throws IllegalStateException {
        incomingParamCheck(principalIds, "principalIds");
        incomingParamCheck(roleNamespaceCode, "roleNamespaceCode");
        incomingParamCheck(roleName, "roleName");

        final List<String> subList = new ArrayList<>();
        final RoleLite role = getRoleLiteByName(roleNamespaceCode, roleName);
        for (final String principalId : principalIds) {
            if (getProxiedRoleService().principalHasRole(principalId, Collections.singletonList(role.getId()),
                    qualification)) {
                subList.add(principalId);
            }
        }
        return Collections.unmodifiableList(subList);
    }

    @Cacheable(cacheNames = RoleMember.CACHE_NAME,
            key = "'getPrincipalIdSubListWithRoleAllowNull' + 'principalIds=' + " +
                  "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0) + '|' + 'roleNamespaceCode=' + #p1 + " +
                  "'|' + 'roleName=' + #p2 + '|' + 'qualification=' + " +
                  "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p3)",
            condition = "!T(org.kuali.kfs.kim.api.cache.KimCacheUtils).isDynamicMembshipAllRolesByNamespaceAndName"
                        + "(#p1, "
                        + "#p2)")
    @Override
    public List<String> getPrincipalIdSubListWithRoleAllowNull(
            final List<String> principalIds,
            final String roleNamespaceCode,
            final String roleName,
            final Map<String, String> qualification
    ) throws IllegalStateException {
        incomingParamCheck(principalIds, "principalIds");

        final List<String> subList = new ArrayList<>();
        final List<RoleLite> roles = getRoleLitesByName(roleNamespaceCode, roleName);
        for (final String principalId : principalIds) {
            if (getProxiedRoleService().principalHasRole(principalId,
                    roles.stream().map(RoleLite::getId).collect(Collectors.toList()),
                    qualification
            )) {
                subList.add(principalId);
            }
        }
        return Collections.unmodifiableList(subList);
    }

    @Override
    public GenericQueryResults<RoleLite> findRoles(final QueryByCriteria queryByCriteria) throws IllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        return getCriteriaLookupService().lookup(RoleLite.class, queryByCriteria);
    }

    @Cacheable(cacheNames = RoleMembership.CACHE_NAME,
            key = "'roleIds=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0)")
    @Override
    public List<RoleMembership> getFirstLevelRoleMembers(final List<String> roleIds) throws IllegalStateException {
        incomingParamCheck(roleIds, "roleIds");
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<RoleMember> roleMemberList = new ArrayList<>();
        for (final String roleId: roleIds) {
            roleMemberList.addAll(getStoredRoleMembersForRoleId(roleId, null, null));
        }
        final List<RoleMembership> roleMemberships = new ArrayList<>();
        for (final RoleMember roleMember : roleMemberList) {
            final RoleMembership roleMembeship = RoleMembership.Builder.create(
                    roleMember.getRoleId(),
                    roleMember.getId(),
                    roleMember.getMemberId(),
                    roleMember.getType(),
                    roleMember.getAttributes()).build();
            roleMemberships.add(roleMembeship);
        }
        return Collections.unmodifiableList(roleMemberships);
    }

    @Cacheable(cacheNames = DelegateMember.CACHE_NAME, key = "'{getDelegationMemberById}-id=' + #p0")
    @Override
    public DelegateMember getDelegationMemberById(final String delegationMemberId) throws IllegalStateException {
        incomingParamCheck(delegationMemberId, "delegationMemberId");

        return getDelegateMember(delegationMemberId);
    }

    @Cacheable(cacheNames = RoleResponsibility.CACHE_NAME, key = "'{getRoleResponsibilities}-roleId=' + #p0")
    @Override
    public List<RoleResponsibility> getRoleResponsibilities(final String roleId) throws IllegalStateException {
        incomingParamCheck(roleId, "roleId");

        final Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, roleId);
        return (List<RoleResponsibility>) getBusinessObjectService().findMatching(RoleResponsibility.class,
                criteria);
    }

    @Cacheable(cacheNames = DelegateType.CACHE_NAME, key = "'roleId=' + #p0 + '|' + 'delegateType=' + #p1")
    @Override
    public DelegateType getDelegateTypeByRoleIdAndDelegateTypeCode(final String roleId, final DelegationType delegationType)
            throws IllegalStateException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(delegationType, "delegationType");

        return getDelegationOfType(roleId, delegationType);
    }

    @Cacheable(cacheNames = DelegateType.CACHE_NAME, key = "'delegationId=' + #p0")
    @Override
    public DelegateType getDelegateTypeByDelegationId(final String delegationId) throws IllegalStateException {
        incomingParamCheck(delegationId, "delegationId");

        return getKimDelegationImpl(delegationId);
    }

    @Cacheable(value = RoleMember.CACHE_NAME,
            key = "'roleIds=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0) + '|' + 'qualification=' + " +
                    "T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p1)",
            condition = "!T(org.kuali.kfs.kim.api.cache.KimCacheUtils).isDynamicRoleMembership(#p0)")
    @Override
    public List<RoleMembership> getRoleMembers(final List<String> roleIds, final Map<String, String> qualification)
            throws IllegalStateException {
        incomingParamCheck(roleIds, "roleIds");

        final Set<String> foundRoleTypeMembers = new HashSet<>();
        final List<RoleMembership> roleMembers = getRoleMembers(roleIds, qualification, true, foundRoleTypeMembers);

        return Collections.unmodifiableList(roleMembers);
    }

    protected List<RoleMembership> getRoleMembers(
            final List<String> roleIds, final Map<String, String> qualification,
            final boolean followDelegations, final Set<String> foundRoleTypeMembers) {
        List<RoleMembership> results = new ArrayList<>();
        final Set<String> allRoleIds = new HashSet<>();
        for (final String roleId : roleIds) {
            if (getProxiedRoleService().isRoleActive(roleId)) {
                allRoleIds.add(roleId);
            }
        }
        // short-circuit if no roles match
        if (allRoleIds.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<String> matchingRoleIds = new HashSet<>(allRoleIds.size());
        // for efficiency, retrieve all roles and store in a map
        final Map<String, RoleLite> roles = getRoleLiteMap(allRoleIds);

        final List<String> copyRoleIds = new ArrayList<>(allRoleIds);
        final List<RoleMember> rms = new ArrayList<>();

        for (final String roleId : allRoleIds) {
            final RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                final List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                    copyRoleIds.remove(roleId);
                    rms.addAll(getStoredRoleMembersForRoleId(roleId, null,
                            populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                }
            }
        }
        for (final String copyRoleId: copyRoleIds) {
            rms.addAll(getStoredRoleMembersForRoleId(copyRoleId, null, null));
        }

        // build a map of role ID to membership information
        // this will be used for later qualification checks
        final Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<>();
        for (final RoleMember roleMember : rms) {
            final RoleMembership mi = RoleMembership.Builder.create(
                    roleMember.getRoleId(),
                    roleMember.getId(),
                    roleMember.getMemberId(),
                    roleMember.getType(),
                    roleMember.getAttributes()).build();

            // if the qualification check does not need to be made, just add the result
            if (qualification == null || qualification.isEmpty()) {
                if (MemberType.ROLE.equals(roleMember.getType())) {
                    // if a role member type, do a non-recursive role member check to obtain the group and principal
                    // members of that role given the qualification
                    Map<String, String> nestedRoleQualification = qualification;
                    final RoleTypeService roleTypeService = getRoleTypeService(roleMember.getRoleId());
                    if (roleTypeService != null) {
                        // get the member role object
                        final RoleLite memberRole = getRoleLite(mi.getMemberId());
                        nestedRoleQualification = roleTypeService.convertQualificationForMemberRoles(
                                roles.get(roleMember.getRoleId()).getNamespaceCode(),
                                roles.get(roleMember.getRoleId()).getName(), memberRole.getNamespaceCode(),
                                memberRole.getName(), qualification);
                    }
                    if (getProxiedRoleService().isRoleActive(roleMember.getRoleId())) {
                        final Collection<RoleMembership> nestedRoleMembers = getNestedRoleMembers(nestedRoleQualification,
                                mi, foundRoleTypeMembers);
                        if (!nestedRoleMembers.isEmpty()) {
                            results.addAll(nestedRoleMembers);
                            matchingRoleIds.add(roleMember.getRoleId());
                        }
                    }
                } else {
                    results.add(mi);
                    matchingRoleIds.add(roleMember.getRoleId());
                }
                matchingRoleIds.add(roleMember.getRoleId());
            } else {
                final List<RoleMembership> lrmi =
                        roleIdToMembershipMap.computeIfAbsent(mi.getRoleId(), k -> new ArrayList<>());
                lrmi.add(mi);
            }
        }
        // if there is anything in the role to membership map, we need to check the role type services for those
        // entries
        if (!roleIdToMembershipMap.isEmpty()) {
            // for each role, send in all the qualifiers for that role to the type service
            // for evaluation, the service will return those which match
            for (final Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
                //it is possible that the the roleTypeService is coming from a remote application and therefore it
                // can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    final RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
                    final List<RoleMembership> matchingMembers = roleTypeService.getMatchingRoleMemberships(qualification,
                            entry.getValue());
                    // loop over the matching entries, adding them to the results
                    for (final RoleMembership roleMemberships : matchingMembers) {
                        if (MemberType.ROLE.equals(roleMemberships.getType())) {
                            // if a role member type, do a non-recursive role member check to obtain the group and
                            // principal members of that role given the qualification get the member role object
                            final RoleLite memberRole = getRoleLite(roleMemberships.getMemberId());
                            if (memberRole.isActive()) {
                                final Map<String, String> nestedRoleQualification =
                                        roleTypeService.convertQualificationForMemberRoles(
                                                roles.get(roleMemberships.getRoleId()).getNamespaceCode(),
                                                roles.get(roleMemberships.getRoleId()).getName(),
                                                memberRole.getNamespaceCode(), memberRole.getName(), qualification);
                                final Collection<RoleMembership> nestedRoleMembers = getNestedRoleMembers(
                                        nestedRoleQualification, roleMemberships, foundRoleTypeMembers);
                                if (!nestedRoleMembers.isEmpty()) {
                                    results.addAll(nestedRoleMembers);
                                    matchingRoleIds.add(roleMemberships.getRoleId());
                                }
                            }
                        } else {
                            results.add(roleMemberships);
                            matchingRoleIds.add(roleMemberships.getRoleId());
                        }
                    }
                } catch (final Exception ex) {
                    LOG.warn(
                            "Not able to retrieve RoleTypeService from remote system for role Id: {}",
                            entry::getKey,
                            () -> ex
                    );
                }
            }
        }

        // handle derived roles
        for (final String roleId : allRoleIds) {
            final RoleTypeService roleTypeService = getRoleTypeService(roleId);
            final RoleLite role = roles.get(roleId);
            // check if a derived role
            try {
                if (isDerivedRoleType(roleTypeService)) {
                    // for each derived role, get the list of principals and groups which are in that role given the
                    // qualification (per the role type service)
                    final List<RoleMembership> roleMembers = roleTypeService.getRoleMembersFromDerivedRole(
                            role.getNamespaceCode(), role.getName(), qualification);
                    if (!roleMembers.isEmpty()) {
                        matchingRoleIds.add(roleId);
                    }
                    for (final RoleMembership rm : roleMembers) {
                        final RoleMembership.Builder builder = RoleMembership.Builder.create(rm);
                        builder.setRoleId(roleId);
                        builder.setId("*");
                        results.add(builder.build());
                    }
                }
            } catch (final Exception ex) {
                LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: {}", roleId, ex);
            }
        }

        if (followDelegations && !matchingRoleIds.isEmpty()) {
            // we have a list of RoleMembershipInfo objects
            // need to get delegations for distinct list of roles in that list
            final Map<String, DelegateType> delegationIdToDelegationMap = getStoredDelegationImplMapFromRoleIds(
                    matchingRoleIds);
            if (!delegationIdToDelegationMap.isEmpty()) {
                final List<RoleMembership.Builder> membershipsWithDelegations =
                        applyDelegationsToRoleMembers(results, delegationIdToDelegationMap.values(), qualification);
                resolveDelegationMemberRoles(membershipsWithDelegations, qualification, foundRoleTypeMembers);
                final List<RoleMembership> roleMemberships = membershipsWithDelegations.stream()
                        .map(RoleMembership.Builder::build)
                        .collect(Collectors.toList());
                results = List.copyOf(roleMemberships);
            }
        }

        // sort the results if a single role type service can be identified for
        // all the matching role members
        if (results.size() > 1) {
            // if a single role: easy case
            if (matchingRoleIds.size() == 1) {
                final String roleId = matchingRoleIds.iterator().next();
                final RoleTypeService roleTypeService = getRoleTypeService(roleId);
                // it is possible that the the roleTypeService is coming from a remote application and therefore it
                // can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    if (roleTypeService != null) {
                        results = roleTypeService.sortRoleMembers(results);
                    }
                } catch (final Exception ex) {
                    LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: {}", roleId, ex);
                }
            } else if (matchingRoleIds.size() > 1) {
                // if more than one, check if there is only a single role type service
                String prevServiceName = null;
                boolean multipleServices = false;
                for (final String roleId : matchingRoleIds) {
                    final String serviceName = kimTypeInfoService.getKimType(getRoleWithoutMembers(roleId)
                            .getKimTypeId()).getServiceName();
                    if (prevServiceName != null && !StringUtils.equals(prevServiceName, serviceName)) {
                        multipleServices = true;
                        break;
                    }
                    prevServiceName = serviceName;
                }
                if (!multipleServices) {
                    final String roleId = matchingRoleIds.iterator().next();
                    // it is possible that the the roleTypeService is coming from a remote application and therefore
                    // it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {
                        final RoleTypeService kimRoleTypeService = getRoleTypeService(roleId);
                        if (kimRoleTypeService != null) {
                            results = kimRoleTypeService.sortRoleMembers(results);
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: {}", roleId, ex);
                    }
                } else {
                    LOG.warn(
                            "Did not sort role members - multiple role type services found.  Role Ids: {}",
                            matchingRoleIds
                    );
                }
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Checks each of the result records to determine if there are potentially applicable delegation members for that
     * role membership. If there are, applicable delegations and members will be linked to the RoleMemberships in the
     * given list. An updated list will be returned from this method which includes the appropriate linked delegations.
     */
    protected List<RoleMembership.Builder> applyDelegationsToRoleMembers(
            final List<RoleMembership> roleMemberships,
            final Collection<DelegateType> delegations, final Map<String, String> qualification) {
        final MultiValueMap<String, String> roleIdToRoleMembershipIds = new LinkedMultiValueMap<>();
        final Map<String, RoleMembership.Builder> roleMembershipIdToBuilder = new HashMap<>();
        final List<RoleMembership.Builder> roleMembershipBuilders = new ArrayList<>();
        // to make our algorithm less painful, let's do some indexing and load the given list of RoleMemberships into
        // builders
        for (final RoleMembership roleMembership : roleMemberships) {
            roleIdToRoleMembershipIds.add(roleMembership.getRoleId(), roleMembership.getId());
            final RoleMembership.Builder builder = RoleMembership.Builder.create(roleMembership);
            roleMembershipBuilders.add(builder);
            roleMembershipIdToBuilder.put(roleMembership.getId(), builder);
        }
        for (final DelegateType delegation : delegations) {
            // determine the candidate role memberships where this delegation can be mapped
            final List<String> candidateRoleMembershipIds = roleIdToRoleMembershipIds.get(delegation.getRoleId());
            if (CollectionUtils.isNotEmpty(candidateRoleMembershipIds)) {
                final DelegationTypeService delegationTypeService = getDelegationTypeService(delegation.getDelegationId());
                for (final DelegateMember delegationMember : delegation.getMembers()) {
                    // Make sure that the delegation member is active
                    if (delegationMember.isActive(dateTimeService.getLocalDateTimeNow()) &&
                        (delegationTypeService == null ||
                            delegationTypeService.doesDelegationQualifierMatchQualification(qualification,
                                    delegationMember.getQualifier()))) {
                        // if the member has no role member id, check qualifications and apply to all matching role
                        // memberships on the role
                        if (StringUtils.isBlank(delegationMember.getRoleMemberId())) {
                            final RoleTypeService roleTypeService = getRoleTypeService(delegation.getRoleId());
                            for (final String roleMembershipId : candidateRoleMembershipIds) {
                                final RoleMembership.Builder roleMembershipBuilder = roleMembershipIdToBuilder.get(
                                        roleMembershipId);
                                if (roleTypeService == null || roleTypeService.doesRoleQualifierMatchQualification(
                                        roleMembershipBuilder.getQualifier(), delegationMember.getQualifier())) {
                                    linkDelegateToRoleMembership(delegation, delegationMember,
                                            roleMembershipBuilder);
                                }
                            }
                        } else if (candidateRoleMembershipIds.contains(delegationMember.getRoleMemberId())) {
                            final RoleMembership.Builder roleMembershipBuilder = roleMembershipIdToBuilder.get(
                                    delegationMember.getRoleMemberId());
                            linkDelegateToRoleMembership(delegation, delegationMember, roleMembershipBuilder);
                        }
                    }
                }
            }
        }
        return roleMembershipBuilders;
    }

    protected void linkDelegateToRoleMembership(
            final DelegateType delegation,
            final DelegateMember delegateMember, final RoleMembership.Builder roleMembershipBuilder) {
        DelegateType delegateType = null;
        for (final DelegateType existingDelegateType : roleMembershipBuilder.getDelegates()) {
            if (existingDelegateType.getDelegationId().equals(delegation.getDelegationId())) {
                delegateType = existingDelegateType;
            }
        }
        if (delegateType == null) {
            delegateType = new DelegateType();
            delegateType.setDelegationType(delegation.getDelegationType());
            delegateType.setDelegationId(delegation.getDelegationId());
            delegateType.setRoleId(delegation.getRoleId());
            delegateType.setActive(delegation.getActive());
            delegateType.setKimTypeId(delegation.getKimTypeId());
            delegateType.setDelegationTypeCode(delegation.getDelegationTypeCode());
            delegateType.setDelegationMembers(new ArrayList<>());
            roleMembershipBuilder.getDelegates().add(delegateType);
        }
        delegateType.getMembers().add(delegateMember);
    }

    /**
     * Once the delegations for a RoleMembershipInfo object have been determined, any "role" member types need to be
     * resolved into groups and principals so that further KIM requests are not needed.
     */
    protected void resolveDelegationMemberRoles(
            final List<RoleMembership.Builder> membershipBuilders,
            final Map<String, String> qualification, final Set<String> foundRoleTypeMembers) {
        // check delegations assigned to this role
        for (final RoleMembership.Builder roleMembership : membershipBuilders) {
            // the applicable delegation IDs will already be set in the RoleMembership.Builder
            // this code examines those delegations and obtains the member groups and principals
            for (final DelegateType delegation : roleMembership.getDelegates()) {
                final List<DelegateMember> newMembers = new ArrayList<>();
                for (final DelegateMember member : delegation.getMembers()) {
                    if (MemberType.ROLE.equals(member.getType())) {
                        // loop over delegation roles and extract the role IDs where the qualifications match
                        final Collection<RoleMembership> delegateMembers = getRoleMembers(Collections.singletonList(
                                member.getMemberId()), qualification, false, foundRoleTypeMembers);
                        // loop over the role members and create the needed DelegationMember builders
                        for (final RoleMembership rmi : delegateMembers) {
                            final DelegateMember delegateMember = new DelegateMember();
                            KimCommonUtilsInternal.copyProperties(delegateMember, member);
                            delegateMember.setMemberId(rmi.getMemberId());
                            delegateMember.setType(rmi.getType());
                            newMembers.add(delegateMember);
                        }
                    } else {
                        newMembers.add(member);
                    }
                }
                delegation.setDelegationMembers(newMembers);
            }
        }
    }

    @Override
    public boolean principalHasRole(final String principalId, final List<String> roleIds, final Map<String, String> qualification)
            throws IllegalStateException {
        if (LOG.isDebugEnabled()) {
            logPrincipalHasRoleCheck(principalId, roleIds, qualification);
        }

        final boolean hasRole = getProxiedRoleService().principalHasRole(principalId, roleIds, qualification, true);
        LOG.debug("Result: {}", hasRole);
        return hasRole;
    }

    @Override
    public boolean principalHasRole(
            final String principalId, final List<String> roleIds, final Map<String, String> qualification,
            final boolean checkDelegations) {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(roleIds, "roleIds");
        return principalHasRole(new Context(principalId), principalId, roleIds, qualification, checkDelegations);
    }

    protected boolean principalHasRole(
            final Context context, final String principalId, final List<String> roleIds,
            final Map<String, String> qualification, final boolean checkDelegations) {
        /*
         * This method uses a multi-phase approach to determining if the given principal of any of the roles given
         * based on the qualification map that is passed.
         *
         * Phase 1: Check the cache to find if it's already been determined that the principal is a member of any of
         *          the roles with the given ids.
         * Phase 2: Perform exact database-level matching. This can be done for all roles if the given qualification
         *          map is null or empty since that means qualification matching does not need to be performed. It can
         *          also be done for roles who's RoleTypeService defines qualifiers for exact match.
         * Phase 3: Use RoleTypeService matching for roles which have not already been checked. Will need to determine
         *          which role memberships match the given principal then delegate to the appropriate RoleTypeService
         *          to execute matching logic.
         * Phase 4: Check nested roles.
         * Phase 5: For any cases where derived roles are used, determine if the principal is a member of those
         *          derived roles.
         * Phase 6: If checkDelegations is true, check if any delegations match
         */
        try {
            // Phase 1: first check if any of the role membership is cached, only proceed with checking the role ids
            // that aren't already cached
            final List<String> roleIdsToCheck = new ArrayList<>(roleIds.size());
            for (final String roleId : roleIds) {
                final Boolean hasRole = getPrincipalHasRoleFromCache(principalId, roleId, qualification, checkDelegations);
                if (hasRole != null) {
                    if (hasRole) {
                        return true;
                    }
                } else {
                    roleIdsToCheck.add(roleId);
                }
            }

            // load the roles, this will also filter out inactive roles!
            final List<RoleLite> roles = loadRoles(roleIdsToCheck);
            // short-circuit if no roles match
            if (roles.isEmpty()) {
                return false;
            }

            // Phase 2: If they didn't pass any qualifications or they are using exact qualifier matching, we can go
            // straight to the database

            final Set<String> rolesCheckedForExactMatch = new HashSet<>();
            for (final RoleLite role : roles) {
                Map<String, String> qualificationForExactMatch = null;
                if (qualification == null || qualification.isEmpty()) {
                    qualificationForExactMatch = new HashMap<>();
                } else {
                    final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
                    if (roleTypeService != null) {
                        final List<String> attributesForExactMatch = getQualifiersForExactMatch(role.getKimTypeId(),
                                roleTypeService);
                        if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                            qualificationForExactMatch = populateQualifiersForExactMatch(qualification,
                                    attributesForExactMatch);
                            if (qualificationForExactMatch.isEmpty()) {
                                // None of the attributes passed into principalHasRole matched attribute qualifiers on
                                // the roleTypeService.  In this case we want to skip the remaining processing and
                                // go onto the next role.
                                continue;
                            }
                        }
                    }
                }
                if (qualificationForExactMatch != null) {
                    rolesCheckedForExactMatch.add(role.getId());
                    final List<RoleMember> matchingRoleMembers = getStoredRolePrincipalsForPrincipalIdAndRoleId(
                            role.getId(), principalId, qualificationForExactMatch);
                    // if a role member matched our principal, we're good to go
                    if (CollectionUtils.isNotEmpty(matchingRoleMembers)) {
                        return putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                checkDelegations);
                    }
                    // now check groups
                    if (!context.getPrincipalGroupIds().isEmpty()) {
                        final List<RoleMember> matchingRoleGroupMembers =
                                getStoredRoleGroupsUsingExactMatchOnQualification(context.getPrincipalGroupIds(),
                                        role.getId(), qualification);
                        if (CollectionUtils.isNotEmpty(matchingRoleGroupMembers)) {
                            return putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                    checkDelegations);
                        }
                    }
                    // if we drop to this point, either we didn't match or the role has nested or derived role
                    // membership, we'll check that later
                }
            }

            // Phase 3: If we couldn't do an exact match, we need to work with the RoleTypeService in order to
            // perform matching

            for (final RoleLite role : roles) {
                // if we didn't do an exact match, we need to do a manual match
                if (!rolesCheckedForExactMatch.contains(role.getId())) {
                    final List<RoleMember> matchingPrincipalRoleMembers = getRoleMembersForPrincipalId(role.getId(),
                            principalId);
                    final List<RoleMember> matchingGroupRoleMembers = getRoleMembersForGroupIds(role.getId(),
                            context.getPrincipalGroupIds());
                    final List<RoleMembership> roleMemberships = convertToRoleMemberships(matchingPrincipalRoleMembers,
                            matchingGroupRoleMembers);
                    for (final RoleMembership roleMembership : roleMemberships) {
                        try {
                            final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
                            if (!roleTypeService.getMatchingRoleMemberships(qualification, roleMemberships).isEmpty()) {
                                return putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                        checkDelegations);
                            }
                        } catch (final Exception ex) {
                            LOG.warn("Unable to find role type service with id: {}", role::getKimTypeId);
                        }
                    }
                }
            }

            // Phase 4: If we have nested roles, execute a recursive check on those

            // first, check that the qualifiers on the role membership match then, perform a principalHasRole on the
            // embedded role
            final Map<String, RoleLite> roleIndex = new HashMap<>();
            for (final RoleLite role : roles) {
                roleIndex.put(role.getId(), role);
            }
            final List<RoleMember> roleMembers = new ArrayList<>();
            for (final String roleIndexId: roleIndex.keySet()) {
                roleMembers.addAll(getStoredRoleMembersForRoleId(roleIndexId, MemberType.ROLE.getCode(), null));
            }
            for (final RoleMember roleMember : roleMembers) {
                final RoleLite role = roleIndex.get(roleMember.getRoleId());
                final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
                if (roleTypeService != null) {
                    // it is possible that the the roleTypeService is coming from a remote application and therefore
                    // it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {
                        if (roleTypeService.doesRoleQualifierMatchQualification(qualification,
                                roleMember.getAttributes())) {
                            final RoleLite memberRole = getRoleLite(roleMember.getMemberId());
                            final Map<String, String> nestedRoleQualification =
                                    roleTypeService.convertQualificationForMemberRoles(role.getNamespaceCode(),
                                            role.getName(), memberRole.getNamespaceCode(), memberRole.getName(),
                                            qualification);
                            if (principalHasRole(context, principalId,
                                    Collections.singletonList(roleMember.getMemberId()), nestedRoleQualification,
                                    true)) {
                                return putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                        checkDelegations);
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.warn(
                                "Not able to retrieve RoleTypeService from remote system for role Id: {}",
                                roleMember::getRoleId,
                                () -> ex
                        );
                    }
                } else {
                    // no qualifiers - role is always used - check membership
                    // no role type service, so can't convert qualification - just pass as is
                    if (principalHasRole(context, principalId, Collections.singletonList(roleMember.getMemberId()),
                            qualification, true)) {
                        return putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                checkDelegations);
                    }
                }

            }

            // Phase 5: derived roles

            // check for derived roles and extract principals and groups from that - then check them against the
            // role type service passing in the qualification and principal - the qualifier comes from the
            // external system (application)
            for (final RoleLite role : roles) {
                // check if an derived role it is possible that the the roleTypeService is coming from a remote
                // application and therefore it can't be guaranteed that it is up and working, so using a try/catch
                // to catch this possibility.
                try {
                    final boolean isDerivedRoleType = context.isDerivedRoleType(role.getKimTypeId());
                    if (isDerivedRoleType) {
                        final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
                        if (roleTypeService.hasDerivedRole(principalId,
                                context.getPrincipalGroupIds(), role.getNamespaceCode(), role.getName(), qualification)) {
                            if (!roleTypeService.dynamicRoleMembership(role.getNamespaceCode(), role.getName())) {
                                putPrincipalHasRoleInCache(true, principalId, role.getId(), qualification,
                                        checkDelegations);
                            }
                            return true;
                        }
                    } else {
                        if (!checkDelegations) {
                            putPrincipalHasRoleInCache(false, principalId, role.getId(), qualification, false);
                        }
                    }
                } catch (final Exception ex) {
                    LOG.warn(
                            "Not able to retrieve RoleTypeService from remote system for role Id: {}",
                            role::getId,
                            () -> ex
                    );
                }
            }

            // Phase 6: delegations

            if (checkDelegations) {
                if (matchesOnDelegation(roleIndex.keySet(), principalId, context.getPrincipalGroupIds(),
                        qualification, context)) {
                    return true;
                }
            }
        } catch (final Exception e) {
            LOG.warn("Caught exception during a principalHasRole check", e);
        }
        return false;
    }

    protected Boolean getPrincipalHasRoleFromCache(
            final String principalId, final String roleId,
            final Map<String, String> qualification, final boolean checkDelegations) {
        final String key = buildPrincipalHasRoleCacheKey(principalId, roleId, qualification, checkDelegations);
        final Cache.ValueWrapper value = cacheManager.getCache(Role.CACHE_NAME).get(key);
        return value == null ? null : (Boolean) value.get();
    }

    protected boolean putPrincipalHasRoleInCache(
            final boolean principalHasRole, final String principalId, final String roleId,
            final Map<String, String> qualification, final boolean checkDelegations) {
        final String key = buildPrincipalHasRoleCacheKey(principalId, roleId, qualification, checkDelegations);
        cacheManager.getCache(Role.CACHE_NAME).put(key, principalHasRole);
        return principalHasRole;
    }

    private String buildPrincipalHasRoleCacheKey(
            final String principalId, final String roleId, final Map<String, String> qualification,
            final boolean checkDelegations) {
        return "{principalHasRole}principalId=" + principalId + "|roleId=" + roleId + "|qualification=" +
                CacheKeyUtils.mapKey(qualification) + "|checkDelegations=" + checkDelegations;
    }

    protected List<String> getQualifiersForExactMatch(final String kimTypeId, final RoleTypeService roleTypeService) {
        final String cacheKey = "{getQualifiersForExactMatch}kimTypeId=" + kimTypeId;
        final Cache cache = cacheManager.getCache(Role.CACHE_NAME);
        final Cache.ValueWrapper value = cache.get(cacheKey);
        List<String> qualifiers = new ArrayList<>();
        if (value == null) {
            try {
                qualifiers = roleTypeService.getQualifiersForExactMatch();
                cache.put(cacheKey, qualifiers);
            } catch (final Exception e) {
                LOG.warn("Caught exception when attempting to invoke a role type service", e);
            }
        } else {
            qualifiers = (List<String>) value.get();
        }
        return qualifiers;
    }

    public boolean isDerivedRoleType(final RoleTypeService service) {
        return service != null && service.isDerivedRoleType();
    }

    private boolean dynamicRoleMembership(final RoleTypeService service, final RoleLite role) {
        return service != null && role != null && service.dynamicRoleMembership(role.getNamespaceCode(),
                role.getName());
    }

    @Cacheable(value = Role.CACHE_NAME, key = "'{isDynamicRoleMembership}' + 'roleId=' + #p0")
    @Override
    public boolean isDynamicRoleMembership(final String roleId) {
        incomingParamCheck(roleId, "roleId");
        final RoleTypeService service = getRoleTypeService(roleId);
        try {
            return dynamicRoleMembership(service, getRoleWithoutMembers(roleId));
        } catch (final Exception e) {
            LOG.warn("Caught exception while invoking a role type service for role {}", roleId, e);
            // Returning true so the role won't be cached
            return true;
        }
    }

    /**
     * Support method for principalHasRole. Checks delegations on the passed in roles for the given principal and
     * groups. (It's assumed that the principal belongs to the given groups.)
     * <p>
     * Delegation checks are mostly the same as role checks except that the delegateType itself is qualified against the
     * original role (like a RolePrincipal or RoleGroup.) And then, the members of that delegateType may have additional
     * qualifiers which are not part of the original role qualifiers.
     * <p>
     * For example:
     * <p>
     * A role could be qualified by organization. So, there is a person in the organization with primary authority for
     * that org. But, then they delegate authority for that organization (not their authority - the delegateType is
     * attached to the org.) So, in this case the delegateType has a qualifier of the organization when it is attached
     * to the role.
     * <p>
     * The principals then attached to that delegateType (which is specific to the organization), may have additional
     * qualifiers.
     * For Example: dollar amount range, effective dates, document types.
     * As a subsequent step, those qualifiers are checked against the qualification passed in from the client.
     */
    protected boolean matchesOnDelegation(
            final Set<String> allRoleIds, final String principalId, final List<String> principalGroupIds,
            final Map<String, String> qualification, final Context context) {
        // get the list of delegations for the roles
        final Map<String, DelegateType> delegations = getStoredDelegationImplMapFromRoleIds(allRoleIds);

        // If there are no delegations then we should cache that the principal doesn't have the given roles if those
        // roles do not have dynamic membership
        if (delegations.isEmpty()) {
            for (final String roleId : allRoleIds) {
                final RoleLite role = loadRole(roleId);
                final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
                if (!context.isDerivedRoleType(role.getKimTypeId()) || roleTypeService == null
                        || !roleTypeService.dynamicRoleMembership(role.getNamespaceCode(), role.getName())) {
                    putPrincipalHasRoleInCache(false, principalId, roleId, qualification, true);
                }
            }
            return false;
        }

        // Build a map from a role ID to the delegations for that role ID
        final Map<String, List<DelegateType>> roleToDelegations = new HashMap<>();
        for (final DelegateType delegation : delegations.values()) {
            final List<DelegateType> roleDelegations =
                    roleToDelegations.computeIfAbsent(delegation.getRoleId(), k -> new ArrayList<>());
            roleDelegations.add(delegation);
        }
        // Iterate through each role and check its delegations to determine if the principal has one of the roles
        for (final String roleId : roleToDelegations.keySet()) {
            boolean matchesOnRoleDelegation = false;
            final RoleLite role = getRoleWithoutMembers(roleId);
            final RoleTypeService roleTypeService = context.getRoleTypeService(role.getKimTypeId());
            // Iterate through each delegation for this role and determine if the principal has the role through this
            // delegation
            for (final DelegateType delegation : roleToDelegations.get(roleId)) {
                // If the delegation isn't active skip it
                if (!delegation.isActive()) {
                    continue;
                }
                // Now iterate through all of the members of the delegation to determine if any of them apply to this
                // principal
                for (final DelegateMember delegateMember : delegation.getMembers()) {
                    // If the membership isn't active skip the rest of the checks
                    if (!delegateMember.isActive(new Timestamp(new Date().getTime()))) {
                        continue;
                    }
                    // If the membership is a principal type then check the delegate's member ID against the
                    // principal ID
                    if (MemberType.PRINCIPAL.equals(delegateMember.getType())
                            && !delegateMember.getMemberId().equals(principalId)) {
                        continue;
                    }
                    // If the membership is a group type then check to see if the group's ID is contained in the list
                    // of groups the principal belongs to
                    if (MemberType.GROUP.equals(delegateMember.getType())
                            && !principalGroupIds.contains(delegateMember.getMemberId())) {
                        continue;
                    }
                    // If the membership is a role type then we need to recurse
                    // into the principalHasRole method to check if this
                    // principal is a member of that role
                    if (MemberType.ROLE.equals(delegateMember.getType())
                            && !principalHasRole(principalId, Collections.singletonList(
                                    delegateMember.getMemberId()), qualification, false)) {
                        continue;
                    }

                    // OK, the member matches the current user, now check the qualifications

                    // NOTE: this compare is slightly different than the member enumeration
                    // since the requested qualifier is always being used rather than
                    // the role qualifier for the member (which is not available)

                    // it is possible that the the roleTypeService is coming from a remote application and therefore
                    // it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {
                        if (roleTypeService != null && !roleTypeService.doesRoleQualifierMatchQualification(
                                qualification, delegateMember.getQualifier())) {
                            continue;
                        }
                    } catch (final Exception ex) {
                        LOG.warn(
                                "Unable to call doesRoleQualifierMatchQualification on role type service for role Id: {} / {} / {}",
                                delegation::getRoleId,
                                () -> qualification,
                                delegateMember::getQualifier,
                                () -> ex
                        );
                        continue;
                    }

                    // role service matches this qualifier
                    // now try the delegateType service
                    final DelegationTypeService delegationTypeService = getDelegationTypeService(
                            delegateMember.getDelegationId());
                    // QUESTION: does the qualifier map need to be merged with the main delegateType qualification?
                    if (delegationTypeService != null
                            && !delegationTypeService.doesDelegationQualifierMatchQualification(qualification,
                                delegateMember.getQualifier())) {
                        continue;
                    }
                    // check if a role member ID is present on the delegateType record and that it's not a wildcard
                    // (since that would apply to more than one role member)
                    // if so, check that the original role member would match the given qualifiers
                    final String roleMemberId = delegateMember.getRoleMemberId();
                    if (StringUtils.isNotBlank(roleMemberId) && !StringUtils.equals("*", roleMemberId)) {
                        final RoleMember rm = getRoleMember(roleMemberId);
                        if (rm != null) {
                            // check that the original role member's is active and that their qualifier would have
                            // matched this request's qualifications (that the original person would have the
                            // permission/responsibility for an action) this prevents a role-membership based
                            // delegateType from surviving the inactivation/ changing of the main person's role
                            // membership
                            if (!rm.isActive(new Timestamp(new Date().getTime()))) {
                                continue;
                            }
                            final Map<String, String> roleQualifier = rm.getAttributes();
                            // it is possible that the the roleTypeService is coming from a remote application and
                            // therefore it can't be guaranteed that it is up and working, so using a try/catch to
                            // catch this possibility.
                            try {
                                if (roleTypeService != null
                                        && !roleTypeService.doesRoleQualifierMatchQualification(qualification,
                                        roleQualifier)) {
                                    continue;
                                }
                            } catch (final Exception ex) {
                                LOG.warn(
                                        "Unable to call doesRoleQualifierMatchQualification on role type service for role Id: {} / {} / {}",
                                        delegation::getRoleId,
                                        () -> qualification,
                                        () -> roleQualifier,
                                        () -> ex
                                );
                                continue;
                            }
                        } else {
                            LOG.warn("Unknown role member ID cited in the delegateType member table:");
                            LOG.warn(
                                    "       assignedToId: {} / roleMemberId: {}",
                                    delegateMember::getDelegationMemberId,
                                    delegateMember::getRoleMemberId
                            );
                        }
                    }
                    // If we've made it here then all of the tests pass so the principal must belong to this
                    // delegation so set the flag to true and break out of this loop
                    matchesOnRoleDelegation = true;
                    break;
                }

                // If we've found a match for one of the delegations break out of this loop
                if (matchesOnRoleDelegation) {
                    break;
                }
            }
            // If the role is not derived nor dynamic then cache the result of this since the principal has the role
            // through one of these delegations
            if (!context.isDerivedRoleType(role.getKimTypeId()) || roleTypeService == null
                    || !roleTypeService.dynamicRoleMembership(role.getNamespaceCode(), role.getName())) {
                putPrincipalHasRoleInCache(matchesOnRoleDelegation, principalId, roleId, qualification, true);
            }
            // If we've found a matching delegation skip processing the rest of the roles
            if (matchesOnRoleDelegation) {
                return true;
            }
        }
        // If we get here we didn't find a matching delegation so return false
        return false;
    }

    protected List<RoleMembership> convertToRoleMemberships(final List<RoleMember>... roleMemberLists) {
        final List<RoleMembership> roleMemberships = new ArrayList<>();
        for (final List<RoleMember> roleMembers : roleMemberLists) {
            for (final RoleMember roleMember : roleMembers) {
                final RoleMembership roleMembership = RoleMembership.Builder.create(roleMember.getRoleId(),
                        roleMember.getId(), roleMember.getMemberId(), roleMember.getType(),
                        roleMember.getAttributes()).build();
                roleMemberships.add(roleMembership);
            }
        }
        return roleMemberships;
    }

    /**
     * @return a KimDelegationImpl object by its ID. If the delegateType already exists in the cache, this method will
     *         return the cached version; otherwise, it will retrieve the uncached version from the database and then
     *         cache it before returning it.
     */
    protected DelegateType getKimDelegationImpl(final String delegationId) {
        if (StringUtils.isBlank(delegationId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(DelegateType.class,
                Collections.singletonMap(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegationId));
    }

    protected DelegationTypeService getDelegationTypeService(final String delegationId) {
        DelegationTypeService service = null;
        final DelegateType delegateType = getKimDelegationImpl(delegationId);
        final KimType kimType = kimTypeInfoService.getKimType(delegateType.getKimTypeId());
        if (kimType != null) {
            final KimTypeService tempService = KimFrameworkServiceLocator.getKimTypeService(kimType);
            if (tempService instanceof DelegationTypeService) {
                service = (DelegationTypeService) tempService;
            } else {
                LOG.error(
                        "Service returned for type {}({}) was not a DelegationTypeService.  Was a {}",
                        () -> kimType,
                        kimType::getName,
                        () -> tempService != null ? tempService.getClass() : "(null)"
                );
            }
        } else {
            // delegateType has no type - default to role type if possible
            final RoleTypeService roleTypeService = getRoleTypeService(delegateType.getRoleId());
            if (roleTypeService instanceof DelegationTypeService) {
                service = (DelegationTypeService) roleTypeService;
            }
        }
        return service;
    }

    protected Collection<RoleMembership> getNestedRoleMembers(
            final Map<String, String> qualification, final RoleMembership rm,
            final Set<String> foundRoleTypeMembers) {
        // If this role has already been traversed, skip it
        if (foundRoleTypeMembers.contains(rm.getMemberId())) {
            return new ArrayList<>();
        }
        foundRoleTypeMembers.add(rm.getMemberId());

        final ArrayList<String> roleIdList = new ArrayList<>(1);
        roleIdList.add(rm.getMemberId());

        // get the list of members from the nested role - ignore delegations on those sub-roles
        final Collection<RoleMembership> currentNestedRoleMembers = getRoleMembers(roleIdList, qualification, false,
                foundRoleTypeMembers);

        // add the roles whose members matched to the list for delegateType checks later
        final Collection<RoleMembership> returnRoleMembers = new ArrayList<>();
        for (final RoleMembership roleMembership : currentNestedRoleMembers) {
            final RoleMembership.Builder rmBuilder = RoleMembership.Builder.create(roleMembership);

            // use the member ID of the parent role (needed for responsibility joining)
            rmBuilder.setId(rm.getId());
            // store the role ID, so we know where this member actually came from
            rmBuilder.setRoleId(rm.getRoleId());
            rmBuilder.setEmbeddedRoleId(rm.getMemberId());
            returnRoleMembers.add(rmBuilder.build());
        }
        return returnRoleMembers;
    }

    private List<RoleMember> getStoredRoleMembersUsingExactMatchOnQualification(
            final String principalId,
            final List<String> groupIds, final List<String> roleIds, final Map<String, String> qualification) {
        final List<String> copyRoleIds = new ArrayList<>(roleIds);
        final List<RoleMember> roleMemberList = new ArrayList<>();

        for (final String roleId : roleIds) {
            final RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                try {
                    final List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                    if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                        copyRoleIds.remove(roleId);
                        roleMemberList.addAll(getStoredRoleMembersForRoleIdWithFilters(roleId, principalId, groupIds,
                                populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                    }
                } catch (final Exception e) {
                    LOG.warn("Caught exception when attempting to invoke a role type service for role {}", roleId, e);
                }
            }
        }
        for (final String copyRoleId: copyRoleIds) {
            roleMemberList.addAll(getStoredRoleMembersForRoleIdWithFilters(copyRoleId, principalId, groupIds, null));
        }
        return roleMemberList;
    }

    private List<RoleMember> getStoredRoleGroupsUsingExactMatchOnQualification(
            final List<String> groupIds, final String roleId,
            final Map<String, String> qualification) {
        final Set<String> roleIds = new HashSet<>();
        if (roleId != null) {
            roleIds.add(roleId);
        }
        return getStoredRoleGroupsUsingExactMatchOnQualification(groupIds, roleIds, qualification);
    }

    private List<RoleMember> getStoredRoleGroupsUsingExactMatchOnQualification(
            final List<String> groupIds,
            final Set<String> roleIds, final Map<String, String> qualification) {
        final List<String> copyRoleIds = new ArrayList<>(roleIds);
        final List<RoleMember> roleMembers = new ArrayList<>();

        for (final String roleId : roleIds) {
            final RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                try {
                    final List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                    if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                        copyRoleIds.remove(roleId);
                        roleMembers.addAll(getStoredRoleGroupsForGroupIdsAndRoleId(roleId, groupIds,
                                populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                    }
                } catch (final Exception e) {
                    LOG.warn("Caught exception when attempting to invoke a role type service for role {}", roleId, e);
                }
            }
        }
        for (final String copyRoleId: copyRoleIds) {
            roleMembers.addAll(getStoredRoleGroupsForGroupIdsAndRoleId(copyRoleId, groupIds, null));
        }
        return roleMembers;
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleMember assignPrincipalToRole(
            final String principalId, final String namespaceCode, final String roleName,
            final Map<String, String> qualifier) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        final RoleLite role = getRoleLiteByName(namespaceCode, roleName);

        // check that identical member does not already exist
        final List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(role, principalId,
                memberTypeToRoleDaoActionMap.get(MemberType.PRINCIPAL.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        final List<RoleMember> roleMembers = getRoleDao().getRoleMembersForRoleId(role.getId(),
                MemberType.PRINCIPAL.getCode(), qualifier);
        final RoleMember anyMemberMatch = doAnyMemberRecordsMatch(roleMembers, principalId, MemberType.PRINCIPAL.getCode(),
                qualifier);
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        // create the new role member object
        final RoleMember newRoleMember = new RoleMember();

        newRoleMember.setRoleId(role.getId());
        newRoleMember.setMemberId(principalId);
        newRoleMember.setType(MemberType.PRINCIPAL);

        // CU Customization KFSPTS-27162 Set default for activeFromDate field
        newRoleMember.setActiveFromDateValue(dateTimeService.getCurrentTimestamp());

        // build role member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, role.getKimTypeId());

        // add row to member table
        // When members are added to roles, clients must be notified.
        return getResponsibilityInternalService().saveRoleMember(newRoleMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleMember assignGroupToRole(
            final String groupId, final String namespaceCode, final String roleName,
                                        final Map<String, String> qualifier) throws IllegalStateException {
        incomingParamCheck(groupId, "groupId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        final Role role = getRoleByName(namespaceCode, roleName);

        // check that identical member does not already exist
        final List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(role, groupId,
                memberTypeToRoleDaoActionMap.get(MemberType.GROUP.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        final RoleMember anyMemberMatch = doAnyMemberRecordsMatch(role.getMembers(), groupId, MemberType.GROUP.getCode(),
                qualifier);
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        // create the new role member object
        final RoleMember newRoleMember = new RoleMember();
        newRoleMember.setRoleId(role.getId());
        newRoleMember.setMemberId(groupId);
        newRoleMember.setType(MemberType.GROUP);

        // CU Customization KFSPTS-27162 Set default for activeFromDate field
        newRoleMember.setActiveFromDateValue(dateTimeService.getCurrentTimestamp());

        // build role member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, role.getKimTypeId());

        // When members are added to roles, clients must be notified.
        return getResponsibilityInternalService().saveRoleMember(newRoleMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleMember assignRoleToRole(
            final String roleId, final String namespaceCode, final String roleName,
            final Map<String, String> qualifier) throws IllegalStateException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        final Role role = getRoleByName(namespaceCode, roleName);

        // check that identical member does not already exist
        final List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(role, roleId,
                memberTypeToRoleDaoActionMap.get(MemberType.ROLE.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        final RoleMember anyMemberMatch = doAnyMemberRecordsMatch(role.getMembers(), roleId, MemberType.ROLE.getCode(),
                qualifier);
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        // Check to make sure this doesn't create a circular membership
        if (!checkForCircularRoleMembership(roleId, role)) {
            throw new IllegalArgumentException("Circular role reference.");
        }
        // create the new roleBo member object
        final RoleMember newRoleMember = new RoleMember();
        newRoleMember.setRoleId(role.getId());
        newRoleMember.setMemberId(roleId);
        newRoleMember.setType(MemberType.ROLE);

        // CU Customization KFSPTS-27162 Set default for activeFromDate field
        newRoleMember.setActiveFromDateValue(dateTimeService.getCurrentTimestamp());

        // build roleBo member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, role.getKimTypeId());

        // When members are added to roles, clients must be notified.
        return getResponsibilityInternalService().saveRoleMember(newRoleMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleMember createRoleMember(final RoleMember roleMember) throws IllegalStateException {
        incomingParamCheck(roleMember, "roleMember");

        if (StringUtils.isNotBlank(roleMember.getId()) && getRoleMember(roleMember.getId()) != null) {
            throw new IllegalStateException("the roleMember to create already exists: " + roleMember);
        }

        final String kimTypeId = getRoleLite(roleMember.getRoleId()).getKimTypeId();
        final List<RoleMemberAttributeData> attrBos = KimAttributeData.createFrom(RoleMemberAttributeData.class,
                roleMember.getAttributes(), kimTypeId);

        roleMember.setAttributeDetails(attrBos);
        return getResponsibilityInternalService().saveRoleMember(roleMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleMember updateRoleMember(final RoleMember roleMember) throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(roleMember, "roleMember");

        RoleMember originalRoleMember = null;
        if (StringUtils.isNotBlank(roleMember.getId())) {
            originalRoleMember = getRoleMember(roleMember.getId());
        }
        if (StringUtils.isBlank(roleMember.getId()) || originalRoleMember == null) {
            throw new IllegalStateException("the roleMember to update does not exists: " + roleMember);
        }

        final String kimTypeId = getRoleLite(roleMember.getRoleId()).getKimTypeId();
        final List<RoleMemberAttributeData> attrBos = KimAttributeData.createFrom(RoleMemberAttributeData.class,
                roleMember.getAttributes(), kimTypeId);

        final List<RoleMemberAttributeData> updateAttrBos = new ArrayList<>();

        boolean matched = false;
        for (final RoleMemberAttributeData newRoleMemberAttrData : attrBos) {
            for (final RoleMemberAttributeData oldRoleMemberAttrData : originalRoleMember.getAttributeDetails()) {
                if (newRoleMemberAttrData.getKimTypeId().equals(oldRoleMemberAttrData.getKimTypeId())
                        && newRoleMemberAttrData.getKimAttributeId().equals(
                                oldRoleMemberAttrData.getKimAttributeId())) {
                    newRoleMemberAttrData.setAssignedToId(oldRoleMemberAttrData.getAssignedToId());
                    newRoleMemberAttrData.setVersionNumber(oldRoleMemberAttrData.getVersionNumber());
                    newRoleMemberAttrData.setId(oldRoleMemberAttrData.getId());
                    updateAttrBos.add(newRoleMemberAttrData);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                updateAttrBos.add(newRoleMemberAttrData);
            } else {
                matched = false;
            }
        }

        roleMember.setAttributeDetails(updateAttrBos);

        return getResponsibilityInternalService().saveRoleMember(roleMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public DelegateMember updateDelegateMember(final DelegateMember delegateMember) throws IllegalArgumentException,
            IllegalStateException {
        //check delegateMember not empty
        incomingParamCheck(delegateMember, "delegateMember");

        //check delegate exists
        final String delegationId = delegateMember.getDelegationId();
        incomingParamCheck(delegationId, "delegationId");
        final DelegateType delegate = getKimDelegationImpl(delegationId);
        DelegateMember originalDelegateMember = null;
        final String delegationMemberId = delegateMember.getDelegationMemberId();
        if (StringUtils.isNotEmpty(delegationMemberId)) {
            originalDelegateMember = getDelegateMember(delegateMember.getDelegationMemberId());
        }
        if (delegate == null) {
            throw new IllegalStateException("the delegate does not exist: " + delegationId);
        }

        //save the delegateMember  (actually updates)
        final String kimTypeId = getRoleLite(delegate.getRoleId()).getKimTypeId();
        final List<DelegateMemberAttributeData> attrBos = KimAttributeData.createFrom(
                DelegateMemberAttributeData.class, delegateMember.getAttributes(), kimTypeId);

        final List<DelegateMemberAttributeData> updateAttrBos = new ArrayList<>();

        boolean matched = false;
        if (originalDelegateMember != null) {
            delegateMember.setVersionNumber(originalDelegateMember.getVersionNumber());
            for (final DelegateMemberAttributeData newDelegateMemberAttrData : attrBos) {
                for (final DelegateMemberAttributeData oldDelegateMemberAttrData :
                        originalDelegateMember.getAttributeDetails()) {
                    if (newDelegateMemberAttrData.getKimTypeId().equals(oldDelegateMemberAttrData .getKimTypeId())
                            && newDelegateMemberAttrData.getKimAttributeId().equals(
                                    oldDelegateMemberAttrData .getKimAttributeId())) {
                        newDelegateMemberAttrData.setAssignedToId(oldDelegateMemberAttrData .getAssignedToId());
                        newDelegateMemberAttrData.setVersionNumber(oldDelegateMemberAttrData .getVersionNumber());
                        newDelegateMemberAttrData.setId(oldDelegateMemberAttrData .getId());
                        updateAttrBos.add(newDelegateMemberAttrData);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    updateAttrBos.add(newDelegateMemberAttrData);
                } else {
                    matched = false;
                }
            }
        }

        delegateMember.setAttributeDetails(updateAttrBos);
        return getResponsibilityInternalService().saveDelegateMember(delegateMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public DelegateMember createDelegateMember(final DelegateMember delegateMember) throws IllegalArgumentException,
            IllegalStateException {
        //ensure object not empty
        incomingParamCheck(delegateMember, "delegateMember");

        //check key is null
        if (delegateMember.getDelegationMemberId() != null) {
            throw new IllegalStateException("the delegate member already exists: " +
                    delegateMember.getDelegationMemberId());
        }

        //check delegate exists
        final String delegationId = delegateMember.getDelegationId();
        incomingParamCheck(delegationId, "delegationId");
        final DelegateType delegate = getKimDelegationImpl(delegationId);
        if (delegate == null) {
            throw new IllegalStateException("the delegate does not exist: " + delegationId);
        }

        //check member exists
        checkMemberExists(delegateMember);

        //create member delegate
        final String kimTypeId = getRoleLite(delegate.getRoleId()).getKimTypeId();
        final List<DelegateMemberAttributeData> attrBos = KimAttributeData.createFrom(
                DelegateMemberAttributeData.class, delegateMember.getAttributes(), kimTypeId);
        delegateMember.setAttributeDetails(attrBos);
        return getResponsibilityInternalService().saveDelegateMember(delegateMember);
    }

    private void checkMemberExists(final DelegateMember delegateMember) {
        final String memberId = delegateMember.getMemberId();
        incomingParamCheck(memberId, "memberId");
        final MemberType memberType = delegateMember.getType();
        switch (memberType) {
            case ROLE:
                final RoleLite roleWithoutMembers = getRoleWithoutMembers(memberId);
                if (roleWithoutMembers == null) {
                    throw new IllegalStateException("the role does not exist: " + memberId);
                }
                break;
            case GROUP:
                final Group group = getGroupService().getGroup(memberId);
                if (group == null) {
                    throw new IllegalStateException("the group does not exist: " + memberId);
                }
                break;
            case PRINCIPAL:
                final Person person = personService.getPerson(memberId);
                if (person == null) {
                    throw new IllegalStateException("the user does not exist: " + memberId);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected memberType: " + memberType + " for memberId: " + memberId);
        }
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void removeDelegateMembers(final List<DelegateMember> delegateMembers) throws IllegalArgumentException,
            IllegalStateException {
        incomingParamCheck(delegateMembers, "delegateMembers");
        delegateMembers.forEach(this::updateDelegateMember);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleResponsibilityAction createRoleResponsibilityAction(
            final RoleResponsibilityAction roleResponsibilityAction) throws IllegalArgumentException,
            IllegalStateException {
        incomingParamCheck(roleResponsibilityAction, "roleResponsibilityAction");

        if (StringUtils.isNotBlank(roleResponsibilityAction.getId())
                && getRoleResponsibilityAction(roleResponsibilityAction.getId()) != null) {
            throw new IllegalStateException("the roleResponsibilityAction to create already exists: " +
                    roleResponsibilityAction);
        }

        return getBusinessObjectService().save(roleResponsibilityAction);
    }

    /**
     * Queues ActionRequest refresh/regeneration for RoleResponsibilityAction change
     *
     * @param bo the changed or deleted RoleResponsibilityAction
     */
    protected void updateActionRequestsForRoleResponsibilityActionChange(final RoleResponsibilityAction bo) {
        final RoleResponsibility rr = bo.getRoleResponsibility();
        if (rr != null) {
            getResponsibilityInternalService().updateActionRequestsForResponsibilityChange(Collections.singleton(
                    rr.getResponsibilityId()));
        }
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public RoleResponsibilityAction updateRoleResponsibilityAction(RoleResponsibilityAction roleResponsibilityAction)
            throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(roleResponsibilityAction, "roleResponsibilityAction");

        if (StringUtils.isBlank(roleResponsibilityAction.getId())
                || getRoleResponsibilityAction(roleResponsibilityAction.getId()) == null) {
            throw new IllegalStateException("the roleResponsibilityAction to create does not exist: " +
                    roleResponsibilityAction);
        }

        roleResponsibilityAction = getBusinessObjectService().save(roleResponsibilityAction);

        // update action requests
        updateActionRequestsForRoleResponsibilityActionChange(roleResponsibilityAction);

        return roleResponsibilityAction;
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public DelegateType createDelegateType(final DelegateType delegateType) throws IllegalArgumentException,
            IllegalStateException {
        incomingParamCheck(delegateType, "delegateType");

        if (StringUtils.isNotBlank(delegateType.getDelegationId())
                && getDelegateTypeByDelegationId(delegateType.getDelegationId()) != null) {
            throw new IllegalStateException("the delegateType to create already exists: " + delegateType);
        }

        return getBusinessObjectService().save(delegateType);
    }

    private void removeRoleMembers(final List<RoleMember> members) {
        if (CollectionUtils.isNotEmpty(members)) {
            for (final RoleMember rm : members) {
                getResponsibilityInternalService().removeRoleMember(rm);
            }
        }
    }

    private List<RoleMember> getRoleMembersByDefaultStrategy(
            final String roleId, final String memberId, final String memberTypeCode,
            final Map<String, String> qualifier) {
        final Map<String, String> convertedQualifier = convertQualifierKeys(qualifier, roleId);
        final List<RoleMember> rms = new ArrayList<>();
        final List<RoleMember> roleMem = getRoleDao().getRoleMembershipsForMemberId(memberTypeCode, memberId,
                convertedQualifier);
        for (final RoleMember rm : roleMem) {
            if (rm.getRoleId().equals(roleId)) {
                // if found, remove
                rms.add(rm);
            }
        }
        return rms;
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void removePrincipalFromRole(
            final String principalId, final String namespaceCode, final String roleName,
            final Map<String, String> qualifier) throws IllegalArgumentException {
        if (StringUtils.isBlank(principalId)) {
            throw new IllegalArgumentException("principalId is null");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new IllegalArgumentException("namespaceCode is null");
        }

        if (StringUtils.isBlank(roleName)) {
            throw new IllegalArgumentException("roleName is null");
        }

        if (qualifier == null) {
            throw new IllegalArgumentException("qualifier is null");
        }
        // look up the role
        final RoleLite role = getRoleLiteByName(namespaceCode, roleName);
        // pull all the principal members
        // look for an exact qualifier match
        List<RoleMember> rms = getRoleMembersByExactQualifierMatch(role, principalId,
                memberTypeToRoleDaoActionMap.get(MemberType.PRINCIPAL.getCode()), qualifier);
        if (CollectionUtils.isEmpty(rms)) {
            rms = getRoleMembersByDefaultStrategy(role.getId(), principalId, MemberType.PRINCIPAL.getCode(), qualifier);
        }
        removeRoleMembers(rms);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void removeRoleFromRole(
            final String roleId, final String namespaceCode, final String roleName,
            final Map<String, String> qualifier) throws IllegalArgumentException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        final RoleLite role = getRoleLiteByName(namespaceCode, roleName);
        // pull all the group role members
        // look for an exact qualifier match
        List<RoleMember> rms = getRoleMembersByExactQualifierMatch(role, roleId,
                memberTypeToRoleDaoActionMap.get(MemberType.ROLE.getCode()), qualifier);
        if (CollectionUtils.isEmpty(rms)) {
            rms = getRoleMembersByDefaultStrategy(role.getId(), roleId, MemberType.ROLE.getCode(), qualifier);
        }
        removeRoleMembers(rms);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void assignPermissionToRole(final String permissionId, final String roleId) throws IllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");
        incomingParamCheck(roleId, "roleId");

        final RolePermission newRolePermission = new RolePermission();

        final Long nextSeq = KRADServiceLocator.getSequenceAccessorService().getNextAvailableSequenceNumber(
                KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S, RolePermission.class);

        if (nextSeq == null) {
            LOG.error(
                    "Unable to get new role permission id from sequence {}",
                    KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S
            );
            throw new RuntimeException("Unable to get new role permission id from sequence " +
                    KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S);
        }

        newRolePermission.setId(nextSeq.toString());
        newRolePermission.setRoleId(roleId);
        newRolePermission.setPermissionId(permissionId);
        newRolePermission.setActive(true);

        getBusinessObjectService().save(newRolePermission);
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                Permission.CACHE_NAME,
                Responsibility.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void revokePermissionFromRole(final String permissionId, final String roleId) throws IllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");
        incomingParamCheck(roleId, "roleId");

        final Map<String, Object> params = new HashMap<>();
        params.put("roleId", roleId);
        params.put("permissionId", permissionId);
        params.put("active", Boolean.TRUE);
        final Collection<RolePermission> rolePermissions = getBusinessObjectService()
                .findMatching(RolePermission.class, params);
        final List<RolePermission> rolePermsToSave = new ArrayList<>();
        for (final RolePermission rolePerm : rolePermissions) {
            rolePerm.setActive(false);
            rolePermsToSave.add(rolePerm);
        }

        getBusinessObjectService().save(rolePermsToSave);
    }

    protected void addMemberAttributeData(final RoleMember roleMember, final Map<String, String> qualifier, final String kimTypeId) {
        final List<RoleMemberAttributeData> attributes = new ArrayList<>();
        for (final Map.Entry<String, String> entry : qualifier.entrySet()) {
            final RoleMemberAttributeData roleMemberAttrBo = new RoleMemberAttributeData();
            roleMemberAttrBo.setAttributeValue(entry.getValue());
            roleMemberAttrBo.setKimTypeId(kimTypeId);
            roleMemberAttrBo.setAssignedToId(roleMember.getId());
            // look up the attribute ID
            // CU Customization: Use the custom getKimAttributeId variant that also uses a kimTypeId.
            roleMemberAttrBo.setKimAttributeId(getKimAttributeId(kimTypeId, entry.getKey()));

            final Map<String, String> criteria = new HashMap<>();
            criteria.put(KimConstants.PrimaryKeyConstants.KIM_ATTRIBUTE_ID, roleMemberAttrBo.getKimAttributeId());
            //criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMember.getId());
            criteria.put("assignedToId", roleMember.getId());
            final List<RoleMemberAttributeData> origRoleMemberAttributes =
                    (List<RoleMemberAttributeData>) getBusinessObjectService()
                            .findMatching(RoleMemberAttributeData.class, criteria);
            final RoleMemberAttributeData origRoleMemberAttribute =
                    origRoleMemberAttributes != null && !origRoleMemberAttributes.isEmpty()
                            ? origRoleMemberAttributes.get(0) : null;
            if (origRoleMemberAttribute != null) {
                roleMemberAttrBo.setId(origRoleMemberAttribute.getId());
                roleMemberAttrBo.setVersionNumber(origRoleMemberAttribute.getVersionNumber());
            }
            attributes.add(roleMemberAttrBo);
        }
        roleMember.setAttributeDetails(attributes);
    }

    protected void logPrincipalHasRoleCheck(
            final String principalId, final List<String> roleIds,
            final Map<String, String> roleQualifiers) {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("Has Role     : ").append(roleIds).append('\n');
        if (roleIds != null) {
            for (final String roleId : roleIds) {
                final RoleLite role = getRoleWithoutMembers(roleId);
                if (role != null) {
                    sb.append("        Name : ").append(role.getNamespaceCode()).append('/').append(role.getName());
                    sb.append(" (").append(roleId).append(')');
                    sb.append('\n');
                }
            }
        }
        sb.append("   Principal : ").append(principalId);
        if (principalId != null) {
            final Person person = personService.getPerson(principalId);
            if (person != null) {
                sb.append(" (").append(person.getPrincipalName()).append(')');
            }
        }
        sb.append('\n');
        sb.append("     Details :\n");
        if (roleQualifiers != null) {
            sb.append(roleQualifiers);
        } else {
            sb.append("               [null]\n");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(sb.append(ExceptionUtils.getStackTrace(new Throwable())));
        } else {
            LOG.debug(sb);
        }
    }

    private void incomingParamCheck(final Object object, final String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " was null");
        } else if (object instanceof String
                && StringUtils.isBlank((String) object)) {
            throw new IllegalArgumentException(name + " was blank");
        }
    }

    /**
     * This gets the proxied version of the role service which will go through Spring's caching mechanism for method
     * calls rather than skipping it when methods are called directly.
     *
     * @return The proxied role service
     */
    protected RoleService getProxiedRoleService() {
        if (proxiedRoleService == null) {
            proxiedRoleService = KimApiServiceLocator.getRoleService();
        }
        return proxiedRoleService;
    }

    /**
     * Sets the cache manager which this service implementation can for internal caching.
     * Calling this setter is optional, though the value passed to it must not be null.
     *
     * @param cacheManager the cache manager to use for internal caching, must not be null
     * @throws IllegalArgumentException if a null cache manager is passed
     */
    public void setCacheManager(final CacheManager cacheManager) {
        if (cacheManager == null) {
            throw new IllegalArgumentException("cacheManager must not be null");
        }
        this.cacheManager = cacheManager;
    }

    public void setKimTypeInfoService(final KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
        //CU Customization: Ensure service is set on abstract super class due to it being an attribute of both.
        super.setKimTypeInfoService(kimTypeInfoService);
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * An internal helper class which is used to keep context for an invocation of principalHasRole.
     */
    private final class Context {

        private final String principalId;
        private List<String> principalGroupIds;
        private final Map<String, RoleTypeService> roleTypeServiceCache;
        private final Map<String, Boolean> isDerivedRoleTypeCache;

        Context(final String principalId) {
            this.principalId = principalId;
            roleTypeServiceCache = new HashMap<>();
            isDerivedRoleTypeCache = new HashMap<>();
        }

        List<String> getPrincipalGroupIds() {
            if (principalGroupIds == null) {
                principalGroupIds = getGroupService().getGroupIdsByPrincipalId(principalId);
            }
            return principalGroupIds;
        }

        RoleTypeService getRoleTypeService(final String kimTypeId) {
            if (roleTypeServiceCache.containsKey(kimTypeId)) {
                return roleTypeServiceCache.get(kimTypeId);
            }
            RoleTypeService roleTypeService = null;
            if (kimTypeId != null) {
                final KimType roleType = kimTypeInfoService.getKimType(kimTypeId);
                if (roleType != null && StringUtils.isNotBlank(roleType.getServiceName())) {
                    roleTypeService = getRoleTypeServiceByName(roleType.getServiceName());
                }
            }
            if (roleTypeService == null) {
                roleTypeService = KimImplServiceLocator.getDefaultRoleTypeService();
            }
            roleTypeServiceCache.put(kimTypeId, roleTypeService);
            return roleTypeService;
        }

        boolean isDerivedRoleType(final String kimTypeId) {
            Boolean isDerived = isDerivedRoleTypeCache.get(kimTypeId);
            if (isDerived == null) {
                isDerived = RoleServiceImpl.this.isDerivedRoleType(getRoleTypeService(kimTypeId));
                isDerivedRoleTypeCache.put(kimTypeId, isDerived);
            }
            return isDerived;
        }
    }
}
