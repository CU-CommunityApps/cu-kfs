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
package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.context.SpringContext;

public class SecurityRequestRole extends PersistableBusinessObjectBase implements MutableInactivatable {
    private static final long serialVersionUID = -8781959491437530198L;

    private String documentNumber;
    private Long roleRequestId;
    private String roleId;
    private int nextQualificationId;
    private boolean active;
    private boolean currentActive;
    private String currentQualifications;

    private RoleLite roleInfo;

    private List<SecurityRequestRoleQualification> requestRoleQualifications;

    private SecurityRequestRoleQualification newRequestRoleQualification;

    public SecurityRequestRole() {
        super();

        nextQualificationId = 1;
        requestRoleQualifications = new ArrayList<SecurityRequestRoleQualification>();
    }
    
    public boolean isQualifiedRole() {
        if (getRoleInfo() != null) {
            KimType typeInfo = SpringContext.getBean(KimTypeInfoService.class).getKimType(getRoleInfo().getKimTypeId());

            if (typeInfo != null) {
                return (typeInfo.getAttributeDefinitions() != null) && !typeInfo.getAttributeDefinitions().isEmpty();
            }
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

    public RoleLite getRoleInfo() {
        if ((roleInfo == null) || (!StringUtils.equals(roleId, roleInfo.getId()))) {
            roleInfo = SpringContext.getBean(RoleService.class).getRoleWithoutMembers(roleId);
        }

        return roleInfo;
    }

    public void setRoleInfo(RoleLite roleInfo) {
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

}
