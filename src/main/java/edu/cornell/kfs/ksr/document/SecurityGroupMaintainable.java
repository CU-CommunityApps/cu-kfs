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
package edu.cornell.kfs.ksr.document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioning;

public class SecurityGroupMaintainable extends FinancialSystemMaintainable {

    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {        
        super.processAfterCopy(document, parameters);

        SecurityGroup securityGroup = (SecurityGroup) document.getNewMaintainableObject().getDataObject();

        if (CollectionUtils.isNotEmpty(securityGroup.getSecurityGroupTabs())) {
            SequenceAccessorService sas = SpringContext.getBean(SequenceAccessorService.class);
                    
            for (SecurityGroupTab securityGroupTab : securityGroup.getSecurityGroupTabs()) {
                Long newId = sas.getNextAvailableSequenceNumber(KSRConstants.SECURITY_GROUP_TAB_SEQ_NAME);
                securityGroupTab.setTabId(newId);
            }
        }
    }
    
    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);

        WorkflowDocument workflowDoc = documentHeader.getWorkflowDocument();

        if (workflowDoc.isProcessed()) {
            SecurityGroup securityGroup = (SecurityGroup) getDataObject();
            createSecurityProvisioningEntryIfItDoesNotExist(securityGroup);
        }
    }
          
    
    private void createSecurityProvisioningEntryIfItDoesNotExist(SecurityGroup securityGroup) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(KSRPropertyConstants.SECURITY_GROUP_ID, securityGroup.getSecurityGroupId());
        Collection<SecurityProvisioning> securityProvisioningDB = getBusinessObjectService().findMatching(SecurityProvisioning.class, hashMap);

        // if record doesn't exist, create it
        if (CollectionUtils.isEmpty(securityProvisioningDB)) {
            SecurityProvisioning securityProvisioning = new SecurityProvisioning();
            securityProvisioning.setSecurityGroupId(securityGroup.getSecurityGroupId());

            getBusinessObjectService().save(securityProvisioning);
        }
    }

}
