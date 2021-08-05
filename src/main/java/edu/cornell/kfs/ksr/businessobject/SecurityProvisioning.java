package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SecurityProvisioning extends PersistableBusinessObjectBase {

    private Long securityGroupId;

    private List<SecurityProvisioningGroup> securityProvisioningGroups;

    private SecurityGroup securityGroup;

    public SecurityProvisioning() {
        securityProvisioningGroups = new ArrayList<>();
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public List<SecurityProvisioningGroup> getSecurityProvisioningGroups() {
        return securityProvisioningGroups;
    }

    public void setSecurityProvisioningGroups(List<SecurityProvisioningGroup> securityProvisioningGroups) {
        this.securityProvisioningGroups = securityProvisioningGroups;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

}
