/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.kim.document;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRolePermission;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibility;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibilityAction;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalService;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.IdentityManagementTypeAttributeTransactionalDocument;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.kim.web.struts.form.IdentityManagementRoleDocumentForm;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.util.AutoPopulatingList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
 * CU Customization:
 * Added override of addDelegationMemberToDelegation() method so that it will work properly
 * for delegation members that are not tied to a specific role member (such as for
 * derived delegation members on the Fiscal Officer role).
 */
public class IdentityManagementRoleDocument extends IdentityManagementTypeAttributeTransactionalDocument {

    private static final long serialVersionUID = 1L;

    // principal data
    protected String roleId;

    protected String roleTypeId;

    protected String roleTypeName;

    protected String roleNamespace = "";

    protected String roleName = "";

    protected String roleDescription = "";

    protected boolean active = true;

    protected boolean editing;

    protected List<KimDocumentRolePermission> permissions = new AutoPopulatingList<>(KimDocumentRolePermission.class);

    protected List<KimDocumentRoleResponsibility> responsibilities =
            new AutoPopulatingList<>(KimDocumentRoleResponsibility.class);

    protected List<KimDocumentRoleMember> modifiedMembers = new AutoPopulatingList<>(KimDocumentRoleMember.class);
    protected List<KimDocumentRoleMember> searchResultMembers = new ArrayList<>();
    protected List<KimDocumentRoleMember> members = new ArrayList<>();
    protected RoleMemberMetaDataType memberMetaDataType = RoleMemberMetaDataType.MEMBER_NAME;
    private List<RoleDocumentDelegationMember> delegationMembers =
            new AutoPopulatingList<>(RoleDocumentDelegationMember.class);
    private List<RoleDocumentDelegation> delegations = new AutoPopulatingList<>(RoleDocumentDelegation.class);
    private transient ResponsibilityInternalService responsibilityInternalService;

