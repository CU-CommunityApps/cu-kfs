/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.kim.bo.ui;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.type.KimTypeBo;
import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/*
 * CU Customization:
 * Updated this BO to have a non-persisted "editable" flag, similar to the one on PersonDocumentRole objects.
 */
@Entity
@Table(name = "KRIM_PND_GRP_PRNCPL_MT")
public class PersonDocumentGroup extends KimDocumentBoActivatableToFromEditableBase {

    private static final long serialVersionUID = -1551337026170706411L;

    @PortableSequenceGenerator(name = "KRIM_GRP_MBR_ID_S")
    @GeneratedValue(generator = "KRIM_GRP_MBR_ID_S")
    @Id
    @Column(name = "GRP_MBR_ID")
    protected String groupMemberId;

    @Column(name = "GRP_TYPE")
    protected String groupType;

    @Column(name = "GRP_ID")
    protected String groupId;

    @Column(name = "GRP_NM")
    protected String groupName;

    @Column(name = "NMSPC_CD")
    protected String namespaceCode;

    @Column(name = "PRNCPL_ID")
    protected String principalId;

    @Transient
    protected transient KimTypeBo kimGroupType;

    @Transient
    protected String kimTypeId;

    @Transient
    protected boolean editable = true;

    public String getGroupId() {
        return this.groupId;
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

    public KimTypeBo getKimGroupType() {
        if (StringUtils.isNotBlank(getKimTypeId())) {
            if (kimGroupType == null || (!StringUtils.equals(kimGroupType.getId(), kimTypeId))) {
                kimGroupType = KimTypeBo.from(KimApiServiceLocator.getKimTypeInfoService().getKimType(kimTypeId));
            }
        }
        return kimGroupType;
    }

    public String getKimTypeId() {
        return this.kimTypeId;
    }

    public void setKimTypeId(String kimTypeId) {
        this.kimTypeId = kimTypeId;
    }

    public String getGroupMemberId() {
        return this.groupMemberId;
    }

    public void setGroupMemberId(String groupMemberId) {
        this.groupMemberId = groupMemberId;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getGroupType() {
        return this.groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getNamespaceCode() {
        return this.namespaceCode;
    }

    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
