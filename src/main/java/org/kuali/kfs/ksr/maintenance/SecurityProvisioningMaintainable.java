/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.ksr.maintenance;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.ksr.bo.SecurityProvisioning;
import org.kuali.kfs.ksr.bo.SecurityProvisioningGroup;
import org.kuali.kfs.ksr.bo.SecurityProvisioningGroupDependentRoles;
import org.kuali.rice.krad.maintenance.MaintainableImpl;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Overrides the default maintainable to help with manually entered dependent roles
 * 
 * @author rSmart Development Team
 */
public class SecurityProvisioningMaintainable extends MaintainableImpl {
    private static final long serialVersionUID = 1L;

    /**
     * Updates the security group IDs and provisioning IDs on the related
     * provisioning-group and dependent-role objects accordingly.
     * 
     * @see org.kuali.rice.krad.maintenance.MaintainableImpl#prepareForSave()
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
