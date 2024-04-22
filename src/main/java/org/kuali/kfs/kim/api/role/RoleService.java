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
package org.kuali.kfs.kim.api.role;

import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service provides operations for querying role and role qualification data.
 *
 * <p>A role is where permissions and responsibilities are granted. Roles have a membership consisting of principals,
 * groups or even other roles. By being assigned as members of a role, the associated principals will be granted all
 * permissions and responsibilities that have been granted to the role.
 *
 * <p>Each membership assignment on the role can have a qualification which defines extra information about that
 * particular member of the role. For example, one may have the role of "Dean" but that can be further qualified
 * by the school they are the dean of, such as "Dean of Computer Science". Authorization checks that are then done in
 * the permission service can pass qualifiers as part of the operation if they want to restrict the subset of the role
 * against which the check is made.
 */
/*
 * CU Customization: Backported the FINP-9235 changes.
 * This overlay can be removed when we upgrade to the 2023-03-08 financials patch.
 */
public interface RoleService {

    /**
     * This will create a {@link RoleLite} exactly like the role passed in.
     *
     * @param role the role to create
     * @return the newly created object.  will never be null.
     * @throws IllegalArgumentException if the role passed in is null
     * @throws IllegalStateException    if the role is already existing in the system
     */
    RoleLite createRole(RoleLite role) throws IllegalArgumentException, IllegalStateException;

    /**
     * This will update a {@link RoleLite}.
     *
     * @param role the role to update
     * @throws IllegalArgumentException if the role is null
     * @throws IllegalStateException    if the role does not exist in the system
     */
    RoleLite updateRole(RoleLite role) throws IllegalArgumentException, IllegalStateException;

    /**
     * Get the KIM Role object with the given ID.
     *
     * @param id the id of the role.
     * @return the role with the given id or null if role doesn't exist.
     * @throws IllegalArgumentException if roleId is null or Blank
     */
    RoleLite getRoleWithoutMembers(String id) throws IllegalArgumentException;

    /**
     * Get the KIM Role objects for the role IDs in the given List.
     *
     * @param ids the ids of the roles.
     * @return a list of roles with the given ids or null if no roles are found.
     * @throws IllegalArgumentException if ids is null or Blank
     */
    List<RoleLite> getRoles(List<String> ids) throws IllegalArgumentException;

    /**
     * Get the KIM Role object with the unique combination of namespace, component, and role name.
     *
     * @param namespaceCode the namespace code of the role.
     * @param name          the name of the role.
     * @return a role with the given namespace code and name or null if role does not exist.
     * @throws IllegalArgumentException if namespaceCode or name is null or blank.
     */
    RoleLite getRoleByNamespaceCodeAndName(String namespaceCode, String name) throws IllegalArgumentException;

    /**
     * Get the KIM Role object with the unique combination of namespace, component, and role name.
     *
     * @param namespaceCode the namespace code of the role.
     * @param name          the name of the role.
     * @return a role with the given namespace code and name or null if role does not exist.
     * @throws IllegalArgumentException if namespaceCode or name is null or blank.
     */
    List<RoleLite> getAllRolesByNamespaceCodeAndName(String namespaceCode, String name) throws IllegalArgumentException;

    /**
     * Return the Role ID for the given unique combination of namespace, component and role name.
     *
     * @param namespaceCode the namespace code of the role.
     * @param name          the name of the role.
     * @return a role id for a role with the given namespace code and name or null if role does not exist.
     * @throws IllegalArgumentException if namespaceCode or name is null or blank.
     */
    String getRoleIdByNamespaceCodeAndName(String namespaceCode, String name) throws IllegalArgumentException;

    /**
     * Return the Role ID for the given unique combination of namespace, component and role name.
     *
     * @param namespaceCode the namespace code of the role.
     * @param name          the name of the role.
     * @return a role id for a role with the given namespace code and name or null if role does not exist.
     * @throws IllegalArgumentException if namespaceCode or name is null or blank.
     */
    List<String> getAllRoleIdsByNamespaceCodeAndName(String namespaceCode, String name) throws IllegalArgumentException;

    /**
     * Checks whether the role with the given role ID is active.
     *
     * @param id the unique id of a role.
     * @return true if the role with the given id is active.
     * @throws IllegalArgumentException if id is null or blank.
     */
    boolean isRoleActive(String id) throws IllegalArgumentException;

