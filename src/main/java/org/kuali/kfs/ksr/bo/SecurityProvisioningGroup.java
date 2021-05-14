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
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.rice.core.api.mo.common.active.Inactivatable;
import org.kuali.rice.kim.impl.role.RoleBo;
import org.kuali.rice.krad.bo.DataObjectBase;
import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;
import org.kuali.rice.krad.data.jpa.converters.BooleanYNConverter;

/**
 * ====
 * CU Customization:
 * Copied over the SecurityProvisioningGroup class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@Entity
@Table(name = "KRSR_SEC_PRV_GRP_T")
public class SecurityProvisioningGroup extends DataObjectBase implements Inactivatable, KsrObjectWithRoles {

    private static final long serialVersionUID = -2489238745962499259L;

    @Id
    @GeneratedValue(generator = "KRSR_SEC_PRV_ID_SEQ")
    @PortableSequenceGenerator(name = "KRSR_SEC_PRV_ID_SEQ")
    @Column(name = "PRV_ID")
    private Long provisioningId;

    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @Column(name = "RL_ID")
    private String roleId;

    @Column(name = "RL_TB_ORD")
    private Long roleTabOrder;

    @Column(name = "TB_ID")
    private Long tabId;

    @Column(name = "DI_AUTH_RL_ID")
    private String distributedAuthorizerRoleId;

    @Column(name = "ADDL_AUTH_RL_ID")
    private String additionalAuthorizerRoleId;

    @Column(name = "CNTRL_AUTH_RL_ID")
    private String centralAuthorizerRoleId;

    @Column(name = "ACTV_IND")
    @Convert(converter=BooleanYNConverter.class)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEC_GRP_ID", insertable = false, updatable = false)
    private SecurityGroup securityGroup;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PRV_ID")
    private List<SecurityProvisioningGroupDependentRoles> dependentRoles;

    @Transient
    private SecurityGroupTab securityGroupTab;

    @Transient
    private RoleBo role;

    @Transient
    private RoleBo distributedAuthorizerRole;

    @Transient
    private RoleBo additionalAuthorizerRole;

    @Transient
    private RoleBo centralAuthorizerRole;

    public SecurityProvisioningGroup() {
        dependentRoles = new ArrayList<>();
        active = true;
    }

    public Long getProvisioningId() {
        return provisioningId;
    }

    public void setProvisioningId(Long provisioningId) {
        this.provisioningId = provisioningId;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public Long getRoleTabOrder() {
        return roleTabOrder;
    }

    public void setRoleTabOrder(Long roleTabOrder) {
        this.roleTabOrder = roleTabOrder;
    }

    public Long getTabId() {
        return tabId;
    }

    public void setTabId(Long tabId) {
        this.tabId = tabId;
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

    public String getDistributedAuthorizerRoleId() {
        return distributedAuthorizerRoleId;
    }

    public void setDistributedAuthorizerRoleId(String distributedAuthorizerRoleId) {
        this.distributedAuthorizerRoleId = distributedAuthorizerRoleId;
    }

    public RoleBo getDistributedAuthorizerRole() {
        initializeRoleBoIfNecessary(distributedAuthorizerRoleId, distributedAuthorizerRole, this::setDistributedAuthorizerRole);
        return distributedAuthorizerRole;
    }

    public void setDistributedAuthorizerRole(RoleBo distributedAuthorizerRole) {
        this.distributedAuthorizerRole = distributedAuthorizerRole;
    }

    public String getDistributedAuthorizerRoleName() {
        RoleBo roleImpl = getDistributedAuthorizerRole();
        return getFormattedRoleName(roleImpl);
    }

    public String getAdditionalAuthorizerRoleId() {
        return additionalAuthorizerRoleId;
    }

    public void setAdditionalAuthorizerRoleId(String additionalAuthorizerRoleId) {
        this.additionalAuthorizerRoleId = additionalAuthorizerRoleId;
    }

    public RoleBo getAdditionalAuthorizerRole() {
        initializeRoleBoIfNecessary(additionalAuthorizerRoleId, additionalAuthorizerRole, this::setAdditionalAuthorizerRole);
        return additionalAuthorizerRole;
    }

    public void setAdditionalAuthorizerRole(RoleBo additionalAuthorizerRole) {
        this.additionalAuthorizerRole = additionalAuthorizerRole;
    }

    public String getAdditionalAuthorizerRoleName() {
        RoleBo roleImpl = getAdditionalAuthorizerRole();
        return getFormattedRoleName(roleImpl);
    }

    public String getCentralAuthorizerRoleId() {
        return centralAuthorizerRoleId;
    }

    public void setCentralAuthorizerRoleId(String centralAuthorizerRoleId) {
        this.centralAuthorizerRoleId = centralAuthorizerRoleId;
    }

    public RoleBo getCentralAuthorizerRole() {
        initializeRoleBoIfNecessary(centralAuthorizerRoleId, centralAuthorizerRole, this::setCentralAuthorizerRole);
        return centralAuthorizerRole;
    }

    public void setCentralAuthorizerRole(RoleBo centralAuthorizerRole) {
        this.centralAuthorizerRole = centralAuthorizerRole;
    }

    public String getCentralAuthorizerRoleName() {
        RoleBo roleImpl = getCentralAuthorizerRole();
        return getFormattedRoleName(roleImpl);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SecurityProvisioningGroupDependentRoles> getDependentRoles() {
        return dependentRoles;
    }

    public void setDependentRoles(List<SecurityProvisioningGroupDependentRoles> dependentRoles) {
        this.dependentRoles = dependentRoles;
    }

    public SecurityGroupTab getSecurityGroupTab() {
        if (tabId == null) {
            if (securityGroupTab != null) {
                setSecurityGroupTab(null);
            }
        } else if ((securityGroupTab == null || !tabId.equals(securityGroupTab.getTabId())) && securityGroup != null) {
            Optional<SecurityGroupTab> matchingTab = securityGroup.getSecurityGroupTabs()
                    .stream()
                    .filter((tab) -> tabId.equals(tab.getTabId()))
                    .findFirst();
            matchingTab.ifPresent(this::setSecurityGroupTab);
        }
        return securityGroupTab;
    }

    public void setSecurityGroupTab(SecurityGroupTab securityGroupTab) {
        this.securityGroupTab = securityGroupTab;
    }

}
