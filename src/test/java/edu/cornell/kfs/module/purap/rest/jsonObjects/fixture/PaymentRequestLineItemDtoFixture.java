package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestLineItemDto;

public enum PaymentRequestLineItemDtoFixture {

    ITEM_1_10_100("1", "10", "100"),
    ITEM_1_5_200("1", "5", "200"),
    ITEM_2_3_400("2", "3", "400"),
    ITEM_3_2_500("3", "2", "500"),
    EMPTY_ITEM(null, null, null);

    public final String lineNumber;
    public final String itemQuantity;
    public final String itemPrice;

    private PaymentRequestLineItemDtoFixture(String lineNumber, String itemQuantity, String itemPrice) {
        this.lineNumber = lineNumber;
        this.itemQuantity = itemQuantity;
        this.itemPrice = itemPrice;
    }

    public PaymentRequestLineItemDto toPaymentRequestLineItemDto() {
        PaymentRequestLineItemDto dto = new PaymentRequestLineItemDto();
        dto.setLineNumber(lineNumber);
        dto.setItemQuantity(itemQuantity);
        dto.setItemPrice(itemPrice);
        return dto;
    }

}
