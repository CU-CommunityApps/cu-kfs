/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.kim.impl.group;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.LookupCustomizer;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.attribute.AttributeTransform;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* Cornell Customization: backport redis */
public class GroupServiceImpl implements GroupService {

    private static final Logger LOG = LogManager.getLogger();

    protected BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;

    @Cacheable(cacheNames = Group.CACHE_NAME, key = "'{getGroup}|id=' + #p0")
    @Override
    public Group getGroup(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");
        return getGroupUncached(groupId);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'principalId=' + #p0")
    @Override
    public List<Group> getGroupsByPrincipalId(String principalId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        return getGroupsByPrincipalIdAndNamespaceCodeInternal(principalId, null);
    }

    protected List<Group> getGroupsByPrincipalIdAndNamespaceCodeInternal(String principalId, String namespaceCode)
            throws IllegalArgumentException {
        Collection<Group> directGroups = getDirectGroupsForPrincipal(principalId, namespaceCode);
        Set<Group> groups = new HashSet<>(directGroups);
        for (Group group : directGroups) {
            groups.add(group);
            groups.addAll(getParentGroups(group.getId()));
        }
        return List.copyOf(groups);
    }

    @Override
    public List<String> findGroupIds(final QueryByCriteria queryByCriteria) throws IllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<Group> results = this.findGroups(queryByCriteria);
        List<String> result = new ArrayList<>();

        for (Group group : results.getResults()) {
            result.add(group.getId());
        }

        return Collections.unmodifiableList(result);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME,
            key = "'{isDirectMemberOfGroup}' + 'principalId=' + #p0 + '|' + 'groupId=' + #p1")
    @Override
    public boolean isDirectMemberOfGroup(String principalId, String groupId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(groupId, "groupId");

        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.KimMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        criteria.put(KIMPropertyConstants.GroupMember.GROUP_ID, groupId);

        Collection<GroupMember> groupMembers = businessObjectService.findMatching(GroupMember.class, criteria);
        for (GroupMember gm : groupMembers) {
            if (gm.isActive(new Timestamp(System.currentTimeMillis()))) {
                return true;
            }
        }
        return false;
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getGroupIdsByPrincipalId}' + 'principalId=' + #p0")
    @Override
    public List<String> getGroupIdsByPrincipalId(String principalId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        return getGroupIdsByPrincipalIdAndNamespaceCodeInternal(principalId);
    }

