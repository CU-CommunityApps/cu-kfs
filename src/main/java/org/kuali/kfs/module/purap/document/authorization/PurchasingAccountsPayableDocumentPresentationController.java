/*
 * Copyright 2008-2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.document.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.bo.impl.KimAttributes;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

public class PurchasingAccountsPayableDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    private static final String NON_AD_HOC_APPROVE_REQUEST_RECIPIENT_ROLE_NAME = "Non-Ad Hoc Approve Request Recipient";
    private static RoleService roleService;

    /**
     * None of the PURAP documents allowing editing by adhoc requests
     * 
     * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canEdit(org.kuali.rice.kns.document.Document)
     */
    @Override
    public boolean canEdit(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
    	WorkflowDocument kwf  = document.getDocumentHeader().getWorkflowDocument();
        //Adding this check so that the initiator will always be able to edit the document (before initial submission)
    	if (kwf.getInitiatorPrincipalId().equals(currentUser.getPrincipalId()) && (kwf.isInitiated() || kwf.isSaved()) ) {
        	return true;
        }

        if (document.getDocumentHeader().getWorkflowDocument().isAdHocRequested() && !hasNonAdhocRequests(document)) {
            return false;
        }

        return super.canEdit(document);
    }

    /**
     * Check to see if the current user has any non-adhoc requests, since if so they should be able to edit the doc
     * even if they have adhoc requests too. (See KFSPTS-750)
     * 
     * @param document Document with requests to check
     * @return boolean true if the user has non-adhoc requests, false otherwise
     */
    private boolean hasNonAdhocRequests(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(getRoleService().getRoleIdByName(KimConstants.KIM_GROUP_WORKFLOW_NAMESPACE_CODE, NON_AD_HOC_APPROVE_REQUEST_RECIPIENT_ROLE_NAME));

        Map<String,String> qualifications = new HashMap<String,String>();
        qualifications.put(KimAttributes.DOCUMENT_NUMBER, ""+document.getDocumentNumber());
              
        return getRoleService().principalHasRole(currentUser.getPrincipalId(), roleIds, qualifications);
    }

    protected RoleService getRoleService() {
        if ( roleService == null ) {
        	roleService = KimApiServiceLocator.getRoleService();
        }
        return roleService;
    }
    
    @Override
    public boolean canEditDocumentOverview(Document document) {
        // Change logic to allow editing document overview based on if user can edit the document
        return canEdit(document);
    }
  
    // KFSPTS-985
    protected boolean hasEmptyAcctline(PurchasingDocumentBase document) {
        boolean hasEmptyAcct = false;
        if (CollectionUtils.isNotEmpty(document.getItems())) {
    	    for (PurchasingItemBase item : (List<PurchasingItemBase>)document.getItems()) {
    		    if ((StringUtils.equals(item.getItemTypeCode(),ItemTypeCodes.ITEM_TYPE_ITEM_CODE) || StringUtils.equals(item.getItemTypeCode(),ItemTypeCodes.ITEM_TYPE_SERVICE_CODE) ) && CollectionUtils.isEmpty(item.getSourceAccountingLines())) {
    			    hasEmptyAcct = true;
    			    break;
    		    }
    	    }
        }
    	return hasEmptyAcct;
    }

}
