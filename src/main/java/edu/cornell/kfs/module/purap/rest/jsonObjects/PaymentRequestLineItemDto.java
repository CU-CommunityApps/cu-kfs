package edu.cornell.kfs.module.purap.rest.jsonObjects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class PaymentRequestLineItemDto {
    private Integer lineNumber;
    private KualiDecimal itemQuantity;
    private KualiDecimal itemPrice;

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(final KualiDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public KualiDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(final KualiDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
