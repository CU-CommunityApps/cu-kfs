package edu.cornell.kfs.kim.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.service.impl.UiDocumentServiceImpl;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuUiDocumentServiceImpl extends UiDocumentServiceImpl {

    private BusinessObjectService businessObjectService;

    /**
     * Overridden to allow for loading unmodified role members even when there are no delegations,
     * and to create a copy of the RoleBo's members list (to prevent potential member auto-deletion).
     */
    @Override
    public void setMembersInDocument(IdentityManagementRoleDocument identityManagementRoleDocument) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, identityManagementRoleDocument.getRoleId());
        Role roleBo = businessObjectService.findByPrimaryKey(Role.class, criteria);
        if (ObjectUtils.isNotNull(roleBo)) {
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
    }

    @Override
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

}
