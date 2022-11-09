/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.document.authorization;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.identity.Person;

import java.util.List;


public class PurchasingAccountsPayableDocumentPresentationController extends 
        FinancialSystemTransactionalDocumentPresentationControllerBase {
    
    /**
     * None of the PURAP documents allowing editing by adhoc requests
     */
    @Override
    public boolean canEdit(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        WorkflowDocument kwf = document.getDocumentHeader().getWorkflowDocument();
        //Adding this check so that the initiator will always be able to edit the document (before initial submission)
        if (kwf.getInitiatorPrincipalId().equals(currentUser.getPrincipalId()) && (kwf.isInitiated() || kwf.isSaved()) ) {
            return true;
        }
        if (!document.getDocumentHeader().getWorkflowDocument().isCompletionRequested()
                && SpringContext.getBean(FinancialSystemWorkflowHelperService.class)
                    .isAdhocApprovalRequestedForPrincipal(document.getDocumentHeader().getWorkflowDocument(),
                            GlobalVariables.getUserSession().getPrincipalId())) {
            return false;
        }
        return super.canEdit(document);
    }

    @Override
    public boolean canEditDocumentOverview(Document document) {
        // Change logic to allow editing document overview based on if user can edit the document
        return canEdit(document);
    }
 
    // KFSPTS-985
    public boolean hasEmptyAcctline(PurchasingDocumentBase document) {
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
