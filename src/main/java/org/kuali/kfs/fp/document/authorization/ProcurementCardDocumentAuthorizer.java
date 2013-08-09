/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.fp.document.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentAuthorizerBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;


/**
 * Document Authorizer for the Procurement Card document.
 */
public class ProcurementCardDocumentAuthorizer extends AccountingDocumentAuthorizerBase {  


  private static final long serialVersionUID = 1L;
    /**
     * String constant to represent the route nodes for the PCard doc.
     */
    private static final String ACCOUNT_FULL_EDIT="AccountFullEdit";
    
    @Override 
    public Set<String> getDocumentActions(Document document, Person user, Set<String> documentActions) {
        Set<String> docActions = super.getDocumentActions(document, user, documentActions);
        WorkflowDocument workflowDocument = (WorkflowDocument) document.getDocumentHeader().getWorkflowDocument();
        List<String> activeNodes = new ArrayList<String>(workflowDocument.getNodeNames());
        
        if (workflowDocument.isEnroute() && activeNodes.contains(ACCOUNT_FULL_EDIT) && (((FinancialSystemDocumentHeader) document.getDocumentHeader()).getFinancialDocumentInErrorNumber() == null) && !SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(workflowDocument, GlobalVariables.getUserSession().getPrincipalId())) {
            docActions.add(KRADConstants.KUALI_ACTION_CAN_EDIT_DOCUMENT_OVERVIEW); 
        }
           
        return docActions; 
    }
}
