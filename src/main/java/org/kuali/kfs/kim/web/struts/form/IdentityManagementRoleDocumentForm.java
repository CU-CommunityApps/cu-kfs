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
package org.kuali.kfs.kim.web.struts.form;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.core.web.format.DateDisplayTimestampObjectFormatter;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRolePermission;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibility;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kns.util.TableRenderUtil;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;

import java.util.List;

public class IdentityManagementRoleDocumentForm extends IdentityManagementDocumentFormBase {

    protected static final long serialVersionUID = 7099079353241080483L;
    protected String delegationMemberRoleMemberId;
    protected String dmrmi;
    protected boolean canAssignRole = true;
    protected boolean canModifyAssignees = true;
    protected KimType kimType;
    protected String memberSearchValue;
    //kim type id
    protected String id;
    protected KimDocumentRoleMember member;
    protected KimDocumentRolePermission permission = new KimDocumentRolePermission();
    protected KimDocumentRoleResponsibility responsibility = new KimDocumentRoleResponsibility();
    protected RoleDocumentDelegationMember delegationMember;
    protected String roleId;

    {
        requiredNonEditableProperties.add("methodToCall");
        requiredNonEditableProperties.add("roleCommand");
    }

    {
        member = new KimDocumentRoleMember();
        member.getQualifiers().add(new KimDocumentRoleQualifier());
    }

    {
        delegationMember = new RoleDocumentDelegationMember();
        delegationMember.getQualifiers().add(new RoleDocumentDelegationMemberQualifier());
    }

    public IdentityManagementRoleDocumentForm() {
        super();

        setFormatterType("document.members.activeFromDate", DateDisplayTimestampObjectFormatter.class);
        setFormatterType("document.delegationMembers.activeFromDate", DateDisplayTimestampObjectFormatter.class);
        setFormatterType("document.members.activeToDate", DateDisplayTimestampObjectFormatter.class);
        setFormatterType("document.delegationMembers.activeToDate", DateDisplayTimestampObjectFormatter.class);
    }

    public RoleDocumentDelegationMember getDelegationMember() {
        return this.delegationMember;
    }

    public void setDelegationMember(RoleDocumentDelegationMember delegationMember) {
        this.delegationMember = delegationMember;
    }

    @Override
    public String getDefaultDocumentTypeName() {
        return "IdentityManagementRoleDocument";
    }

    @Override
    public void setDocument(Document document) {
        if (document != null) {
            // Restore transient KimType on the document as it gets nuked during save, submit (etc?)
            IdentityManagementRoleDocument roleDocument = (IdentityManagementRoleDocument) document;
            
            //KFSPTS-28105: Start: Cornell Customization to fix Role documents created before 
            //the KEW-to-KFS Upgrade not opening after those code changes were applied.
            if (getKimType() == null) {
                setKimType(KimApiServiceLocator.getKimTypeInfoService().findKimTypeByNameAndNamespace(
                        KFSConstants.CoreModuleNamespaces.KFS,
                        KimConstants.KIM_TYPE_DEFAULT_NAME));
            }//KFSPTS-28105: End

            roleDocument.setKimType(getKimType());
        }
        super.setDocument(document);
    }

    public IdentityManagementRoleDocument getRoleDocument() {
        return (IdentityManagementRoleDocument) this.getDocument();
    }

    public KimDocumentRoleMember getMember() {
        return this.member;
    }

    public void setMember(KimDocumentRoleMember member) {
        this.member = member;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        setKimType(KimApiServiceLocator.getKimTypeInfoService().getKimType(this.id));
    }

    public KimDocumentRolePermission getPermission() {
        return this.permission;
    }

    public void setPermission(KimDocumentRolePermission permission) {
        this.permission = permission;
    }

    public KimDocumentRoleResponsibility getResponsibility() {
        return this.responsibility;
    }

    public void setResponsibility(KimDocumentRoleResponsibility responsibility) {
        this.responsibility = responsibility;
    }

