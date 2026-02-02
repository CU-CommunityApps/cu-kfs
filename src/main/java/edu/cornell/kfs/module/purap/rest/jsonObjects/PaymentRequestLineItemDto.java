package edu.cornell.kfs.module.purap.rest.jsonObjects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class PaymentRequestLineItemDto {
    private String lineNumber;
    private String itemQuantity;
    private String itemPrice;

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getLineNumberAsInteger() {
        if (StringUtils.isBlank(lineNumber)) {
            return null;
        }
        return Integer.valueOf(lineNumber);
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(final String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public KualiDecimal getItemQuantityAsKualiDecimal() {
        if (StringUtils.isBlank(itemQuantity)) {
            return null;
        }
        return new KualiDecimal(itemQuantity);
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(final String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public KualiDecimal getItemPriceAsKualiDecimal() {
        if (StringUtils.isBlank(itemPrice)) {
            return null;
        }
        return new KualiDecimal(itemPrice);
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
