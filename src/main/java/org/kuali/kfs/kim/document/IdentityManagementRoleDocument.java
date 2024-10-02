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
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(final String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRoleNamespace() {
        return roleNamespace;
    }

    public void setRoleNamespace(final String roleNamespace) {
        this.roleNamespace = roleNamespace;
    }

    public String getRoleTypeId() {
        return roleTypeId;
    }

    public void setRoleTypeId(final String roleTypeId) {
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
        return roleTypeName;
    }

    public void setRoleTypeName(final String roleTypeName) {
        this.roleTypeName = roleTypeName;
    }

    @Override
    public List<RoleDocumentDelegationMember> getDelegationMembers() {
        return delegationMembers;
    }

    @Override
    public void setDelegationMembers(final List<RoleDocumentDelegationMember> delegationMembers) {
        this.delegationMembers = delegationMembers;
    }

    public List<KimDocumentRolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<KimDocumentRolePermission> permissions) {
        this.permissions = permissions;
    }

    public List<KimDocumentRoleResponsibility> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(final List<KimDocumentRoleResponsibility> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<KimDocumentRoleMember> getMembers() {
        return members;
    }

    public void setMembers(final List<KimDocumentRoleMember> members) {
        this.members = members;
    }

    public void setMemberMetaDataTypeToSort(final Integer columnNumber) {
        switch (columnNumber) {
            case 1:
                memberMetaDataType = RoleMemberMetaDataType.MEMBER_ID;
                break;
            case 3:
                memberMetaDataType = RoleMemberMetaDataType.FULL_MEMBER_NAME;
                break;
            case 2:
            default:
                memberMetaDataType = RoleMemberMetaDataType.MEMBER_NAME;
                break;
        }
    }

    public RoleMemberMetaDataType getMemberMetaDataType() {
        return memberMetaDataType;
    }

    public void setMemberMetaDataType(final RoleMemberMetaDataType memberMetaDataType) {
        this.memberMetaDataType = memberMetaDataType;
    }

    public KimDocumentRoleMember getMember(final String roleMemberId) {
        if (StringUtils.isEmpty(roleMemberId)) {
            return null;
        }
        for (final KimDocumentRoleMember roleMember : getMembers()) {
            if (roleMemberId.equals(roleMember.getRoleMemberId())) {
                return roleMember;
            }
        }
        return null;
    }

    public List<KimDocumentRoleMember> getModifiedMembers() {
        return modifiedMembers;
    }

    public void setModifiedMembers(final List<KimDocumentRoleMember> modifiedMembers) {
        this.modifiedMembers = modifiedMembers;
    }

    public List<KimDocumentRoleMember> getSearchResultMembers() {
        return searchResultMembers;
    }

    public void setSearchResultMembers(final List<KimDocumentRoleMember> searchResultMembers) {
        this.searchResultMembers = searchResultMembers;
    }

    public void addResponsibility(final KimDocumentRoleResponsibility roleResponsibility) {
        if (!getResponsibilityInternalService().areActionsAtAssignmentLevelById(
                roleResponsibility.getResponsibilityId())) {
            roleResponsibility.getRoleRspActions().add(getNewRespAction(roleResponsibility));
        }
        getResponsibilities().add(roleResponsibility);
    }

    protected KimDocumentRoleResponsibilityAction getNewRespAction(final KimDocumentRoleResponsibility roleResponsibility) {
        final KimDocumentRoleResponsibilityAction roleRspAction = new KimDocumentRoleResponsibilityAction();
        roleRspAction.setKimResponsibility(roleResponsibility.getKimResponsibility());
        roleRspAction.setRoleResponsibilityId(roleResponsibility.getRoleResponsibilityId());
        return roleRspAction;
    }

    public void addDelegationMember(final RoleDocumentDelegationMember newDelegationMember) {
        getDelegationMembers().add(newDelegationMember);
    }

    public void addMember(final KimDocumentRoleMember member) {
        final SequenceAccessorService sas = getSequenceAccessorService();
        final Long nextSeq = sas.getNextAvailableSequenceNumber(
                KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S,
                KimDocumentRoleMember.class);
        final String roleMemberId = nextSeq.toString();
        member.setRoleMemberId(roleMemberId);
        setupMemberRspActions(member);

        if (ObjectUtils.isNull(member.getActiveFromDate())) {
            member.setActiveFromDate(getDateTimeService().getCurrentTimestamp());
        }

        getModifiedMembers().add(member);
    }

    public KimDocumentRoleMember getBlankMember() {
        final KimDocumentRoleMember member = new KimDocumentRoleMember();
        KimDocumentRoleQualifier qualifier;
        if (getDefinitions() != null) {
            for (final KimAttributeField key : getDefinitions()) {
                qualifier = new KimDocumentRoleQualifier();
                qualifier.setKimAttrDefnId(getKimAttributeDefnId(key));
                member.getQualifiers().add(qualifier);
            }
        }
        setupMemberRspActions(member);
        return member;
    }

    public RoleDocumentDelegationMember getBlankDelegationMember() {
        final RoleDocumentDelegationMember member = new RoleDocumentDelegationMember();
        RoleDocumentDelegationMemberQualifier qualifier;
        if (getDefinitions() != null) {
            for (final KimAttributeField key : getDefinitions()) {
                qualifier = new RoleDocumentDelegationMemberQualifier();
                setAttrDefnIdForDelMemberQualifier(qualifier, key);
                member.getQualifiers().add(qualifier);
            }
        }
        return member;
    }

    public void setupMemberRspActions(final KimDocumentRoleMember member) {
        member.getRoleRspActions().clear();
        for (final KimDocumentRoleResponsibility roleResp : getResponsibilities()) {
            if (getResponsibilityInternalService().areActionsAtAssignmentLevelById(roleResp.getResponsibilityId())) {
                final KimDocumentRoleResponsibilityAction action = new KimDocumentRoleResponsibilityAction();
                action.setRoleResponsibilityId("*");
                action.setRoleMemberId(member.getRoleMemberId());
                member.getRoleRspActions().add(action);
                break;
            }
        }
    }

    public void setupMemberRspActions(final KimDocumentRoleResponsibility roleResp, final KimDocumentRoleMember member) {
        if ((member.getRoleRspActions() == null || member.getRoleRspActions().size() < 1)
                && getResponsibilityInternalService().areActionsAtAssignmentLevelById(roleResp.getResponsibilityId())) {
            final KimDocumentRoleResponsibilityAction action = new KimDocumentRoleResponsibilityAction();
            action.setRoleResponsibilityId("*");
            action.setRoleMemberId(member.getRoleMemberId());
            if (member.getRoleRspActions() == null) {
                member.setRoleRspActions(new ArrayList<>());
            }
            member.getRoleRspActions().add(action);
        }
    }

    public void updateMembers(final IdentityManagementRoleDocumentForm roleDocumentForm) {
        for (final KimDocumentRoleMember member : roleDocumentForm.getRoleDocument().getMembers()) {
            roleDocumentForm.getRoleDocument().setupMemberRspActions(member);
        }
    }

    public void updateMembers(final KimDocumentRoleResponsibility newResponsibility) {
        for (final KimDocumentRoleMember member : getMembers()) {
            setupMemberRspActions(newResponsibility, member);
        }
    }

    protected void setAttrDefnIdForDelMemberQualifier(
            final RoleDocumentDelegationMemberQualifier qualifier,
            final KimAttributeField definition) {
        qualifier.setKimAttrDefnId(definition.getId());
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            KIMServiceLocatorInternal.getUiDocumentService().saveRole(this);
        }
    }

    public void initializeDocumentForNewRole() {
        if (StringUtils.isBlank(roleId)) {
            final SequenceAccessorService sas = getSequenceAccessorService();
            final Long nextSeq = sas.getNextAvailableSequenceNumber(
                    KimConstants.SequenceNames.KRIM_ROLE_ID_S, getClass());
            roleId = nextSeq.toString();
        }
        if (StringUtils.isBlank(roleTypeId)) {
            roleTypeId = "1";
        }
    }

    public String getRoleId() {
        if (StringUtils.isBlank(roleId)) {
            initializeDocumentForNewRole();
        }
        return roleId;
    }

    public void setRoleId(final String roleId) {
        this.roleId = roleId;
    }

    @Override
    public void prepareForSave() {
        super.prepareForSave();

        final SequenceAccessorService sas = getSequenceAccessorService();

        final String roleId;
        if (StringUtils.isBlank(getRoleId())) {
            final Long nextSeq = sas.getNextAvailableSequenceNumber(
                    KimConstants.SequenceNames.KRIM_ROLE_ID_S, getClass());
            roleId = nextSeq.toString();
            setRoleId(roleId);
        } else {
            roleId = getRoleId();
        }
        if (getPermissions() != null) {
            String rolePermissionId;
            for (final KimDocumentRolePermission permission : getPermissions()) {
                permission.setRoleId(roleId);
                permission.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isBlank(permission.getRolePermissionId())) {
                    final Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S,
                            KimDocumentRolePermission.class);
                    rolePermissionId = nextSeq.toString();
                    permission.setRolePermissionId(rolePermissionId);
                }
            }
        }
        if (getResponsibilities() != null) {
            String roleResponsibilityId;
            for (final KimDocumentRoleResponsibility responsibility: getResponsibilities()) {
                if (StringUtils.isBlank(responsibility.getRoleResponsibilityId())) {
                    final Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_RSP_ID_S,
                            KimDocumentRoleResponsibility.class);
                    roleResponsibilityId = nextSeq.toString();
                    responsibility.setRoleResponsibilityId(roleResponsibilityId);
                }
                responsibility.setRoleId(roleId);
                if (!getResponsibilityInternalService().areActionsAtAssignmentLevelById(responsibility.getResponsibilityId())) {
                    if (StringUtils.isBlank(responsibility.getRoleRspActions().get(0).getRoleResponsibilityActionId())) {
                        final Long nextSeq = sas.getNextAvailableSequenceNumber(
                                KimConstants.SequenceNames.KRIM_ROLE_RSP_ACTN_ID_S,
                                KimDocumentRoleResponsibilityAction.class);
                        final String roleResponsibilityActionId = nextSeq.toString();
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
            for (final KimDocumentRoleMember member : getModifiedMembers()) {
                member.setDocumentNumber(getDocumentNumber());
                member.setRoleId(roleId);
                if (StringUtils.isBlank(member.getRoleMemberId())) {
                    final Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S,
                            KimDocumentRoleMember.class);
                    roleMemberId = nextSeq.toString();
                    member.setRoleMemberId(roleMemberId);
                }
                for (final KimDocumentRoleQualifier qualifier : member.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setRoleMemberId(member.getRoleMemberId());
                    qualifier.setKimTypId(getKimType().getId());
                }
                for (final KimDocumentRoleResponsibilityAction roleRespAction : member.getRoleRspActions()) {
                    if (StringUtils.isBlank(roleRespAction.getRoleResponsibilityActionId())) {
                        final Long nextSeq = sas.getNextAvailableSequenceNumber(
                                KimConstants.SequenceNames.KRIM_ROLE_RSP_ACTN_ID_S,
                                KimDocumentRoleResponsibilityAction.class);
                        roleResponsibilityActionId = nextSeq.toString();
                        roleRespAction.setRoleResponsibilityActionId(roleResponsibilityActionId);
                    }
                    roleRespAction.setRoleMemberId(member.getRoleMemberId());
                    roleRespAction.setDocumentNumber(getDocumentNumber());
                    if (!StringUtils.equals(roleRespAction.getRoleResponsibilityId(), "*")) {
                        for (final KimDocumentRoleResponsibility responsibility : getResponsibilities()) {
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
            for (final RoleDocumentDelegationMember delegationMember : getDelegationMembers()) {
                delegationMember.setDocumentNumber(getDocumentNumber());
                addDelegationMemberToDelegation(delegationMember);
            }
            for (final RoleDocumentDelegation delegation : getDelegations()) {
                delegation.setDocumentNumber(getDocumentNumber());
                delegation.setKimTypeId(getKimType().getId());
                final List<RoleDocumentDelegationMember> membersToRemove =
                        new AutoPopulatingList<>(RoleDocumentDelegationMember.class);
                for (final RoleDocumentDelegationMember member : delegation.getMembers()) {
                    if (delegation.getDelegationId().equals(member.getDelegationId())
                            && delegation.getDelegationTypeCode().equals(member.getDelegationTypeCode())) {
                        for (final RoleDocumentDelegationMemberQualifier qualifier : member.getQualifiers()) {
                            qualifier.setKimTypId(getKimType().getId());
                            qualifier.setDocumentNumber(getDocumentNumber());
                        }
                    } else {
                        membersToRemove.add(member);
                    }
                }
                if (!membersToRemove.isEmpty()) {
                    for (final RoleDocumentDelegationMember member : membersToRemove) {
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
        return editing;
    }

    public void setEditing(final boolean editing) {
        this.editing = editing;
    }

    @Override
    public List<RoleDocumentDelegation> getDelegations() {
        return delegations;
    }

    @Override
    public void setDelegations(final List<RoleDocumentDelegation> delegations) {
        this.delegations = delegations;
    }

    @Override
    public void setKimType(final KimType kimType) {
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

        RoleMemberMetaDataType(final String anAttributeName) {
            attributeName = anAttributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public int compare(final KimDocumentRoleMember m1, final KimDocumentRoleMember m2) {
            if (m1 == null && m2 == null) {
                return 0;
            } else if (m1 == null) {
                return -1;
            } else if (m2 == null) {
                return 1;
            }
            if (getAttributeName().equals(MEMBER_ID.getAttributeName())) {
                return m1.getMemberId().compareToIgnoreCase(m2.getMemberId());
            } else if (getAttributeName().equals(FULL_MEMBER_NAME.getAttributeName())) {
                return m1.getMemberFullName().compareToIgnoreCase(m2.getMemberFullName());
            }
            return m1.getMemberName().compareToIgnoreCase(m2.getMemberName());
        }
    }
}