    /**
     * Returns a list of role qualifiers that the given principal has without taking into consideration that the
     * principal may be a member via an assigned group or role. Use in situations where you are only interested in the
     * qualifiers that are directly assigned to the principal.
     *
     * @param principalId   the principalId to
     * @param namespaceCode the namespace code of the role.
     * @param roleName      the name of the role.
     * @param qualification the qualifications for the roleIds.
     * @return a map of role qualifiers for the given parameters or an empty map if none found.
     * @throws IllegalArgumentException if principalId, namespaceCode, or roleName is null or blank.
     */
    List<Map<String, String>> getRoleQualifersForPrincipalByNamespaceAndRolename(String principalId,
            String namespaceCode, String roleName, Map<String, String> qualification) throws IllegalArgumentException;

    /**
     * Returns a list of role qualifiers that the given principal. If the principal's membership is via a group or
     * role, that group or role's qualifier on the given role is returned.
     *
     * @param principalId   the principalId to
     * @param roleIds       the namespace code of the role.
     * @param qualification the qualifications for the roleIds.
     * @return a map of role qualifiers for the given roleIds and qualifications or an empty map if none found.
     * @throws IllegalArgumentException if principalId, namespaceCode, or roleName is null or blank.
     */
    List<Map<String, String>> getNestedRoleQualifiersForPrincipalByRoleIds(String principalId, List<String> roleIds,
            Map<String, String> qualification) throws IllegalArgumentException;

    /**
     * Get all the role members (groups and principals) associated with the given list of roles where their role
     * membership/assignment matches the given qualification. The list of RoleMemberships returned will only contain
     * group and principal members. Any nested role members will be resolved and flattened into the principals and
     * groups that are members of that nested role (assuming qualifications match).
     * <p>
     * The return object will have each membership relationship along with the delegations
     *
     * @param roleIds       a list of role Ids.
     * @param qualification the qualifications for the roleIds.
     * @return a list of role members for the given roleIds and qualifications or an empty list if none found.
     * @throws IllegalArgumentException if roleIds is null.
     */
    List<RoleMembership> getRoleMembers(List<String> roleIds, Map<String, String> qualification)
            throws IllegalArgumentException;

    /**
     * This method gets all the members, then traverses down into members of type role and group to obtain the nested
     * principal ids
     *
     * @param namespaceCode the namespace code of the role.
     * @param roleName      the name of the role
     * @param qualification the qualifications for the roleIds.
     * @return a list of role member principalIds for the given roleIds and qualifications, or an empty list if none
     *         found.
     * @throws IllegalArgumentException if namespaceCode, or roleName is null or blank.
     */
    Collection<String> getRoleMemberPrincipalIds(String namespaceCode, String roleName,
            Map<String, String> qualification) throws IllegalArgumentException;

    /**
     * This method gets all the members, then traverses down into members of type role and group to obtain the nested
     * principal ids
     *
     * @param namespaceCode the namespace code of the role.
     * @param roleName      the name of the role
     * @param qualification the qualifications for the roleIds.
     * @return a list of role member principalIds for the given roleIds and qualifications, or an empty list if none
     *         found.
     */
    Collection<String> getRoleMemberPrincipalIdsAllowNull(String namespaceCode, String roleName,
            Map<String, String> qualification);

    /**
     * Returns whether the given principal has any of the passed role IDs with the given qualification.
     *
     * @param principalId   the principal Id to check.
     * @param roleIds       the list of role ids.
     * @param qualification the qualifications for the roleIds.
     * @return true if the principal is assigned the one of the given roleIds with the passed in qualifications.
     * @throws IllegalArgumentException if roleIds is null or principalId is null or blank.
     */
    boolean principalHasRole(String principalId, List<String> roleIds, Map<String, String> qualification)
            throws IllegalArgumentException;

    /**
     * Returns whether the given principal has any of the passed role IDs with the given qualification.
     *
     * @param principalId      the principal Id to check.
     * @param roleIds          the list of role ids.
     * @param qualification    the qualifications for the roleIds.
     * @param checkDelegations whether delegations should be checked or not
     * @return true if the principal is assigned the one of the given roleIds with the passed in qualifications.
     * @throws IllegalArgumentException if roleIds is null or principalId is null or blank.
     * @since 2.1.1
     */
    boolean principalHasRole(String principalId, List<String> roleIds, Map<String, String> qualification,
            boolean checkDelegations) throws IllegalArgumentException;

