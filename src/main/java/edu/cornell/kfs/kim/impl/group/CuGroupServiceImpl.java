package edu.cornell.kfs.kim.impl.group;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.group.GroupServiceImpl;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;

import edu.cornell.kfs.sys.CUKFSConstants;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;

public class CuGroupServiceImpl extends GroupServiceImpl {

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public boolean addGroupToGroup(String childId, String parentId) throws IllegalArgumentException {
        checkGroupEditability(parentId, "parentId");
        return super.addGroupToGroup(childId, parentId);
    }

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public boolean addPrincipalToGroup(String principalId, String groupId) throws IllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        return super.addPrincipalToGroup(principalId, groupId);
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME}, allEntries = true)
    @Override
    public Group createGroup(Group group) throws IllegalArgumentException {
        checkGroupEditability(group, "group");
        return super.createGroup(group);
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME}, allEntries = true)
    @Override
    public Group updateGroup(Group group) throws IllegalArgumentException {
        checkGroupEditability(group, "group");
        return super.updateGroup(group);
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME}, allEntries = true)
    @Override
    public Group updateGroup(String groupId, Group group) throws IllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        checkGroupEditability(group, "group");
        return super.updateGroup(groupId, group);
    }

    @CacheEvict(value = {GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public void removeAllMembers(String groupId) throws IllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        super.removeAllMembers(groupId);
    }

    @Override
    protected Group saveGroup(Group group) {
        checkGroupEditability(group, "group");
        return super.saveGroup(group);
    }

    private void checkGroupEditability(String groupId, String fieldName) {
        if (StringUtils.isNotBlank(groupId)) {
            Group group = getGroupUncached(groupId);
            checkGroupEditability(group, fieldName);
        }
    }

    private void checkGroupEditability(Group group, String fieldName) {
        if (isPermitGroup(group)) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be a reference to a group with the 'PERMIT' namespace");
        }
    }

    private boolean isPermitGroup(Group group) {
        return ObjectUtils.isNotNull(group)
                && StringUtils.equalsIgnoreCase(CUKFSConstants.LEGACY_PERMIT_NAMESPACE, group.getNamespaceCode());
    }
    
    /**
     * FINP-7432 changes from KualiCo patch release 2021-03-11 backported onto original
     * KEW-to-KFS KualiCo patch release 2021-01-28 version of the method.
     */
    @Override
    protected List<Group> getDirectParentGroups(String groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, groupId);
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE,
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

}
