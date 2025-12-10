package edu.cornell.kfs.module.purap.util;

public class PaymentRequestUtil {
    public enum PaymentRequestDtoFields {
        VENDOR_NUMBER(true, "Vendor Number"), 
        PO_NUMBER(true, "PO Number"),
        INVOICE_DATE(true, "Invoice Date"),
        RECEIVED_DATE(true, "Received Date"),
        INVOICE_NUMBER(true, "Invoice Number"),
        INVOICE_AMOUNT(true, "Invoice Amount"),
        SPECIAL_HANDLING_LINE_1(false, "Special Handling Line 1"),
        SPECIAL_HANDLING_LINE_2(false, "Special Handling Line 2"),
        SPECIAL_HANDLING_LINE_3(false, "Special Handling Line 3"),
        FREIGHT_PRICE(false, "Freight Price"),
        FREIGHT_DESCRIPTION(false, "Freight Description"),
        MISC_PRICE(false, "Miscellaneous Price"),
        MISC_DESCRIPTION(false, "Miscellaneous Description"),
        SHIPPING_PRICE(false, "Shipping Price"),
        SHIPPING_DESCRIPTION(false, "Shipping Description"),
        ITEM_LINE_NUMBER(true, "Item Line Number"),
        ITEM_QUANTITY(true, "Item Quantity"),
        ITEM_PRICE(true, "Item Price"),
        NOTE_TEXT(true, "Note Text"),
        NOTE_TYPE(false, "Note Type");

        public final boolean required;
        public final String friendlyName;

        private PaymentRequestDtoFields(boolean required, String friendlyName) {
            this.required = required;
            this.friendlyName = friendlyName;
        }
    }
}
