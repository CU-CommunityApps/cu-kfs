package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.kuali.kfs.core.api.mo.common.active.Inactivatable;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SecurityProvisioningGroup extends PersistableBusinessObjectBase implements Inactivatable, KsrObjectWithRoles {
    
    private Long provisioningId;
    private Long securityGroupId;
    private String roleId;
    private Long roleTabOrder;
    private Long tabId;
    private String distributedAuthorizerRoleId;
    private String additionalAuthorizerRoleId;
    private String centralAuthorizerRoleId;
    private boolean active;

    private SecurityGroup securityGroup;
    private List<SecurityProvisioningGroupDependentRoles> dependentRoles;


    private SecurityGroupTab securityGroupTab;
    private Role role;
    private Role distributedAuthorizerRole;
    private Role additionalAuthorizerRole;
    private Role centralAuthorizerRole;

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

    public Role getRole() {
        initializeRoleBoIfNecessary(roleId, role, this::setRole);
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getRoleName() {
        Role roleImpl = getRole();
        return getFormattedRoleName(roleImpl);
    }

    public String getDistributedAuthorizerRoleId() {
        return distributedAuthorizerRoleId;
    }

    public void setDistributedAuthorizerRoleId(String distributedAuthorizerRoleId) {
        this.distributedAuthorizerRoleId = distributedAuthorizerRoleId;
    }

    public Role getDistributedAuthorizerRole() {
        initializeRoleBoIfNecessary(distributedAuthorizerRoleId, distributedAuthorizerRole, this::setDistributedAuthorizerRole);
        return distributedAuthorizerRole;
    }

    public void setDistributedAuthorizerRole(Role distributedAuthorizerRole) {
        this.distributedAuthorizerRole = distributedAuthorizerRole;
    }

    public String getDistributedAuthorizerRoleName() {
        Role roleImpl = getDistributedAuthorizerRole();
        return getFormattedRoleName(roleImpl);
    }

    public String getAdditionalAuthorizerRoleId() {
        return additionalAuthorizerRoleId;
    }

    public void setAdditionalAuthorizerRoleId(String additionalAuthorizerRoleId) {
        this.additionalAuthorizerRoleId = additionalAuthorizerRoleId;
    }

    public Role getAdditionalAuthorizerRole() {
        initializeRoleBoIfNecessary(additionalAuthorizerRoleId, additionalAuthorizerRole, this::setAdditionalAuthorizerRole);
        return additionalAuthorizerRole;
    }

    public void setAdditionalAuthorizerRole(Role additionalAuthorizerRole) {
        this.additionalAuthorizerRole = additionalAuthorizerRole;
    }

    public String getAdditionalAuthorizerRoleName() {
        Role roleImpl = getAdditionalAuthorizerRole();
        return getFormattedRoleName(roleImpl);
    }

    public String getCentralAuthorizerRoleId() {
        return centralAuthorizerRoleId;
    }

    public void setCentralAuthorizerRoleId(String centralAuthorizerRoleId) {
        this.centralAuthorizerRoleId = centralAuthorizerRoleId;
    }

    public Role getCentralAuthorizerRole() {
        initializeRoleBoIfNecessary(centralAuthorizerRoleId, centralAuthorizerRole, this::setCentralAuthorizerRole);
        return centralAuthorizerRole;
    }

    public void setCentralAuthorizerRole(Role centralAuthorizerRole) {
        this.centralAuthorizerRole = centralAuthorizerRole;
    }

    public String getCentralAuthorizerRoleName() {
        Role roleImpl = getCentralAuthorizerRole();
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
