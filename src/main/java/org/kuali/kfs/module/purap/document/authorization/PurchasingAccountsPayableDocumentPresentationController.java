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

import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.impl.KimAttributes;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.RoleService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.GlobalVariables;


public class PurchasingAccountsPayableDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    private static final String NON_AD_HOC_APPROVE_REQUEST_RECIPIENT_ROLE_NAME = "Non-Ad Hoc Approve Request Recipient";
    private static RoleService roleService;

    /**
     * None of the PURAP documents allowing editing by adhoc requests
     * 
     * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canEdit(org.kuali.rice.kns.document.Document)
     */
    @Override
    protected boolean canEdit(Document document) {
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

        AttributeSet qualifications = new AttributeSet();
        qualifications.put(KimAttributes.DOCUMENT_NUMBER, ""+document.getDocumentNumber());
              
        return getRoleService().principalHasRole(currentUser.getPrincipalId(), roleIds, qualifications);
    }

    protected RoleService getRoleService() {
        if ( roleService == null ) {
        	roleService = KIMServiceLocator.getRoleService();
        }
        return roleService;
    }
    
    @Override
    protected boolean canEditDocumentOverview(Document document) {
        // Change logic to allow editing document overview based on if user can edit the document
        return canEdit(document);
    }
    
}
