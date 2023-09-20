package edu.cornell.kfs.kim.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ojb.broker.core.proxy.CollectionProxyDefaultImpl;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.service.impl.UiDocumentServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuUiDocumentServiceImpl extends UiDocumentServiceImpl {

    /**
     * Overridden to allow for loading unmodified role members even when there are no delegations,
     * and to create a copy of the RoleBo's members list (to prevent potential member auto-deletion).
     */
    @Override
    public void setMembersInDocument(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, identityManagementRoleDocument.getRoleId());
        final Role roleBo = businessObjectService.findByPrimaryKey(Role.class, criteria);
        if (ObjectUtils.isNotNull(roleBo)) {
            final List<RoleMember> members = new ArrayList<>(roleBo.getMembers());
            final List<RoleMember> membersToRemove = new ArrayList<>();
            boolean found = false;
            for (final KimDocumentRoleMember modifiedMember : identityManagementRoleDocument.getModifiedMembers()) {
                for (final RoleMember member : members) {
                    if (modifiedMember.getRoleMemberId().equals(member.getId())) {
                        membersToRemove.add(member);
                        found = true;
                    }
                    if (found) {
                        break;
                    }
                }
            }
            for (final RoleMember memberToRemove : membersToRemove) {
                members.remove(memberToRemove);
            }
    
            identityManagementRoleDocument.setMembers(loadRoleMembers(identityManagementRoleDocument, members));
            loadMemberRoleRspActions(identityManagementRoleDocument);
        }
    }

    /**
     * Overridden to also iterate over the role's members and forcibly load their lists of responsibility actions.
     * This is needed to fix a bug that sometimes occurs when an object contains a lazy-loaded OJB collection proxy
     * but the code forcibly replaces that list with a different type. Such situations may trigger a rare exception
     * when OJB tries to perform a bulk pre-fetch of that specific collection property across multiple objects.
     */
    @Override
    protected void updateRoleMembers(String roleId, List<KimDocumentRoleMember> modifiedRoleMembers,
            List<RoleMember> roleMembers) {
        if (CollectionUtils.isNotEmpty(modifiedRoleMembers) && CollectionUtils.isNotEmpty(roleMembers)) {
            for (RoleMember roleMember : roleMembers) {
                List<RoleResponsibilityAction> rspActions = roleMember.getRoleRspActions();
                if (rspActions instanceof CollectionProxyDefaultImpl) {
                    ((CollectionProxyDefaultImpl) rspActions).getData();
                }
            }
        }
        super.updateRoleMembers(roleId, modifiedRoleMembers, roleMembers);
    }

}