    public String getDelegationMemberFieldConversions() {
        if (getDelegationMember() == null) {
            return "";
        }
        String memberTypeCode = getDelegationMember().getMemberTypeCode();
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            return "principalId:delegationMember.memberId,principalName:delegationMember.memberName";
        } else if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            return "id:delegationMember.memberId,name:delegationMember.memberName,namespaceCode:" +
                    "delegationMember.memberNamespaceCode";
        } else if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
            return "id:delegationMember.memberId,name:delegationMember.memberName,namespaceCode:" +
                    "delegationMember.memberNamespaceCode";
        }
        return "";
    }

    public String getDelegationMemberBusinessObjectName() {
        if (getDelegationMember() == null) {
            return "";
        }
        return getMemberBusinessObjectName(getDelegationMember().getMemberTypeCode());
    }

    public String getMemberFieldConversions() {
        if (member == null) {
            return "";
        }
        return getMemberFieldConversions(member.getMemberTypeCode());
    }

    protected String getMemberFieldConversions(String memberTypeCode) {
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            return "principalId:member.memberId,principalName:member.memberName";
        } else if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            return "id:member.memberId,name:member.memberName,namespaceCode:member.memberNamespaceCode";
        } else if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
            return "id:member.memberId,name:member.memberName,namespaceCode:member.memberNamespaceCode";
        }
        return "";
    }

    public String getMemberBusinessObjectName() {
        if (member == null) {
            return "";
        }
        return getMemberBusinessObjectName(member.getMemberTypeCode());
    }

    protected String getMemberBusinessObjectName(String memberTypeCode) {
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            return Person.class.getName();
        } else if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            return Role.class.getName();
        } else if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
            return Group.class.getName();
        }
        return "";
    }

    public KimType getKimType() {
        return this.kimType;
    }

    public void setKimType(KimType kimType) {
        this.kimType = kimType;
        if (kimType != null && getRoleDocument() != null) {
            getRoleDocument().setKimType(kimType);
        }
    }

    public boolean isCanAssignRole() {
        return this.canAssignRole;
    }

    public void setCanAssignRole(boolean canAssignRole) {
        this.canAssignRole = canAssignRole;
    }

    public boolean isCanModifyAssignees() {
        return this.canModifyAssignees;
    }

    public void setCanModifyAssignees(boolean canModifyAssignees) {
        this.canModifyAssignees = canModifyAssignees;
    }

    @Override
    public List<KimDocumentRoleMember> getMemberRows() {
        return getRoleDocument().getMembers();
    }

    public int getIndexOfRoleMemberFromMemberRows(String roleMemberId) {
        int index = 0;
        for (KimDocumentRoleMember roleMember : getMemberRows()) {
            if (StringUtils.equals(roleMember.getRoleMemberId(), roleMemberId)) {
                break;
            }
            index++;
        }
        return index;
    }

    public int getPageNumberOfRoleMemberId(String roleMemberId) {
        if (StringUtils.isEmpty(roleMemberId)) {
            return 1;
        }
        int index = getIndexOfRoleMemberFromMemberRows(roleMemberId);
        return TableRenderUtil.computeTotalNumberOfPages(index + 1, getRecordsPerPage()) - 1;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getDelegationMemberRoleMemberId() {
        return this.delegationMemberRoleMemberId;
    }

    public void setDelegationMemberRoleMemberId(String delegationMemberRoleMemberId) {
        this.delegationMemberRoleMemberId = delegationMemberRoleMemberId;
        getDelegationMember().setRoleMemberId(delegationMemberRoleMemberId);
        KimDocumentRoleMember roleMember = getRoleDocument().getMember(delegationMemberRoleMemberId);
        if (roleMember != null) {
            delegationMember.setRoleMemberId(roleMember.getRoleMemberId());
            delegationMember.setRoleMemberName(roleMember.getMemberName());
            delegationMember.setRoleMemberNamespaceCode(roleMember.getMemberNamespaceCode());
            RoleDocumentDelegationMemberQualifier delegationMemberQualifier;
            for (KimDocumentRoleQualifier roleQualifier : roleMember.getQualifiers()) {
                delegationMemberQualifier = getDelegationMember().getQualifier(roleQualifier.getKimAttrDefnId());
                delegationMemberQualifier.setAttrVal(roleQualifier.getAttrVal());
            }
        }
    }

    public String getDmrmi() {
        return this.dmrmi;
    }

    public void setDmrmi(String dmrmi) {
        this.dmrmi = dmrmi;
    }

    public String getMemberSearchValue() {
        return this.memberSearchValue;
    }

    public void setMemberSearchValue(String memberSearchValue) {
        this.memberSearchValue = memberSearchValue;
    }
}
