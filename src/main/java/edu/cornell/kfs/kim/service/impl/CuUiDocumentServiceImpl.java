package edu.cornell.kfs.kim.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.document.IdentityManagementGroupDocument;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.service.impl.UiDocumentServiceImpl;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CuUiDocumentServiceImpl extends UiDocumentServiceImpl {

    /**
     * Overridden to exclude PERMIT groups from membership updates by the Person Document.
     */
    @Override
    protected List<GroupMember> populateGroupMembers(
            IdentityManagementPersonDocument identityManagementPersonDocument) {
        List<GroupMember> groupMembers = super.populateGroupMembers(identityManagementPersonDocument);
        Set<String> groupIdWhitelist = getIdsOfUpdatableGroups(identityManagementPersonDocument);
        return groupMembers.stream()
                .filter(groupMember -> groupIdWhitelist.contains(groupMember.getGroupId()))
                .collect(Collectors.toList());
    }

    protected Set<String> getIdsOfUpdatableGroups(IdentityManagementPersonDocument identityManagementPersonDocument) {
        if (CollectionUtils.isEmpty(identityManagementPersonDocument.getGroups())) {
            return Collections.emptySet();
        }
        return identityManagementPersonDocument.getGroups().stream()
                .filter(group -> !StringUtils.equalsIgnoreCase(
                        CUKFSConstants.LEGACY_PERMIT_NAMESPACE, group.getNamespaceCode()))
                .map(PersonDocumentGroup::getGroupId)
                .collect(Collectors.toSet());
    }

    /**
     * Overridden to allow for loading unmodified role members even when there are no delegations,
     * and to create a copy of the RoleBo's members list (to prevent potential member auto-deletion).
     */
    @Override
    public void setMembersInDocument(IdentityManagementRoleDocument identityManagementRoleDocument) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, identityManagementRoleDocument.getRoleId());
        Role roleBo = getBusinessObjectService().findByPrimaryKey(Role.class, criteria);
        List<RoleMember> members = new ArrayList<>(roleBo.getMembers());
        List<RoleMember> membersToRemove = new ArrayList<>();
        boolean found = false;
        for (KimDocumentRoleMember modifiedMember : identityManagementRoleDocument.getModifiedMembers()) {
            for (RoleMember member : members) {
                if (modifiedMember.getRoleMemberId().equals(member.getId())) {
                    membersToRemove.add(member);
                    found = true;
                }
                if (found) {
                    break;
                }
            }
        }
        for (RoleMember memberToRemove : membersToRemove) {
            members.remove(memberToRemove);
        }

        identityManagementRoleDocument.setMembers(loadRoleMembers(identityManagementRoleDocument, members));
        loadMemberRoleRspActions(identityManagementRoleDocument);
    }

    /**
     * Overridden to prevent users from updating PERMIT groups via the Group Document.
     */
    @Override
    public void saveGroup(IdentityManagementGroupDocument identityManagementGroupDocument) {
        if (StringUtils.equalsIgnoreCase(
                CUKFSConstants.LEGACY_PERMIT_NAMESPACE, identityManagementGroupDocument.getGroupNamespace())) {
            throw new UnsupportedOperationException("Cannot modify PERMIT groups using the Group Document");
        }
        super.saveGroup(identityManagementGroupDocument);
    }

}