    /**
     * Returns the subset of the given principal ID list which has the given role and qualification. This is designed
     * to be used by lookups of people by their roles.
     *
     * @param principalIds      the principal Ids to check.
     * @param roleNamespaceCode the namespaceCode of the role.
     * @param roleName          the name of the role.
     * @param qualification     the qualifications for the roleIds.
     * @return list of principalIds that is the subset of list passed in with the given role and qualifications or an
     * empty list.
     * @throws IllegalArgumentException if principalIds is null or the roleNamespaceCode or roleName is null or
     *                                      blank.
     */
    List<String> getPrincipalIdSubListWithRole(List<String> principalIds, String roleNamespaceCode, String roleName,
            Map<String, String> qualification) throws IllegalArgumentException;

    /**
     * Returns the subset of the given principal ID list which has the given role and qualification. This is designed
     * to be used by lookups of people by their roles.
     *
     * @param principalIds      the principal Ids to check.
     * @param roleNamespaceCode the namespaceCode of the role.
     * @param roleName          the name of the role.
     * @param qualification     the qualifications for the roleIds.
     * @return list of principalIds that is the subset of list passed in with the given role and qualifications or an
     * empty list.
     * @throws IllegalArgumentException if principalIds is null.
     */
    List<String> getPrincipalIdSubListWithRoleAllowNull(
            List<String> principalIds, String roleNamespaceCode, String roleName, Map<String, String> qualification
    ) throws IllegalArgumentException;

    /**
     * This method gets search results for role lookup
     *
     * @param queryByCriteria the qualifications for the roleIds.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if queryByCriteria is null.
     */
    GenericQueryResults<RoleLite> findRoles(QueryByCriteria queryByCriteria) throws IllegalArgumentException;

    /**
     * Gets all direct members of the roles that have ids within the given list of role ids. This method does not
     * recurse into any nested roles.
     *
     * <p>The resulting List of role membership will contain membership for all the roles with the specified ids. The
     * list is not guaranteed to be in any particular order and may have membership info for the different roles
     * interleaved with each other.
     *
     * @param roleIds a list of  role Ids.
     * @return list of RoleMembership that contains membership for the specified roleIds or empty list if none found.
     * @throws IllegalArgumentException if roleIds is null.
     */
    List<RoleMembership> getFirstLevelRoleMembers(List<String> roleIds) throws IllegalArgumentException;

    /**
     * Gets a list of Roles that the given member belongs to.
     *
     * @param memberType the role member type.
     * @param memberId   the role member id (principalId, roleId, groupId).
     * @return list of RoleMembership that contains membership for the specified roleIds or an empty list if none
     * found.
     * @throws IllegalArgumentException if memberType or memberId is null or blank.
     */
    List<String> getMemberParentRoleIds(String memberType, String memberId) throws IllegalArgumentException;

    /**
     * Gets role members based on the given search criteria.
     *
     * @param queryByCriteria the qualifications for the roleIds.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if queryByCriteria is null.
     */
    GenericQueryResults<RoleMember> findRoleMembers(QueryByCriteria queryByCriteria) throws IllegalArgumentException;

    /**
     * Gets a list of Roles Ids that are a member of the given roleId, including nested membership.
     *
     * @param roleId the role id.
     * @return list of RoleIds that are members of the given role or and empty list if none found.
     * @throws IllegalArgumentException if roleId is null or blank.
     */
    Set<String> getRoleTypeRoleMemberIds(String roleId) throws IllegalArgumentException;

    /**
     * Gets role members based on the given search criteria.
     *
     * @param queryByCriteria the qualifications for the roleIds.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if queryByCriteria is null.
     */
    GenericQueryResults<DelegateMember> findDelegateMembers(QueryByCriteria queryByCriteria)
            throws IllegalArgumentException;

    /**
     * Gets the delegate member with the given delegation member id.
     *
     * @param id the member id matching the DelegateMember
     * @return the delegate member with the given parameters or null if not found.
     * @throws IllegalArgumentException if delegationId or memberId is null or blank.
     */
    DelegateMember getDelegationMemberById(String id) throws IllegalArgumentException;

    /**
     * Gets a list of role reponsibilities for the given role id.
     *
     * @param roleId the role Id.
     * @return a list of RoleResponsibilities for the given role Id, or an empty list if none found.
     * @throws IllegalArgumentException if roleId is null or blank.
     */
    List<RoleResponsibility> getRoleResponsibilities(String roleId) throws IllegalArgumentException;