    protected List<String> getGroupIdsByPrincipalIdAndNamespaceCodeInternal(String principalId)
            throws IllegalArgumentException {
        List<String> result = new ArrayList<>();

        if (principalId != null) {
            List<Group> groupList = getGroupsByPrincipalIdAndNamespaceCodeInternal(principalId, null);

            for (Group group : groupList) {
                result.add(group.getId());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getDirectGroupIdsByPrincipalId}' + 'principalId=' + #p0")
    @Override
    public List<String> getDirectGroupIdsByPrincipalId(String principalId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");

        List<String> result = new ArrayList<>();

        Collection<Group> groupList = getDirectGroupsForPrincipal(principalId);

        for (Group g : groupList) {
            result.add(g.getId());
        }

        return Collections.unmodifiableList(result);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getMemberPrincipalIds}' + 'groupId=' + #p0")
    @Override
    public List<String> getMemberPrincipalIds(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");

        return getMemberPrincipalIdsInternal(groupId, new HashSet<>());
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getDirectMemberPrincipalIds}' + 'groupId=' + #p0")
    @Override
    public List<String> getDirectMemberPrincipalIds(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");

        return this.getMemberIdsByType(getMembersOfGroup(groupId),
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME,
            key = "'{isGroupMemberOfGroup}' + 'groupMemberId=' + #p0 + '|' + 'groupId=' + #p1")
    @Override
    public boolean isGroupMemberOfGroup(String groupMemberId, String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupMemberId, "groupMemberId");
        incomingParamCheck(groupId, "groupId");

        return isMemberOfGroupInternal(groupMemberId, groupId, new HashSet<>(),
                KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME,
            key = "'{isMemberOfGroup}' + 'principalId=' + #p0 + '|' + 'groupId=' + #p1")
    @Override
    public boolean isMemberOfGroup(String principalId, String groupId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(groupId, "groupId");

        Set<String> visitedGroupIds = new HashSet<>();
        return isMemberOfGroupInternal(principalId, groupId, visitedGroupIds,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getDirectMemberGroupIds}' + 'groupId=' + #p0")
    @Override
    public List<String> getDirectMemberGroupIds(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");

        return this.getMemberIdsByType(getMembersOfGroup(groupId), KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
    }

    @Cacheable(cacheNames = GroupMember.CACHE_NAME, key = "'{getParentGroupIds}' + 'groupId=' + #p0")
    @Override
    public List<String> getParentGroupIds(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");

        List<String> result = new ArrayList<>();
        List<Group> groupList = getParentGroups(groupId);

        for (Group group : groupList) {
            result.add(group.getId());
        }

        return Collections.unmodifiableList(result);
    }

    @Cacheable(value = GroupMember.CACHE_NAME,
            key = "'groupIds=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0)")
    @Override
    public List<GroupMember> getMembers(List<String> groupIds) throws IllegalArgumentException {
        if (groupIds.isEmpty()) {
            throw new IllegalArgumentException("groupIds is empty");
        }

        //TODO: PRIME example of something for new Criteria API
        List<GroupMember> groupMembers = new ArrayList<>();
        groupIds.stream()
                .map(this::getMembersOfGroup)
                .forEach(groupMembers::addAll);
        return Collections.unmodifiableList(groupMembers);
    }

    @Cacheable(cacheNames = Group.CACHE_NAME, key = "'ids=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).key(#p0)")
    @Override
    public List<Group> getGroups(Collection<String> groupIds) throws IllegalArgumentException {
        incomingParamCheck(groupIds, "groupIds");
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        final QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
        builder.setPredicates(PredicateFactory.and(PredicateFactory.in("id", groupIds.toArray()),
                PredicateFactory.equal("active", "Y")));
        GenericQueryResults<Group> qr = findGroups(builder.build());

        return qr.getResults();
    }

    @Cacheable(cacheNames = Group.CACHE_NAME, key = "'namespaceCode=' + #p0 + '|' + 'groupName=' + #p1")
    @Override
    public Group getGroupByNamespaceCodeAndName(String namespaceCode, String groupName) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(groupName, "groupName");

        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.GROUP_NAME, groupName);
        Collection<Group> groups = businessObjectService.findMatching(Group.class, criteria);
        if (!groups.isEmpty()) {
            return groups.iterator().next();
        }
        return null;
    }

    @Override
    public GenericQueryResults<Group> findGroups(final QueryByCriteria queryByCriteria) throws IllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        LookupCustomizer.Builder<Group> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        return criteriaLookupService.lookup(Group.class, queryByCriteria, lc.build());
    }

    protected boolean isMemberOfGroupInternal(String memberId, String groupId, Set<String> visitedGroupIds,
            MemberType memberType) {
        if (memberId == null || groupId == null) {
            return false;
        }

        // when group traversal is not needed
        Group group = getGroup(groupId);
        if (group == null || !group.isActive()) {
            return false;
        }

        List<GroupMember> members = getMembersOfGroup(group.getId());
        // check the immediate group
        for (String groupMemberId : getMemberIdsByType(members, memberType)) {
            if (groupMemberId.equals(memberId)) {
                return true;
            }
        }

        // check each contained group, returning as soon as a match is found
        for (String memberGroupId : getMemberIdsByType(members, KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE)) {
            if (!visitedGroupIds.contains(memberGroupId)) {
                visitedGroupIds.add(memberGroupId);
                if (isMemberOfGroupInternal(memberId, memberGroupId, visitedGroupIds, memberType)) {
                    return true;
                }
            }
        }

        // no match found, return false
        return false;
    }

    protected void getParentGroupsInternal(String groupId, Set<Group> groups) {
        List<Group> parentGroups = getDirectParentGroups(groupId);
        for (Group group : parentGroups) {
            if (!groups.contains(group)) {
                groups.add(group);
                getParentGroupsInternal(group.getId(), groups);
            }
        }
    }

    protected List<Group> getDirectParentGroups(String groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, groupId);
        criteria.put(KIMPropertyConstants.KimMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE.getCode());

        List<GroupMember> groupMembers = (List<GroupMember>) businessObjectService.findMatching(
                GroupMember.class, criteria);
        Set<String> matchingGroupIds = new HashSet<>();
        // filter to active groups
        for (GroupMember gm : groupMembers) {
            if (gm.isActive(new Timestamp(System.currentTimeMillis()))) {
                matchingGroupIds.add(gm.getGroupId());
            }
        }
        if (!matchingGroupIds.isEmpty()) {
            return getGroups(matchingGroupIds);
        }
        return Collections.emptyList();
    }

    @Cacheable(value = GroupMember.CACHE_NAME, key = "'groupId=' + #p0")
    @Override
    public List<GroupMember> getMembersOfGroup(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.GROUP_ID, groupId);

        Collection<GroupMember> groupMembersBos = businessObjectService.findMatching(GroupMember.class, criteria);
        return groupMembersBos.stream()
                .filter(group -> group.isActive(new Timestamp(System.currentTimeMillis())))
                .collect(Collectors.toUnmodifiableList());
    }

