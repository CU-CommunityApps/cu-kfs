package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemFields;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestProcessItemValidation;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuPaymentRequestProcessItemValidation extends PaymentRequestProcessItemValidation {

    protected boolean validateAboveTheLineItems(
            final PaymentRequestItem item, final String identifierString,
            final boolean isReceivingDocumentRequiredIndicator, final PaymentRequestDocument paymentRequestDocument) {
        boolean valid = true;
        // Currently Quantity is allowed to be NULL on screen; must be either a positive number or NULL for DB
        final MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        
        if (ObjectUtils.isNotNull(item.getItemQuantity())) {
            if (item.getItemQuantity().isNegative()) {
                // if quantity is negative give an error
                valid = false;
                errorMap.putError(PurapConstants.ITEM_TAB_ERRORS, PurapKeyConstants.ERROR_ITEM_AMOUNT_BELOW_ZERO, ItemFields.INVOICE_QUANTITY, identifierString);
            }
            if (!isReceivingDocumentRequiredIndicator){
            	// KFSPTS-1719, KFSUPGRADE-485 : add isnoqtyitem check
                if (!item.getPurchaseOrderItem().isNoQtyItem() && item.getPoOutstandingQuantity().isLessThan(item.getItemQuantity())) {
                    valid = false;
                   errorMap.putError(PurapConstants.ITEM_TAB_ERRORS, PurapKeyConstants.ERROR_ITEM_QUANTITY_TOO_MANY, ItemFields.INVOICE_QUANTITY, identifierString, ItemFields.OPEN_QUANTITY);
                }
            }
        }
        if (ObjectUtils.isNotNull(item.getExtendedPrice()) && item.getExtendedPrice().isPositive() && ObjectUtils.isNotNull(item.getPoOutstandingQuantity()) && item.getPoOutstandingQuantity().isPositive()) {

            // here we must require the user to enter some value for quantity if they want a credit amount associated
            if (ObjectUtils.isNull(item.getItemQuantity()) || item.getItemQuantity().isZero()) {
                // here we have a user not entering a quantity with an extended amount but the PO has a quantity...require user to
                // enter a quantity
                valid = false;
                errorMap.putError(PurapConstants.ITEM_TAB_ERRORS, PurapKeyConstants.ERROR_ITEM_QUANTITY_REQUIRED, ItemFields.INVOICE_QUANTITY, identifierString, ItemFields.OPEN_QUANTITY);
            }
        }

        //Modified to use the payment request document to not cause unnecessary refetch
        // check that non-quantity based items are not trying to pay on a zero encumbrance amount (check only prior to ap approval)
        if (ObjectUtils.isNull(paymentRequestDocument.getPurapDocumentIdentifier())
                || PaymentRequestStatuses.APPDOC_IN_PROCESS.equals(
                        paymentRequestDocument.getApplicationDocumentStatus())) {
            if (item.getItemType().isAmountBasedGeneralLedgerIndicator() && item.getExtendedPrice() != null
                    && item.getExtendedPrice().isNonZero()) {
                if (item.getPoOutstandingAmount() == null || item.getPoOutstandingAmount().isZero()) {
                    valid = false;
                    errorMap.putError(PurapConstants.ITEM_TAB_ERRORS, PurapKeyConstants.ERROR_ITEM_AMOUNT_ALREADY_PAID, identifierString);
                }
            }
        }

        return valid;
    }

}
