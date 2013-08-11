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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PurchaseOrderEditMode;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.RequisitionEditMode;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;

public class PurchaseOrderAmendmentDocumentPresentationController extends PurchaseOrderDocumentPresentationController {
    
    @Override
    public boolean canEdit(Document document) {
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument)document;
        // po amend docs in CGIP status are only editable when in Initiated or Saved status
        if (PurchaseOrderStatuses.CHANGE_IN_PROCESS.equals(poDocument.getStatusCode())) {
            WorkflowDocument workflowDoc = document.getDocumentHeader().getWorkflowDocument();
            if (!workflowDoc.isInitiated() && !workflowDoc.isSaved()) {
                return false;
            }
        }
        if (PurchaseOrderStatuses.AWAIT_FISCAL_REVIEW.equals(poDocument.getStatusCode())) {
        	return true;
        }
        return super.canEdit(document);
    }
    
    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument)document;

        if (PurchaseOrderStatuses.CHANGE_IN_PROCESS.equals(poDocument.getStatusCode())) {
            WorkflowDocument workflowDoc = document.getDocumentHeader().getWorkflowDocument();
            //  amendment doc needs to lock its field for initiator while enroute
            if (workflowDoc.isInitiated() || workflowDoc.isSaved()) {
                editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
            }
        }
        if (PurchaseOrderStatuses.AWAIT_NEW_UNORDERED_ITEM_REVIEW.equals(poDocument.getStatusCode())) {
            editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
        }
        if (PurchaseOrderStatuses.AWAIT_FISCAL_REVIEW.equals(poDocument.getStatusCode())) {
        	editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
        }
        if (SpringContext.getBean(PurapService.class).isDocumentStoppedInRouteNode((PurchasingAccountsPayableDocument) document, "NewUnorderedItems")) {
            editModes.add(PurchaseOrderEditMode.UNORDERED_ITEM_ACCOUNT_ENTRY);
        }
        
        // KFS-1768/KFSCNTRB-1138
        // This is ported from 5.0, and Workflowdocument is different, so needs more testing.
		try {
			WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
			if (workflowDocument.isEnroute()) {
				List<String> activeNodes = new ArrayList<String>(workflowDocument.getNodeNames());
				;
				for (String nodeNamesNode : activeNodes) {
					if (RequisitionStatuses.NODE_ACCOUNT.equals(nodeNamesNode) && !SpringContext.getBean(PurapAccountingService.class).isFiscalOfficersForAllAcctLines((PurchaseOrderAmendmentDocument)document)) {
						// disable the button for setup distribution
						editModes.add(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION);
						// disable the button for remove accounts from all items
						editModes.add(RequisitionEditMode.DISABLE_REMOVE_ACCTS);
						// disable the button for remove commodity codes from
						// all
						// items
						if (editModes.contains(RequisitionEditMode.ENABLE_COMMODITY_CODE)) {
							editModes.remove(RequisitionEditMode.ENABLE_COMMODITY_CODE);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		// KFSPTS-985
		if (document instanceof PurchaseOrderAmendmentDocument && !editModes.contains(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION) && !hasEmptyAcctline((PurchaseOrderAmendmentDocument)document) ) {
			editModes.add(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION);
		}
 
		return editModes;
    }
    
    @Override
    public boolean canReload(Document document) {
        //  show the reload button if the doc is anything but processed or final
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return (workflowDocument.isSaved() || workflowDocument.isEnroute()) ;
    }

}
