package edu.cornell.kfs.ksr.businessobject;

import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SecurityProvisioningGroupDependentRoles extends PersistableBusinessObjectBase implements KsrObjectWithRoles {

    private static final long serialVersionUID = 8290338935892653260L;

    private Long provisioningId;

    private String roleId;

    private Role role;

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

}