    protected List<String> getMemberIdsByType(Collection<GroupMember> members, MemberType memberType) {
        List<String> membersIds = new ArrayList<>();
        if (members != null) {
            for (GroupMember member : members) {
                if (member.getType().equals(memberType)) {
                    membersIds.add(member.getMemberId());
                }
            }
        }
        return Collections.unmodifiableList(membersIds);
    }

    protected Group getGroupUncached(String groupId) {
        incomingParamCheck(groupId, "groupId");
        return businessObjectService.findByPrimaryKey(Group.class, Collections.singletonMap("id", groupId));
    }

    protected List<Group> getParentGroups(String groupId) throws IllegalArgumentException {
        if (StringUtils.isEmpty(groupId)) {
            throw new IllegalArgumentException("groupId is blank");
        }
        Set<Group> groups = new HashSet<>();
        getParentGroupsInternal(groupId, groups);
        return new ArrayList<>(groups);
    }

    protected List<String> getMemberPrincipalIdsInternal(String groupId, Set<String> visitedGroupIds) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        Group group = getGroupUncached(groupId);
        if (group == null || !group.isActive()) {
            return Collections.emptyList();
        }

        Set<String> ids = new HashSet<>(group.getMemberPrincipalIds());
        visitedGroupIds.add(group.getId());

        for (String memberGroupId : group.getMemberGroupIds()) {
            if (!visitedGroupIds.contains(memberGroupId)) {
                ids.addAll(getMemberPrincipalIdsInternal(memberGroupId, visitedGroupIds));
            }
        }

