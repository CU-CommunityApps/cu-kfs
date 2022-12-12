package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.validation.impl.RequisitionAssignToTradeInValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuRequisitionAssignToTradeInValidation extends RequisitionAssignToTradeInValidation {

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        
        boolean foundTradeIn = false;
        boolean valid        = true;
        
        PurchasingAccountsPayableDocumentBase purapDoc = (PurchasingAccountsPayableDocumentBase) event.getDocument();
        
        // First, get all the items from the requisition document. For each of the items, look for the ones that are
        // assigned to a trade-in value. For these trade-in items, validate that the trade-in line has a valid
        // description and amount.
        List<PurApItem> items = (List<PurApItem>)purapDoc.getItems();
        for (PurApItem item : items) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
            if (item.getItemAssignedToTradeInIndicator()) {
                foundTradeIn = true;
                break;
            }
        }
        
        // Was a trade-in found for any of the above items?
        if (foundTradeIn) {
            PurApItem tradeInItem = purapDoc.getTradeInItem();
            if (tradeInItem != null) {
                if (StringUtils.isEmpty(tradeInItem.getItemDescription())) {
                    tradeInItem.getItemLineNumber();
                    GlobalVariables.getMessageMap().putError(
                            "document.item[" + getTradeInItemLineIndex(items) + "]." + PurapPropertyConstants.ITEM_DESCRIPTION, 
                            PurapKeyConstants.ERROR_ITEM_BELOW_THE_LINE, 
                            "The item description of " + tradeInItem.getItemType().getItemTypeDescription(), 
                            "empty");
                    
                    valid = false;
                }
                else if (ObjectUtils.isNull(tradeInItem.getItemUnitPrice())) {
                    GlobalVariables.getMessageMap().putError(
                            "document.item[" + getTradeInItemLineIndex(items) + "]." + PurapPropertyConstants.ITEM_UNIT_PRICE,
                            PurapKeyConstants.ERROR_ITEM_BELOW_THE_LINE,
                            tradeInItem.getItemType().getItemTypeDescription(),
                            "zero");
                    
                    valid = false;
                }
            }
        }
        
        return valid;
    }

    private int getTradeInItemLineIndex(List<PurApItem> items) {
        int lineIndex = 0;
        for (PurApItem item : items) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                break;
            } else {
                lineIndex++;
            }
        }
        return lineIndex;
    }
}
