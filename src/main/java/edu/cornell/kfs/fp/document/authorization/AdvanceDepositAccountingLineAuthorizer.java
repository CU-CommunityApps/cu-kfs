/*
 * Copyright 2008 The Kuali Foundation Licensed under the Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl2.php Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.sys.KFSConstants.RouteLevelNames;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * The default implementation of AccountingLineAuthorizer
 */
public class AdvanceDepositAccountingLineAuthorizer extends AccountingLineAuthorizerBase {
    
    @Override
    public boolean determineEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, String fieldName, boolean editablePage) {
       boolean editable = super.determineEditPermissionOnField(accountingDocument, accountingLine, accountingLineCollectionProperty, fieldName, editablePage);
       if (isDocumentStoppedInRouteNode(RouteLevelNames.ACCOUNTING_ORGANIZATION_HIERARCHY, accountingDocument)) {
           editable = true;
       }
       return editable;
    }

    @Override
    public boolean determineEditPermissionOnLine(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator, boolean pageIsEditable) {
        boolean editable = super.determineEditPermissionOnLine(accountingDocument, accountingLine, accountingLineCollectionProperty, currentUserIsDocumentInitiator, pageIsEditable);

        if (isDocumentStoppedInRouteNode(RouteLevelNames.ACCOUNTING_ORGANIZATION_HIERARCHY, accountingDocument)) {
            editable = true;
        }
        return editable;
    }

    public boolean isDocumentStoppedInRouteNode(String nodeName, AccountingDocument accountingDocument) {
        KualiWorkflowDocument workflowDoc = accountingDocument.getDocumentHeader().getWorkflowDocument();
        String currentRouteLevels = accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentRouteNodeNames();
        if (currentRouteLevels.contains(nodeName) && workflowDoc.isApprovalRequested()) {
            return true;
        }
        return false;
    }

}
