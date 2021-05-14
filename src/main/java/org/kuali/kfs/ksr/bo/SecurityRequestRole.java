/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.kfs.ksr.bo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.mo.common.active.Inactivatable;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeInfoService;
import org.kuali.rice.krad.bo.DataObjectBase;
import org.kuali.rice.krad.data.jpa.converters.BooleanYNConverter;

/**
 * ====
 * CU Customization:
 * Updated as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@IdClass(SecurityRequestRoleId.class)
@Entity
@Table(name = "KRSR_SEC_RQ_RL_T")
public class SecurityRequestRole extends DataObjectBase implements Inactivatable {

    private static final long serialVersionUID = 157377630753870095L;

    @Id
    @Column(name = "FDOC_NBR")
    private String documentNumber;

    @Id
    @Column(name = "RL_RQ_ID")
    private Long roleRequestId;

    @Column(name = "RL_ID")
    private String roleId;

    @Column(name = "NEXT_QUAL_ID")
    private int nextQualificationId;

    @Column(name = "ACTV_IND")
    @Convert(converter=BooleanYNConverter.class)
    private boolean active;

    @Column(name = "CUR_ACTV_IND")
    @Convert(converter=BooleanYNConverter.class)
    private boolean currentActive;

    @Column(name = "CUR_QUAL")
    private String currentQualifications;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR"),
        @JoinColumn(name = "RL_RQ_ID", referencedColumnName = "RL_RQ_ID")
    })
    private List<SecurityRequestRoleQualification> requestRoleQualifications;

    @Transient
    private Role roleInfo;

    @Transient
    private SecurityRequestRoleQualification newRequestRoleQualification;

    public SecurityRequestRole() {
        nextQualificationId = 1;
        requestRoleQualifications = new ArrayList<>();
    }
    
    /**
     * Indicates whether the role type is qualified and should allow
     * qualification values to be given
     * 
     * @return boolean true if the role type is qualified, false if the role
     *         does not accept qualification
     */
    public boolean isQualifiedRole() {
        if (getRoleInfo() != null) {
            KimType typeInfo = getKimTypeInfoService().getKimType(getRoleInfo().getKimTypeId());
            return typeInfo != null && CollectionUtils.isNotEmpty(typeInfo.getAttributeDefinitions());
        }

        return false;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getRoleRequestId() {
        return this.roleRequestId;
    }

    public void setRoleRequestId(Long roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getNextQualificationId() {
        return this.nextQualificationId;
    }

    public void setNextQualificationId(int nextQualificationId) {
        this.nextQualificationId = nextQualificationId;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SecurityRequestRoleQualification> getRequestRoleQualifications() {
        return this.requestRoleQualifications;
    }

    public void setRequestRoleQualifications(List<SecurityRequestRoleQualification> requestRoleQualifications) {
        this.requestRoleQualifications = requestRoleQualifications;
    }

    public SecurityRequestRoleQualification getNewRequestRoleQualification() {
        return newRequestRoleQualification;
    }

    public void setNewRequestRoleQualification(SecurityRequestRoleQualification newRequestRoleQualification) {
        this.newRequestRoleQualification = newRequestRoleQualification;
    }

    public Role getRoleInfo() {
        if (StringUtils.isNotBlank(roleId) && (roleInfo == null || !StringUtils.equals(roleId, roleInfo.getId()))) {
            roleInfo = getRoleService().getRole(roleId);
        }

        return roleInfo;
    }

    public void setRoleInfo(Role roleInfo) {
        this.roleInfo = roleInfo;
    }

    public boolean isCurrentActive() {
        return currentActive;
    }

    public void setCurrentActive(boolean currentActive) {
        this.currentActive = currentActive;
    }

    public String getCurrentQualifications() {
        return currentQualifications;
    }

    public void setCurrentQualifications(String currentQualifications) {
        this.currentQualifications = currentQualifications;
    }

    public String getCurrentActiveFlagForDisplay() {
    	return (currentActive) ? "Currently Active" : "Currently Inactive";
    }

    public String getCurrentQualificationsForDisplay() {
    	return (StringUtils.isNotBlank(currentQualifications)) ? "Current Qualifications: " + currentQualifications : "";
    }

    protected RoleService getRoleService() {
        return KimApiServiceLocator.getRoleService();
    }

    protected KimTypeInfoService getKimTypeInfoService() {
        return KimApiServiceLocator.getKimTypeInfoService();
    }

}
