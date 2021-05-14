/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.rice.kim.impl.role.RoleBo;
import org.kuali.rice.krad.bo.DataObjectBase;

/**
 * ====
 * CU Customization:
 * Copied over the version of this class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@IdClass(SecurityProvisioningGroupDependentRolesId.class)
@Entity
@Table(name="KRSR_SEC_PRV_GRP_DEP_RL_T")
public class SecurityProvisioningGroupDependentRoles extends DataObjectBase implements KsrObjectWithRoles {

    private static final long serialVersionUID = 8290338935892653260L;

    @Id
    @Column(name="PRV_ID")
    private Long provisioningId;

    @Id
    @Column(name="RL_ID")
    private String roleId;

    @Transient
    private RoleBo role;

    public Long getProvisioningId() {
        return provisioningId;
    }

    public void setProvisioningId(Long provisioningId) {
        this.provisioningId = provisioningId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public RoleBo getRole() {
        initializeRoleBoIfNecessary(roleId, role, this::setRole);
        return role;
    }

    public void setRole(RoleBo role) {
        this.role = role;
    }

    public String getRoleName() {
        RoleBo roleImpl = getRole();
        return getFormattedRoleName(roleImpl);
    }

}
