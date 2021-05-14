package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.kuali.rice.krad.data.jpa.IdClassBase;

/**
 * ====
 * CU Customization:
 * Added this dependent roles ID class as part of
 * the KSR module's JPA conversion.
 * ====
 */
public class SecurityProvisioningGroupDependentRolesId extends IdClassBase {

    private static final long serialVersionUID = 5655513919120544782L;

    @Id
    @Column(name="PRV_ID")
    private Long provisioningId;

    @Id
    @Column(name="RL_ID")
    private String roleId;

    public SecurityProvisioningGroupDependentRolesId() {
    }

    public SecurityProvisioningGroupDependentRolesId(Long provisioningId, String roleId) {
        this.provisioningId = provisioningId;
        this.roleId = roleId;
    }

    public Long getProvisioningId() {
        return provisioningId;
    }

    public String getRoleId() {
        return roleId;
    }

}
