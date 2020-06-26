package edu.cornell.kfs.kim.impl.group;

import javax.jws.WebParam;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.group.GroupBo;
import org.kuali.kfs.kim.impl.group.GroupServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupContract;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.group.GroupMemberContract;

import edu.cornell.cynergy.CynergyConstants;

public class CuGroupServiceImpl extends GroupServiceImpl {

    @Override
    public boolean addGroupToGroup(String childId, String parentId) throws RiceIllegalArgumentException {
        checkGroupEditability(parentId, "parentId");
        return super.addGroupToGroup(childId, parentId);
    }

    @Override
    public boolean addPrincipalToGroup(String principalId, String groupId) throws RiceIllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        return super.addPrincipalToGroup(principalId, groupId);
    }

    @Override
    public Group createGroup(Group group) throws RiceIllegalArgumentException {
        checkGroupEditability(group, "group");
        return super.createGroup(group);
    }

    @Override
    public Group updateGroup(Group group) throws RiceIllegalArgumentException {
        checkGroupEditability(group, "group");
        return super.updateGroup(group);
    }

    @Override
    public Group updateGroup(String groupId, Group group) throws RiceIllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        checkGroupEditability(group, "group");
        return super.updateGroup(groupId, group);
    }

    @Override
    public GroupMember createGroupMember(GroupMember groupMember) throws RiceIllegalArgumentException {
        checkMemberEditability(groupMember, "groupMember");
        return super.createGroupMember(groupMember);
    }

    @Override
    public GroupMember updateGroupMember(
            @WebParam(name = "groupMember") GroupMember groupMember) throws RiceIllegalArgumentException {
        checkMemberEditability(groupMember, "groupMember");
        return super.updateGroupMember(groupMember);
    }

    @Override
    public void removeAllMembers(String groupId) throws RiceIllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        super.removeAllMembers(groupId);
    }

    @Override
    public boolean removeGroupFromGroup(String childId, String parentId) throws RiceIllegalArgumentException {
        checkGroupEditability(parentId, "parentId");
        return super.removeGroupFromGroup(childId, parentId);
    }

    @Override
    public boolean removePrincipalFromGroup(String principalId, String groupId) throws RiceIllegalArgumentException {
        checkGroupEditability(groupId, "groupId");
        return super.removePrincipalFromGroup(principalId, groupId);
    }

    @Override
    protected GroupBo saveGroup(GroupBo group) {
        checkGroupEditability(group, "group");
        return super.saveGroup(group);
    }

    private void checkMemberEditability(GroupMemberContract groupMember, String fieldName) {
        if (ObjectUtils.isNotNull(groupMember)) {
            String groupId = groupMember.getGroupId();
            if (StringUtils.isNotBlank(groupId)) {
                GroupBo group = getGroupBo(groupId);
                if (isPermitGroup(group)) {
                    throw new RiceIllegalArgumentException(
                            fieldName + " cannot be a member of a group that has the 'PERMIT' namespace");
                }
            }
        }
    }

    private void checkGroupEditability(String groupId, String fieldName) {
        if (StringUtils.isNotBlank(groupId)) {
            GroupBo group = getGroupBo(groupId);
            checkGroupEditability(group, fieldName);
        }
    }

    private void checkGroupEditability(GroupContract group, String fieldName) {
        if (isPermitGroup(group)) {
            throw new RiceIllegalArgumentException(
                    fieldName + " cannot be a reference to a group with the 'PERMIT' namespace");
        }
    }

    private boolean isPermitGroup(GroupContract group) {
        return ObjectUtils.isNotNull(group)
                && StringUtils.equalsIgnoreCase(CynergyConstants.DEFAULT_PERMIT_NAMESPACE, group.getNamespaceCode());
    }

}
