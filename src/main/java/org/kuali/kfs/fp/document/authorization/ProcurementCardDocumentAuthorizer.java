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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentAuthorizerBase;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * Document Authorizer for the Procurement Card document.
 */
public class ProcurementCardDocumentAuthorizer extends AccountingDocumentAuthorizerBase {  

    /**
     * String constant to represent the route nodes for the PCard doc.
     */
    private static final String ACCOUNT_FULL_EDIT="AccountFullEdit";
    
    @Override 
    public Set<String> getDocumentActions(Document document, Person user, Set<String> documentActions) {
        Set<String> docActions = super.getDocumentActions(document, user, documentActions);
        try {
            KualiWorkflowDocument workflowDocument = (KualiWorkflowDocument) document.getDocumentHeader().getWorkflowDocument();
            List<String> activeNodes = Arrays.asList(workflowDocument.getNodeNames());
            
            if (workflowDocument.stateIsEnroute() && activeNodes.contains(ACCOUNT_FULL_EDIT) && (((FinancialSystemDocumentHeader) document.getDocumentHeader()).getFinancialDocumentInErrorNumber() == null) && !workflowDocument.isAdHocRequested()) {
                docActions.add(KNSConstants.KUALI_ACTION_CAN_EDIT__DOCUMENT_OVERVIEW); 
            }
        } catch(WorkflowException we) {
            // If errors occur, then rely on values from parent.
        }
           
        return docActions; 
    }
}
