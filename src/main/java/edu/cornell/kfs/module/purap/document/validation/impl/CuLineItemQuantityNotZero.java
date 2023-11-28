package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.LineItemQuantityNotZero;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.GlobalVariables;

public class CuLineItemQuantityNotZero extends LineItemQuantityNotZero {

    @Override
    public boolean validate(final AttributedDocumentEvent event) 
    {
        boolean valid = true;
        
        final PaymentRequestDocument document = (PaymentRequestDocument)event.getDocument();
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        int i = 0;
        for (final PurApItem item : (List<PurApItem>)document.getItems()) {
            final KualiDecimal itemQuantity = item.getItemQuantity();
            if (!((PaymentRequestItem)item).isNoQtyItem() && itemQuantity != null) {
                if (!itemQuantity.isNonZero()) {
                    GlobalVariables.getMessageMap().putError("item[" + i + "].itemQuantity",
                            PurapKeyConstants.ERROR_PAYMENT_REQUEST_LINE_ITEM_QUANTITY_ZERO);
                    GlobalVariables.getMessageMap().clearErrorPath();
                    
                    valid = false;
                    break;
                }
                i++;
            }
        }
        
        return valid;
    }

}
