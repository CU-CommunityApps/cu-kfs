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

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PurchaseOrderEditMode;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.RequisitionEditMode;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.RoleManagementService;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;


public class PurchaseOrderAmendmentDocumentPresentationController extends PurchaseOrderDocumentPresentationController {
    
    @Override
    protected boolean canEdit(Document document) {
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument)document;
        // po amend docs in CGIP status are only editable when in Initiated or Saved status
        if (PurchaseOrderStatuses.CHANGE_IN_PROCESS.equals(poDocument.getStatusCode())) {
            KualiWorkflowDocument workflowDoc = document.getDocumentHeader().getWorkflowDocument();
            if (!workflowDoc.stateIsInitiated() && !workflowDoc.stateIsSaved()) {
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
            KualiWorkflowDocument workflowDoc = document.getDocumentHeader().getWorkflowDocument();
            //  amendment doc needs to lock its field for initiator while enroute
            if (workflowDoc.stateIsInitiated() || workflowDoc.stateIsSaved()) {
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
			KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
			if (workflowDocument.stateIsEnroute()) {
				List<String> activeNodes = Arrays.asList(workflowDocument.getNodeNames());
				;
				for (String nodeNamesNode : activeNodes) {
					if (RequisitionStatuses.NODE_ACCOUNT.equals(nodeNamesNode) && !isFiscalOfficersForAllAcctLines((PurchaseOrderAmendmentDocument)document)) {
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
 
		return editModes;
    }

	private boolean isFiscalOfficersForAllAcctLines(PurchaseOrderAmendmentDocument document) {

		boolean isFoForAcctLines = true;
		String personId = GlobalVariables.getUserSession().getPrincipalId();
		for (SourceAccountingLine accountingLine : (List<SourceAccountingLine>)document.getSourceAccountingLines()) {
			List<String> fiscalOfficers = new ArrayList<String>();
			AttributeSet roleQualifier = new AttributeSet();
			roleQualifier.put(KfsKimAttributes.DOCUMENT_NUMBER,document.getDocumentNumber());
			roleQualifier.put(KfsKimAttributes.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentType());
			roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,document.getDocumentHeader().getFinancialDocumentTotalAmount().toString());
			roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLine.getChartOfAccountsCode());
			roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLine.getAccountNumber());
			fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
					KFSConstants.SysKimConstants.FISCAL_OFFICER_KIM_ROLE_NAME,roleQualifier));
			if (!fiscalOfficers.contains(personId)) {
				fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(
										KFSConstants.ParameterNamespaces.KFS,KFSConstants.SysKimConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME,
										roleQualifier));
			}
			if (!fiscalOfficers.contains(personId)) {
				fiscalOfficers.addAll(SpringContext.getBean(RoleManagementService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
										KFSConstants.SysKimConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME,roleQualifier));
			}
			if (!fiscalOfficers.contains(personId)) {
				isFoForAcctLines = false;
				break;
			}
		}

		return isFoForAcctLines;
	}

    @Override
    protected boolean canReload(Document document) {
        //  show the reload button if the doc is anything but processed or final
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return (workflowDocument.stateIsSaved() || workflowDocument.stateIsEnroute()) ;
    }

}