    /**
     * Gets a list of RoleResponsibilityActions for the given role member id.
     *
     * @param roleMemberId the role member Id.
     * @return a list of RoleResponsibilityActions for the given role member Id, or an empty list if none found.
     * @throws IllegalArgumentException if roleMemberId is null or blank.
     */
    List<RoleResponsibilityAction> getRoleMemberResponsibilityActions(String roleMemberId)
            throws IllegalArgumentException;

    /**
     * Gets a DelegateType for the given role id and delegation type.
     *
     * @param roleId       the role Id.
     * @param delegateType type of delegation
     * @return the DelegateType for the given role Id and delegationType, or null if none found.
     * @throws IllegalArgumentException if roleId or delegationType is null or blank.
     */
    DelegateType getDelegateTypeByRoleIdAndDelegateTypeCode(String roleId, DelegationType delegateType)
            throws IllegalArgumentException;

    /**
     * Gets a DelegateType for the given delegation id.
     *
     * @param delegationId the id of delegation
     * @return the DelegateType for the given delegation Id, or null if none found.
     * @throws IllegalArgumentException if delegationId is null or blank.
     */
    DelegateType getDelegateTypeByDelegationId(String delegationId) throws IllegalArgumentException;

    /**
     * Assigns the principal with the given id to the role with the specified
     * namespace code and name with the supplied set of qualifications.
     *
     * @param principalId    the principalId
     * @param namespaceCode  the namespaceCode of the Role
     * @param roleName       the name of the role
     * @param qualifications the qualifications for the principalId to be assigned to the role
     * @return newly created/assigned RoleMember.
     * @throws IllegalArgumentException if princialId, namespaceCode or roleName is null or blank.
     */
    RoleMember assignPrincipalToRole(String principalId, String namespaceCode, String roleName,
                                     Map<String, String> qualifications) throws IllegalArgumentException;

    /**
     * Assigns the group with the given id to the role with the specified namespace code and name with the supplied
     * set of qualifications.
     *
     * @param groupId        the groupId
     * @param namespaceCode  the namespaceCode of the Role
     * @param roleName       the name of the role
     * @param qualifications the qualifications for the principalId to be assigned to the role
     * @return newly created/assigned RoleMember.
     * @throws IllegalArgumentException if groupId, namespaceCode or roleName is null or blank.
     */
    RoleMember assignGroupToRole(String groupId, String namespaceCode, String roleName,
                                 Map<String, String> qualifications) throws IllegalArgumentException;

    /**
     * Assigns the role with the given id to the role with the specified namespace code and name with the supplied set
     * of qualifications.
     *
     * @param roleId         the roleId
     * @param namespaceCode  the namespaceCode of the Role
     * @param roleName       the name of the role
     * @param qualifications the qualifications for the principalId to be assigned to the role
     * @return newly created/assigned RoleMember.
     * @throws IllegalArgumentException if princiapId, namespaceCode or roleName is null or blank.
     */
    RoleMember assignRoleToRole(String roleId, String namespaceCode, String roleName,
                                Map<String, String> qualifications) throws IllegalArgumentException;

    /**
     * Creates a new RoleMember. Needs to be passed a valid RoleMember object that does not currently exist.
     *
     * @param roleMember the new RoleMember to save.
     * @return RoleMember as created.
     * @throws IllegalArgumentException if roleMember is null.
     * @throws IllegalStateException    if roleMember already exists.
     */
    RoleMember createRoleMember(RoleMember roleMember) throws IllegalArgumentException, IllegalStateException;

    /**
     * Updates the given roleMember to the values in the passed in roleMember
     *
     * @param roleMember the new RoleMember to save.
     * @return RoleMember as updated.
     * @throws IllegalArgumentException if roleMember is null.
     * @throws IllegalStateException    if roleMember does not yet exist.
     */
    RoleMember updateRoleMember(RoleMember roleMember) throws IllegalArgumentException, IllegalStateException;

    /**
     * Updates the given delegateMember to the values in the passed in delegateMember
     *
     * @param delegateMember the new DelegateMember to save.
     * @return DelegateMember as updated.
     * @throws IllegalArgumentException if delegateMember is null.
     * @throws IllegalStateException    if delegateMember does not yet exist.
     */
    DelegateMember updateDelegateMember(DelegateMember delegateMember) throws IllegalArgumentException,
            IllegalStateException;