        return List.copyOf(ids);
    }

    protected Collection<Group> getDirectGroupsForPrincipal(String principalId) {
        return getDirectGroupsForPrincipal(principalId, null);
    }

    protected Collection<Group> getDirectGroupsForPrincipal(String principalId, String namespaceCode) {
        if (principalId == null) {
            return Collections.emptyList();
        }
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.KimMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        Collection<GroupMember> groupMembers = businessObjectService.findMatching(GroupMember.class, criteria);
        Set<String> groupIds = new HashSet<>(groupMembers.size());
        // only return the active members
        for (GroupMember gm : groupMembers) {
            if (gm.isActive(new Timestamp(System.currentTimeMillis()))) {
                groupIds.add(gm.getGroupId());
            }
        }
        // pull all the group information for the matching members
        List<Group> groups = groupIds.isEmpty() ? Collections.emptyList() : getGroups(groupIds);
        List<Group> result = new ArrayList<>(groups.size());
        // filter by namespace if necessary
        for (Group group : groups) {
            if (group.isActive()) {
                if (StringUtils.isBlank(namespaceCode) || StringUtils.equals(namespaceCode, group.getNamespaceCode())) {
                    result.add(group);
                }
            }
        }
        return result;
    }

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public boolean addGroupToGroup(String childId, String parentId) throws IllegalArgumentException {
        incomingParamCheck(childId, "childId");
        incomingParamCheck(parentId, "parentId");

        if (childId.equals(parentId)) {
            throw new IllegalArgumentException("Can't add group to itself.");
        }
        if (isGroupMemberOfGroup(parentId, childId)) {
            throw new IllegalArgumentException("Circular group reference.");
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(parentId);
        groupMember.setType(KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
        groupMember.setMemberId(childId);

        this.businessObjectService.save(groupMember);
        return true;
    }

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public boolean addPrincipalToGroup(String principalId, String groupId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(groupId, "groupId");

        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setType(KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
        groupMember.setMemberId(principalId);

        groupMember = this.businessObjectService.save(groupMember);
        KimImplServiceLocator.getGroupInternalService().updateForUserAddedToGroup(groupMember.getMemberId(),
                groupMember.getGroupId());
        return true;
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME}, allEntries = true)
    @Override
    public Group createGroup(Group group) throws IllegalArgumentException {
        incomingParamCheck(group, "group");
        if (StringUtils.isNotBlank(group.getId()) && getGroup(group.getId()) != null) {
            throw new IllegalArgumentException("the group to create already exists: " + group);
        }
        List<GroupAttribute> attrBos = KimAttributeData
                .createFrom(GroupAttribute.class, group.getAttributes(), group.getKimTypeId());
        if (StringUtils.isNotEmpty(group.getId())) {
            for (GroupAttribute attr : attrBos) {
                attr.setAssignedToId(group.getId());
            }
        }
        group.setAttributeDetails(attrBos);

        return saveGroup(group);
    }

    private Group updateGroup(Group group) throws IllegalArgumentException {
        incomingParamCheck(group, "group");
        Group origGroup = getGroupUncached(group.getId());
        if (StringUtils.isBlank(group.getId()) || origGroup == null) {
            throw new IllegalArgumentException("the group does not exist: " + group);
        }
        List<GroupAttribute> attrBos = KimAttributeData.createFrom(GroupAttribute.class, group.getAttributes(),
                group.getKimTypeId());
        group.setMembers(origGroup.getMembers());
        group.setAttributeDetails(attrBos);

        group = saveGroup(group);
        if (origGroup.isActive() && !group.isActive()) {
            KimImplServiceLocator.getRoleInternalService().groupInactivated(group.getId());
        }

        return group;
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME}, allEntries = true)
    @Override
    public Group updateGroup(String groupId, Group group) throws IllegalArgumentException {
        incomingParamCheck(group, "group");
        incomingParamCheck(groupId, "groupId");

        if (StringUtils.equals(groupId, group.getId())) {
            return updateGroup(group);
        }

        //if group Ids are different, inactivate old group, and create new with new id based off old
        Group oldGroup = getGroupUncached(groupId);

        if (StringUtils.isBlank(group.getId()) || oldGroup == null) {
            throw new IllegalArgumentException("the group does not exist: " + group);
        }

        //create and save new group
        group.setMembers(oldGroup.getMembers());
        List<GroupAttribute> attrBos = KimAttributeData.createFrom(GroupAttribute.class, group.getAttributes(),
                group.getKimTypeId());
        group.setAttributeDetails(attrBos);
        group = saveGroup(group);

        //inactivate and save old group
        oldGroup.setActive(false);
        saveGroup(oldGroup);

        return group;
    }

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public void removeAllMembers(String groupId) throws IllegalArgumentException {
        incomingParamCheck(groupId, "groupId");

        GroupService groupService = KimApiServiceLocator.getGroupService();
        List<String> memberPrincipalsBefore = groupService.getMemberPrincipalIds(groupId);

        Collection<GroupMember> toDeactivate = getActiveGroupMembers(groupId);
        Timestamp today = new Timestamp(System.currentTimeMillis());

        // Set principals as inactive
        for (GroupMember aToDeactivate : toDeactivate) {
            aToDeactivate.setActiveToDateValue(today);
        }

        // Save
        this.businessObjectService.save(new ArrayList<>(toDeactivate));
        List<String> memberPrincipalsAfter = groupService.getMemberPrincipalIds(groupId);

        if (!memberPrincipalsAfter.isEmpty()) {
            // should never happen!
            LOG.warn("after attempting removal of all members, group with id '" + groupId +
                    "' still has principal members");
        }

        // do updates
        KimImplServiceLocator.getGroupInternalService().updateForWorkgroupChange(groupId, memberPrincipalsBefore,
                memberPrincipalsAfter);
    }

    protected Group saveGroup(Group group) {
        if (group == null) {
            return null;
        } else if (group.getId() != null) {
            // Get the version of the group that is in the DB
            Group oldGroup = getGroupUncached(group.getId());

            if (oldGroup != null) {
                // Inactivate and re-add members no longer in the group (in order to preserve history).
                Timestamp activeTo = new Timestamp(System.currentTimeMillis());
                List<GroupMember> toReAdd = null;

                if (oldGroup.getMembers() != null) {
                    for (GroupMember member : oldGroup.getMembers()) {
                        // if the old member isn't in the new group
                        if (group.getMembers() == null || !group.getMembers().contains(member)) {
                            // inactivate the member
                            member.setActiveToDateValue(activeTo);
                            if (toReAdd == null) {
                                toReAdd = new ArrayList<>();
                            }
                            // queue it up for re-adding
                            toReAdd.add(member);
                        }
                    }
                }

                // do the re-adding
                if (toReAdd != null) {
                    List<GroupMember> groupMembers = group.getMembers();
                    if (groupMembers == null) {
                        groupMembers = new ArrayList<>(toReAdd.size());
                    }
                    group.setMembers(groupMembers);
                }
            }
        }

        return KimImplServiceLocator.getGroupInternalService().saveWorkgroup(group);
    }

    /**
     * This helper method gets the active group members of the specified type
     * (@see {@link KimConstants.KimGroupMemberTypes}). If the optional params are null, it will return all active
     * members for the specified group regardless of type.
     *
     * @param parentId
     * @return a list of group members
     */
    private List<GroupMember> getActiveGroupMembers(String parentId) {
        final Date today = new Date(System.currentTimeMillis());

        Collection<GroupMember> groupMembers = this.businessObjectService.findMatching(GroupMember.class,
                Map.of(KIMPropertyConstants.GroupMember.GROUP_ID, parentId));

        CollectionUtils.filter(groupMembers, (Predicate) object -> {
            GroupMember member = (GroupMember) object;
            // keep in the collection (return true) if the activeToDate is null, or if it is set to a future date
            return member.getActiveToDate() == null || today.before(member.getActiveToDate().toDate());
        });

        return new ArrayList<>(groupMembers);
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " was null");
        } else if (object instanceof String
                && StringUtils.isBlank((String) object)) {
            throw new IllegalArgumentException(name + " was blank");
        }
    }
}
