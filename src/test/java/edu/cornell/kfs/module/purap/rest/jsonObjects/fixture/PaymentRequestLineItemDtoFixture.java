package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestLineItemDto;

public enum PaymentRequestLineItemDtoFixture {

    ITEM_1_10_100(1, new KualiDecimal(10), new KualiDecimal(100)),
    ITEM_1_5_200(1, new KualiDecimal(5), new KualiDecimal(200)),
    ITEM_2_3_400(2, new KualiDecimal(3), new KualiDecimal(400)),
    ITEM_3_2_500(3, new KualiDecimal(2), new KualiDecimal(500)),
    EMPTY_ITEM(null, null, null);

    public final Integer lineNumber;
    public final KualiDecimal itemQuantity;
    public final KualiDecimal itemPrice;

    private PaymentRequestLineItemDtoFixture(Integer lineNumber, KualiDecimal itemQuantity, KualiDecimal itemPrice) {
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