    public IdentityManagementRoleDocument() {
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return this.roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRoleNamespace() {
        return this.roleNamespace;
    }

    public void setRoleNamespace(String roleNamespace) {
        this.roleNamespace = roleNamespace;
    }

    public String getRoleTypeId() {
        return this.roleTypeId;
    }

    public void setRoleTypeId(String roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    public String getRoleTypeName() {
        if (roleTypeName == null) {
            if (kimType != null) {
                roleTypeName = kimType.getName();
            } else if (roleTypeId != null) {
                setKimType(KimApiServiceLocator.getKimTypeInfoService().getKimType(roleTypeId));
                if (kimType != null) {
                    roleTypeName = kimType.getName();
                }
            }
        }
        return this.roleTypeName;
    }

    public void setRoleTypeName(String roleTypeName) {
        this.roleTypeName = roleTypeName;
    }

    @Override
    public List<RoleDocumentDelegationMember> getDelegationMembers() {
        return this.delegationMembers;
    }

    @Override
    public void setDelegationMembers(List<RoleDocumentDelegationMember> delegationMembers) {
        this.delegationMembers = delegationMembers;
    }

    public List<KimDocumentRolePermission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<KimDocumentRolePermission> permissions) {
        this.permissions = permissions;
    }

    public List<KimDocumentRoleResponsibility> getResponsibilities() {
        return this.responsibilities;
    }

    public void setResponsibilities(List<KimDocumentRoleResponsibility> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<KimDocumentRoleMember> getMembers() {
        return this.members;
    }

    public void setMembers(List<KimDocumentRoleMember> members) {
        this.members = members;
    }

    public void setMemberMetaDataTypeToSort(Integer columnNumber) {
        switch (columnNumber) {
            case 1:
                this.memberMetaDataType = RoleMemberMetaDataType.MEMBER_ID;
                break;
            case 3:
                this.memberMetaDataType = RoleMemberMetaDataType.FULL_MEMBER_NAME;
                break;
            case 2:
            default:
                this.memberMetaDataType = RoleMemberMetaDataType.MEMBER_NAME;
                break;
        }
    }

    public RoleMemberMetaDataType getMemberMetaDataType() {
        return memberMetaDataType;
    }

    public void setMemberMetaDataType(RoleMemberMetaDataType memberMetaDataType) {
        this.memberMetaDataType = memberMetaDataType;
    }

    public KimDocumentRoleMember getMember(String roleMemberId) {
        if (StringUtils.isEmpty(roleMemberId)) {
            return null;
        }
        for (KimDocumentRoleMember roleMember : getMembers()) {
            if (roleMemberId.equals(roleMember.getRoleMemberId())) {
                return roleMember;
            }
        }
        return null;
    }

    public List<KimDocumentRoleMember> getModifiedMembers() {
        return this.modifiedMembers;
    }

    public void setModifiedMembers(List<KimDocumentRoleMember> modifiedMembers) {
        this.modifiedMembers = modifiedMembers;
    }

    public List<KimDocumentRoleMember> getSearchResultMembers() {
        return this.searchResultMembers;
    }

    public void setSearchResultMembers(List<KimDocumentRoleMember> searchResultMembers) {
        this.searchResultMembers = searchResultMembers;
    }

    public void addResponsibility(KimDocumentRoleResponsibility roleResponsibility) {
        if (!getResponsibilityInternalService().areActionsAtAssignmentLevelById(
                roleResponsibility.getResponsibilityId())) {
            roleResponsibility.getRoleRspActions().add(getNewRespAction(roleResponsibility));
        }
        getResponsibilities().add(roleResponsibility);
    }

    protected KimDocumentRoleResponsibilityAction getNewRespAction(KimDocumentRoleResponsibility roleResponsibility) {
        KimDocumentRoleResponsibilityAction roleRspAction = new KimDocumentRoleResponsibilityAction();
        roleRspAction.setKimResponsibility(roleResponsibility.getKimResponsibility());
        roleRspAction.setRoleResponsibilityId(roleResponsibility.getRoleResponsibilityId());
        return roleRspAction;
    }

    public void addDelegationMember(RoleDocumentDelegationMember newDelegationMember) {
        getDelegationMembers().add(newDelegationMember);
    }

    public void addMember(KimDocumentRoleMember member) {
        SequenceAccessorService sas = getSequenceAccessorService();
        Long nextSeq = sas.getNextAvailableSequenceNumber(
                KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S,
                KimDocumentRoleMember.class);
        String roleMemberId = nextSeq.toString();
        member.setRoleMemberId(roleMemberId);
        setupMemberRspActions(member);
        getModifiedMembers().add(member);
    }

    public KimDocumentRoleMember getBlankMember() {
        KimDocumentRoleMember member = new KimDocumentRoleMember();
        KimDocumentRoleQualifier qualifier;
        if (getDefinitions() != null) {
            for (KimAttributeField key : getDefinitions()) {
                qualifier = new KimDocumentRoleQualifier();
                qualifier.setKimAttrDefnId(getKimAttributeDefnId(key));
                member.getQualifiers().add(qualifier);
            }
        }
        setupMemberRspActions(member);
        return member;
    }

    public RoleDocumentDelegationMember getBlankDelegationMember() {
        RoleDocumentDelegationMember member = new RoleDocumentDelegationMember();
        RoleDocumentDelegationMemberQualifier qualifier;
        if (getDefinitions() != null) {
            for (KimAttributeField key : getDefinitions()) {
                qualifier = new RoleDocumentDelegationMemberQualifier();
                setAttrDefnIdForDelMemberQualifier(qualifier, key);
                member.getQualifiers().add(qualifier);
            }
        }
        return member;
    }

    public void setupMemberRspActions(KimDocumentRoleMember member) {
        member.getRoleRspActions().clear();
        for (KimDocumentRoleResponsibility roleResp : getResponsibilities()) {
            if (getResponsibilityInternalService().areActionsAtAssignmentLevelById(roleResp.getResponsibilityId())) {
                KimDocumentRoleResponsibilityAction action = new KimDocumentRoleResponsibilityAction();
                action.setRoleResponsibilityId("*");
                action.setRoleMemberId(member.getRoleMemberId());
                member.getRoleRspActions().add(action);
                break;
            }
        }
    }

    public void setupMemberRspActions(KimDocumentRoleResponsibility roleResp, KimDocumentRoleMember member) {
        if ((member.getRoleRspActions() == null || member.getRoleRspActions().size() < 1)
                && getResponsibilityInternalService().areActionsAtAssignmentLevelById(roleResp.getResponsibilityId())) {
            KimDocumentRoleResponsibilityAction action = new KimDocumentRoleResponsibilityAction();
            action.setRoleResponsibilityId("*");
            action.setRoleMemberId(member.getRoleMemberId());
            if (member.getRoleRspActions() == null) {
                member.setRoleRspActions(new ArrayList<>());
            }
            member.getRoleRspActions().add(action);
        }
    }

    public void updateMembers(IdentityManagementRoleDocumentForm roleDocumentForm) {
        for (KimDocumentRoleMember member : roleDocumentForm.getRoleDocument().getMembers()) {
            roleDocumentForm.getRoleDocument().setupMemberRspActions(member);
        }
    }

    public void updateMembers(KimDocumentRoleResponsibility newResponsibility) {
        for (KimDocumentRoleMember member : getMembers()) {
            setupMemberRspActions(newResponsibility, member);
        }
    }

    protected void setAttrDefnIdForDelMemberQualifier(RoleDocumentDelegationMemberQualifier qualifier,
            KimAttributeField definition) {
        qualifier.setKimAttrDefnId(definition.getId());
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            KIMServiceLocatorInternal.getUiDocumentService().saveRole(this);
        }
    }

    public void initializeDocumentForNewRole() {
        if (StringUtils.isBlank(this.roleId)) {
            SequenceAccessorService sas = getSequenceAccessorService();
            Long nextSeq = sas.getNextAvailableSequenceNumber(
                    KimConstants.SequenceNames.KRIM_ROLE_ID_S, this.getClass());
            this.roleId = nextSeq.toString();
        }
        if (StringUtils.isBlank(this.roleTypeId)) {
            this.roleTypeId = "1";
        }
    }

    public String getRoleId() {
        if (StringUtils.isBlank(this.roleId)) {
            initializeDocumentForNewRole();
        }
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public void prepareForSave() {
        SequenceAccessorService sas = getSequenceAccessorService();

        String roleId;
        if (StringUtils.isBlank(getRoleId())) {
            Long nextSeq = sas.getNextAvailableSequenceNumber(
                    KimConstants.SequenceNames.KRIM_ROLE_ID_S, this.getClass());
            roleId = nextSeq.toString();
            setRoleId(roleId);
        } else {
            roleId = getRoleId();
        }
        if (getPermissions() != null) {
            String rolePermissionId;
            for (KimDocumentRolePermission permission : getPermissions()) {
                permission.setRoleId(roleId);
                permission.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isBlank(permission.getRolePermissionId())) {
                    Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S,
                            KimDocumentRolePermission.class);
                    rolePermissionId = nextSeq.toString();
                    permission.setRolePermissionId(rolePermissionId);
                }
            }
        }
        if (getResponsibilities() != null) {
            String roleResponsibilityId;
            for (KimDocumentRoleResponsibility responsibility: getResponsibilities()) {
                if (StringUtils.isBlank(responsibility.getRoleResponsibilityId())) {
                    Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_RSP_ID_S,
                            KimDocumentRoleResponsibility.class);
                    roleResponsibilityId = nextSeq.toString();
                    responsibility.setRoleResponsibilityId(roleResponsibilityId);
                }
                responsibility.setRoleId(roleId);
                if (!getResponsibilityInternalService().areActionsAtAssignmentLevelById(responsibility.getResponsibilityId())) {
                    if (StringUtils.isBlank(responsibility.getRoleRspActions().get(0).getRoleResponsibilityActionId())) {
                        Long nextSeq = sas.getNextAvailableSequenceNumber(
                                KimConstants.SequenceNames.KRIM_ROLE_RSP_ACTN_ID_S,
                                KimDocumentRoleResponsibilityAction.class);
                        String roleResponsibilityActionId = nextSeq.toString();
                        responsibility.getRoleRspActions().get(0).setRoleResponsibilityActionId(roleResponsibilityActionId);
                    }
                    responsibility.getRoleRspActions().get(0).setRoleMemberId("*");
                    responsibility.getRoleRspActions().get(0).setDocumentNumber(getDocumentNumber());
                }
            }
        }
        if (getModifiedMembers() != null) {
            String roleMemberId;
            String roleResponsibilityActionId;
            for (KimDocumentRoleMember member : getModifiedMembers()) {
                member.setDocumentNumber(getDocumentNumber());
                member.setRoleId(roleId);
                if (StringUtils.isBlank(member.getRoleMemberId())) {
                    Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S,
                            KimDocumentRoleMember.class);
                    roleMemberId = nextSeq.toString();
                    member.setRoleMemberId(roleMemberId);
                }
                for (KimDocumentRoleQualifier qualifier : member.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setRoleMemberId(member.getRoleMemberId());
                    qualifier.setKimTypId(getKimType().getId());
                }
                for (KimDocumentRoleResponsibilityAction roleRespAction : member.getRoleRspActions()) {
                    if (StringUtils.isBlank(roleRespAction.getRoleResponsibilityActionId())) {
                        Long nextSeq = sas.getNextAvailableSequenceNumber(
                                KimConstants.SequenceNames.KRIM_ROLE_RSP_ACTN_ID_S,
                                KimDocumentRoleResponsibilityAction.class);
                        roleResponsibilityActionId = nextSeq.toString();
                        roleRespAction.setRoleResponsibilityActionId(roleResponsibilityActionId);
                    }
                    roleRespAction.setRoleMemberId(member.getRoleMemberId());
                    roleRespAction.setDocumentNumber(getDocumentNumber());
                    if (!StringUtils.equals(roleRespAction.getRoleResponsibilityId(), "*")) {
                        for (KimDocumentRoleResponsibility responsibility : getResponsibilities()) {
                            if (StringUtils.equals(roleRespAction.getKimResponsibility().getId(),
                                    responsibility.getResponsibilityId())) {
                                roleRespAction.setRoleResponsibilityId(responsibility.getRoleResponsibilityId());
                            }
                        }
                    }
                }
            }
        }
        if (getDelegationMembers() != null) {
            for (RoleDocumentDelegationMember delegationMember : getDelegationMembers()) {
                delegationMember.setDocumentNumber(getDocumentNumber());
                addDelegationMemberToDelegation(delegationMember);
            }
            for (RoleDocumentDelegation delegation : getDelegations()) {
                delegation.setDocumentNumber(getDocumentNumber());
                delegation.setKimTypeId(getKimType().getId());
                List<RoleDocumentDelegationMember> membersToRemove =
                        new AutoPopulatingList<>(RoleDocumentDelegationMember.class);
                for (RoleDocumentDelegationMember member : delegation.getMembers()) {
                    if (delegation.getDelegationId().equals(member.getDelegationId())
                            && delegation.getDelegationTypeCode().equals(member.getDelegationTypeCode())) {
                        for (RoleDocumentDelegationMemberQualifier qualifier : member.getQualifiers()) {
                            qualifier.setKimTypId(getKimType().getId());
                            qualifier.setDocumentNumber(getDocumentNumber());
                        }
                    } else {
                        membersToRemove.add(member);
                    }
                }
                if (!membersToRemove.isEmpty()) {
                    for (RoleDocumentDelegationMember member : membersToRemove) {
                        delegation.getMembers().remove(member);
                    }
                }
                delegation.setRoleId(roleId);
            }
        }
    }

    public ResponsibilityInternalService getResponsibilityInternalService() {
        if (responsibilityInternalService == null) {
            responsibilityInternalService = KimImplServiceLocator.getResponsibilityInternalService();
        }
        return responsibilityInternalService;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    @Override
    public List<RoleDocumentDelegation> getDelegations() {
        return this.delegations;
    }

    @Override
    public void setDelegations(List<RoleDocumentDelegation> delegations) {
        this.delegations = delegations;
    }

    @Override
    public void setKimType(KimType kimType) {
        super.setKimType(kimType);
        if (kimType != null) {
            setRoleTypeId(kimType.getId());
            setRoleTypeName(kimType.getName());
        }
    }

    /*
     * CU Customization:
     * Overridden to perform extra setup on the transient RoleBo objects as needed.
     * This is needed to allow the document to function properly for delegation members
     * that are not tied to a specific role member.
     */
    @Override
    protected void addDelegationMemberToDelegation(RoleDocumentDelegationMember delegationMember) {
        if (delegationMemberDoesNotReferenceSpecificRoleMember(delegationMember)) {
            initializeMinimalRoleBoForDelegationMember(delegationMember);
        }
        super.addDelegationMemberToDelegation(delegationMember);
    }

    protected boolean delegationMemberDoesNotReferenceSpecificRoleMember(
            RoleDocumentDelegationMember delegationMember) {
        return StringUtils.equals(KFSConstants.WILDCARD_CHARACTER, delegationMember.getRoleMemberId());
    }

    protected void initializeMinimalRoleBoForDelegationMember(RoleDocumentDelegationMember delegationMember) {
        Role role = delegationMember.getMemberRole();
        if (ObjectUtils.isNull(role)) {
            role = new Role();
        }
        role.setId(getRoleId());
        role.setKimTypeId(getRoleTypeId());
        delegationMember.setMemberRole(role);
    }

    public enum RoleMemberMetaDataType implements Comparator<KimDocumentRoleMember> {

        MEMBER_ID("memberId"),
        MEMBER_NAME("memberName"),
        FULL_MEMBER_NAME("memberFullName");

        private final String attributeName;

        RoleMemberMetaDataType(String anAttributeName) {
            this.attributeName = anAttributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public int compare(KimDocumentRoleMember m1, KimDocumentRoleMember m2) {
            if (m1 == null && m2 == null) {
                return 0;
            } else if (m1 == null) {
                return -1;
            } else if (m2 == null) {
                return 1;
            }
            if (this.getAttributeName().equals(MEMBER_ID.getAttributeName())) {
                return m1.getMemberId().compareToIgnoreCase(m2.getMemberId());
            } else if (this.getAttributeName().equals(FULL_MEMBER_NAME.getAttributeName())) {
                return m1.getMemberFullName().compareToIgnoreCase(m2.getMemberFullName());
            }
            return m1.getMemberName().compareToIgnoreCase(m2.getMemberName());
        }
    }
}
