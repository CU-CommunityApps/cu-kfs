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
package org.kuali.kfs.kim.document;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.bo.ui.GroupDocumentMember;
import org.kuali.kfs.kim.bo.ui.GroupDocumentQualifier;
import org.kuali.kfs.kim.impl.type.IdentityManagementTypeAttributeTransactionalDocument;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.util.AutoPopulatingList;

import java.util.ArrayList;
import java.util.List;

/*
 * CU Customization:
 * Backported the FINP-7913 fix. This overlay can be removed when we upgrade to the 2021-10-14 patch or later.
 */
public class IdentityManagementGroupDocument extends IdentityManagementTypeAttributeTransactionalDocument {

    private static final long serialVersionUID = 1L;

    // principal data
    protected String groupId;

    protected String groupTypeId;

    protected String groupTypeName;

    protected String groupNamespace;

    protected String groupName;

    protected String groupDescription;

    protected boolean active = true;

    protected boolean editing;

    private List<GroupDocumentMember> members = new AutoPopulatingList<>(GroupDocumentMember.class);

    private List<GroupDocumentQualifier> qualifiers = new AutoPopulatingList<>(GroupDocumentQualifier.class);

    public IdentityManagementGroupDocument() {
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRoleId(String groupId) {
        this.groupId = groupId;
    }

    public void addMember(GroupDocumentMember member) {
        getMembers().add(member);
    }

    public KimType getKimType() {
        if (getGroupTypeId() != null) {
            return KimApiServiceLocator.getKimTypeInfoService().getKimType(getGroupTypeId());
        }
        return null;
    }

    public void setKimType(KimType kimType) {
        super.setKimType(kimType);
        if (kimType != null) {
            setGroupTypeId(kimType.getId());
            setGroupTypeName(kimType.getName());
        }
    }

    public GroupDocumentMember getBlankMember() {
        return new GroupDocumentMember();
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            KIMServiceLocatorInternal.getUiDocumentService().saveGroup(this);
        }
    }

    @Override
    public void prepareForSave() {
        String groupId;
        if (StringUtils.isBlank(getGroupId())) {
            SequenceAccessorService sas = getSequenceAccessorService();
            Long nextSeq = sas.getNextAvailableSequenceNumber("KRIM_GRP_ID_S", this.getClass());
            groupId = nextSeq.toString();
            setGroupId(groupId);
        } else {
            groupId = getGroupId();
        }
        if (getMembers() != null) {
            String groupMemberId;
            for (GroupDocumentMember member : getMembers()) {
                member.setGroupId(getGroupId());
                if (StringUtils.isBlank(member.getGroupMemberId())) {
                    SequenceAccessorService sas = getSequenceAccessorService();
                    Long nextSeq = sas.getNextAvailableSequenceNumber("KRIM_GRP_MBR_ID_S", this.getClass());
                    groupMemberId = nextSeq.toString();
                    member.setGroupMemberId(groupMemberId);
                }
                if (StringUtils.isBlank(member.getDocumentNumber())) {
                    member.setDocumentNumber(getDocumentNumber());
                }
            }
        }
        int index = 0;
        // this needs to be checked - are all qualifiers present?
        if (getDefinitions() != null) {
            for (KimAttributeField key : getDefinitions()) {
                if (getQualifiers().size() > index) {
                    GroupDocumentQualifier qualifier = getQualifiers().get(index);
                    qualifier.setKimAttrDefnId(getKimAttributeDefnId(key));
                    qualifier.setKimTypId(getKimType().getId());
                    qualifier.setGroupId(groupId);
                    qualifier.setDocumentNumber(getDocumentNumber());
                }
                index++;
            }
        }
    }

    public void initializeDocumentForNewGroup() {
        if (StringUtils.isBlank(this.groupId)) {
            SequenceAccessorService sas = getSequenceAccessorService();
            Long nextSeq = sas.getNextAvailableSequenceNumber(KimConstants.SequenceNames.KRIM_GROUP_ID_S,
                    this.getClass());
            this.groupId = nextSeq.toString();
        }
        if (StringUtils.isBlank(this.groupTypeId)) {
            /*
             * CU Customization: Backport the FINP-7913 fix.
             */
            final KimType defaultKimType = KimApiServiceLocator.getKimTypeInfoService().findKimTypeByNameAndNamespace(
                    KFSConstants.CoreModuleNamespaces.KFS,
                    KimConstants.KIM_TYPE_DEFAULT_NAME
            );
            if (defaultKimType != null) {
                groupTypeId = defaultKimType.getId();
            }
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return this.groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupNamespace() {
        return this.groupNamespace;
    }

    public void setGroupNamespace(String groupNamespace) {
        this.groupNamespace = groupNamespace;
    }

    public String getGroupTypeId() {
        return this.groupTypeId;
    }

    public void setGroupTypeId(String groupTypeId) {
        this.groupTypeId = groupTypeId;
    }

    public String getGroupTypeName() {
        return this.groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName;
    }

    public List<GroupDocumentMember> getMembers() {
        return this.members;
    }

    public void setMembers(List<GroupDocumentMember> members) {
        this.members = members;
    }

    public List<GroupDocumentQualifier> getQualifiers() {
        return this.qualifiers;
    }

    public void setQualifiers(List<GroupDocumentQualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public GroupDocumentQualifier getQualifier(String kimAttributeDefnId) {
        for (GroupDocumentQualifier qualifier : qualifiers) {
            if (qualifier.getKimAttrDefnId().equals(kimAttributeDefnId)) {
                return qualifier;
            }
        }
        return null;
    }

    public void setDefinitions(List<KimAttributeField> definitions) {
        super.setDefinitions(definitions);
        if (getQualifiers() == null || getQualifiers().size() < 1) {
            GroupDocumentQualifier qualifier;
            setQualifiers(new ArrayList<>());
            if (getDefinitions() != null) {
                for (KimAttributeField key : getDefinitions()) {
                    qualifier = new GroupDocumentQualifier();
                    qualifier.setKimAttrDefnId(getKimAttributeDefnId(key));
                    getQualifiers().add(qualifier);
                }
            }
        }
    }

    public boolean isEditing() {
        return this.editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
