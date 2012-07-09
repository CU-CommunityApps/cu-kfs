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
package org.kuali.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

public class PurchaseOrderAmendmentNewIndividualItemValidation extends PurchaseOrderNewIndividualItemValidation {

    /**
     * Overrides the method in PurchaseOrderNewIndividualItemValidation to add additional validations that are specific to Amendment.
     * 
     * @see org.kuali.kfs.module.purap.document.validation.impl.PurchaseOrderDocumentRule#newIndividualItemValidation(org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument, java.lang.String, org.kuali.kfs.module.purap.businessobject.PurApItem)
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = super.validate(event);
        PurchaseOrderItem item = (PurchaseOrderItem) getItemForValidation();
        String identifierString = item.getItemIdentifierString();
        if ((item.getItemInvoicedTotalQuantity() != null) && (!(item.getItemInvoicedTotalQuantity()).isZero())) {
            if (item.getItemQuantity() == null) {
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_AMND_NULL, "Item Quantity", identifierString);
            }
            else if (item.getItemQuantity().compareTo(item.getItemInvoicedTotalQuantity()) < 0) {
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_AMND_INVALID, "Item Quantity", identifierString);
            }
        }

        if (item.getItemInvoicedTotalAmount() != null) {
            KualiDecimal total = item.getTotalAmount().abs();
            if ((total == null) || total.compareTo(item.getItemInvoicedTotalAmount().abs()) < 0) {
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_AMND_INVALID_AMT, "Item Extended Price", identifierString);
            }
        }
        
        return valid;
    }

}
