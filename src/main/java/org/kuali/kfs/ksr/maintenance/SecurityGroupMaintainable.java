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
package org.kuali.kfs.ksr.maintenance;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityGroupTab;
import org.kuali.kfs.ksr.bo.SecurityProvisioning;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.data.DataObjectService;
import org.kuali.rice.krad.data.platform.MaxValueIncrementerFactory;
import org.kuali.rice.krad.maintenance.MaintainableImpl;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.KRADUtils;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
public class SecurityGroupMaintainable extends MaintainableImpl {
    private static final long serialVersionUID = 1L;

    /**
     * Overridden to also change the tab IDs on the copied SecurityGroupTab sub-objects.
     * 
     * @see org.kuali.rice.krad.maintenance.MaintainableImpl#processAfterCopy(
     * org.kuali.rice.krad.maintenance.MaintenanceDocument, java.util.Map)
     */
    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.processAfterCopy(document, parameters);

        SecurityGroup securityGroup = (SecurityGroup) document.getNewMaintainableObject().getDataObject();

        if (CollectionUtils.isNotEmpty(securityGroup.getSecurityGroupTabs())) {
            DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                    KSRServiceLocator.getDataSource(), KsrConstants.SECURITY_GROUP_TAB_SEQ_NAME);
            
            for (SecurityGroupTab securityGroupTab : securityGroup.getSecurityGroupTabs()) {
                Long newId = Long.valueOf(incrementer.nextLongValue());
                securityGroupTab.setTabId(newId);
            }
        }
    }

    /**
     * Updates the security group IDs on the SecurityGroupTab objects accordingly.
     * 
     * @see org.kuali.rice.krad.maintenance.MaintainableImpl#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        super.prepareForSave();
        
        SecurityGroup securityGroup = (SecurityGroup) getDataObject();
        for (SecurityGroupTab securityGroupTab : securityGroup.getSecurityGroupTabs()) {
            securityGroupTab.setSecurityGroupId(securityGroup.getSecurityGroupId());
        }
    }

    /**
     * Add an empty SecurityProvisioning object to the db for all new SecurityGroup objects
     * 
     * @see org.kuali.rice.krad.maintenance.MaintainableImpl#doRouteStatusChange(org.kuali.rice.krad.bo.DocumentHeader)
     */
    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);

        SecurityGroup securityGroup = (SecurityGroup) getDataObject();

        SecurityProvisioning existingSecurityProvisioning = getDataObjectService().find(
                SecurityProvisioning.class, securityGroup.getSecurityGroupId());

        if (KRADUtils.isNull(existingSecurityProvisioning)) {
            SecurityProvisioning securityProvisioning = new SecurityProvisioning();
            securityProvisioning.setSecurityGroupId(securityGroup.getSecurityGroupId());

            getDataObjectService().save(securityProvisioning);
        }
    }

    protected DataObjectService getDataObjectService() {
        return KRADServiceLocator.getDataObjectService();
    }

}
