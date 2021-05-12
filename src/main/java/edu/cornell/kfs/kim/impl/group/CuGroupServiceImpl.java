package edu.cornell.kfs.kim.impl.group;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.group.GroupServiceImpl;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;

import edu.cornell.kfs.sys.CUKFSConstants;

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

}