    /**
     * Creates a new DelegateMember. Needs to be passed a valid DelegateMember object that does not currently exist.
     *
     * @param delegateMember the new DelegateMember to save.
     * @return DelegateMember as created.
     * @throws IllegalArgumentException if delegateMember is null.
     * @throws IllegalStateException    if delegateMember already exists.
     */
    DelegateMember createDelegateMember(DelegateMember delegateMember) throws IllegalArgumentException,
            IllegalStateException;

    /**
     * Removes existing DelegateMembers. Needs to be passed DelegateMember objects.
     *
     * @param delegateMembers to remove.
     * @throws IllegalArgumentException if delegateMember is null.
     */
    void removeDelegateMembers(List<DelegateMember> delegateMembers) throws IllegalArgumentException,
            IllegalStateException;

    /**
     * Creates a new RoleResponsibilityAction. Needs to be passed a valid RoleResponsibilityAction object that does
     * not currently exist.
     *
     * @param roleResponsibilityAction the new RoleResponsibilityAction to save.
     * @return RoleResponsibilityAction as created.
     * @throws IllegalArgumentException if roleResponsibilityAction is null.
     * @throws IllegalStateException    if roleResponsibilityAction already exists.
     */
    RoleResponsibilityAction createRoleResponsibilityAction(RoleResponsibilityAction roleResponsibilityAction)
            throws IllegalArgumentException;

    /**
     * Updates the given RoleResponsibilityAction to the values in the passed in roleResponsibilityAction
     *
     * @param roleResponsibilityAction the new RoleResponsibilityAction to save.
     * @return RoleResponsibilityAction as updated.
     * @throws IllegalArgumentException if roleResponsibilityAction is null.
     * @throws IllegalStateException    if roleResponsibilityAction does not exist.
     */
    RoleResponsibilityAction updateRoleResponsibilityAction(RoleResponsibilityAction roleResponsibilityAction)
            throws IllegalArgumentException;

    /**
     * Creates a new DelegateType.  Needs to be passed a valid DelegateType object that does not currently exist.
     *
     * @param delegateType the new DelegateType to save.
     * @return DelegateType as created.
     * @throws IllegalArgumentException if delegateType is null.
     * @throws IllegalStateException    if delegateType already exists.
     */
    DelegateType createDelegateType(DelegateType delegateType) throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove the principal with the given id and qualifications from the role with the specified namespace code and
     * role name.
     *
     * @param principalId    the principalId
     * @param namespaceCode  the namespaceCode of the Role
     * @param roleName       the name of the role
     * @param qualifications the qualifications for the principalId to be assigned to the role
     * @throws IllegalArgumentException if principalId, namespaceCode or roleName is null or blank.
     */
    void removePrincipalFromRole(String principalId, String namespaceCode, String roleName,
            Map<String, String> qualifications) throws IllegalArgumentException;

    /**
     * Remove the group with the given id and qualifications from the role with the specified namespace code and role
     * name.
     *
     * @param roleId         the roleId
     * @param namespaceCode  the namespaceCode of the Role
     * @param roleName       the name of the role
     * @param qualifications the qualifications for the principalId to be assigned to the role
     * @throws IllegalArgumentException if roleId, namespaceCode or roleName is null or blank.
     */
    void removeRoleFromRole(String roleId, String namespaceCode, String roleName, Map<String, String> qualifications)
            throws IllegalArgumentException;

    /**
     * Assigns the given permission to the given role
     *
     * @param permissionId the permissionId
     * @param roleId       the roleId
     * @throws IllegalArgumentException if permissionId or roleId is null or blank.
     */
    void assignPermissionToRole(String permissionId, String roleId) throws IllegalArgumentException;

    /**
     * Removes the given permission to the given role
     *
     * @param permissionId the permissionId
     * @param roleId       the roleId
     * @throws IllegalArgumentException if permissionId or roleId is null or blank.
     */
    void revokePermissionFromRole(String permissionId, String roleId) throws IllegalArgumentException;

    /**
     * Determines if a role with a provided id is a uses dynamic role memberships
     *
     * @param roleId the roleId
     * @return true if role uses dynamic memberships
     * @throws IllegalArgumentException if roleId is null or blank.
     * @since 2.1.1
     */
    boolean isDynamicRoleMembership(String roleId) throws IllegalArgumentException;
}
