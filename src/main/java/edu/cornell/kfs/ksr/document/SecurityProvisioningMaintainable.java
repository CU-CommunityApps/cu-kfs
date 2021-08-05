package edu.cornell.kfs.ksr.document;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import edu.cornell.kfs.ksr.businessobject.SecurityProvisioning;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroupDependentRoles;

public class SecurityProvisioningMaintainable extends FinancialSystemMaintainable {
    
    /**
     * Updates the security group IDs and provisioning IDs on the related
     * provisioning-group and dependent-role objects accordingly.
     */
    @Override
    public void prepareForSave() {
        super.prepareForSave();
        
        SecurityProvisioning provisioning = (SecurityProvisioning) getDataObject();
        for (SecurityProvisioningGroup provisioningGroup : provisioning.getSecurityProvisioningGroups()) {
            provisioningGroup.setSecurityGroupId(provisioning.getSecurityGroupId());
            if (CollectionUtils.isNotEmpty(provisioningGroup.getDependentRoles())) {
                for (SecurityProvisioningGroupDependentRoles dependentRole : provisioningGroup.getDependentRoles()) {
                    dependentRole.setProvisioningId(provisioningGroup.getProvisioningId());
                }
            }
        }
    }

}
