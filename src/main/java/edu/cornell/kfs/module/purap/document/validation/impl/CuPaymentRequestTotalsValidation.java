package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestTotalsValidation;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;

public class CuPaymentRequestTotalsValidation extends PaymentRequestTotalsValidation {

    protected void flagLineItemTotals(final List<PurApItem> itemList) {
        for (final PurApItem purApItem : itemList) {
            final PaymentRequestItem item = (PaymentRequestItem) purApItem;
            // KITI-2549 : Added check to confirm extended price doesn't equal 0.00 since unit prices of $0.00 are now allowed in KFS.
            // KFSPTS-1719 if po is no qty, inv is qty. 
            if (item.getItemQuantity() != null && (item.getExtendedPrice()!=null && KualiDecimal.ZERO.compareTo(item.getExtendedPrice())!=0)) {
                if (!item.getPurchaseOrderItem().isNoQtyItem() && item.calculateExtendedPrice().compareTo(item.getExtendedPrice()) != 0) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, CUPurapKeyConstants.ERROR_PAYMENT_REQUEST_ITEM_TOTAL_NOT_EQUAL, item.getItemIdentifierString());
                }
            }
        }
    }

}
