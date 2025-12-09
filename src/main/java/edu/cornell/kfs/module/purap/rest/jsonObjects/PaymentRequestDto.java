package edu.cornell.kfs.module.purap.rest.jsonObjects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class PaymentRequestDto {
    private String vendorNumber;
    private Integer poNumber;
    private LocalDate invoiceDate;
    private LocalDate receivedDate;
    private String invoiceNumber;
    private KualiDecimal invoiceAmount;
    private String specialHandlingLine1;
    private String specialHandlingLine2;
    private String specialHandlingLine3;
    private List<PaymentRequestLineItemDto> items;
    private KualiDecimal freightPrice;
    private String freightDescription;
    private KualiDecimal miscellaneousPrice;
    private String miscellaneousDescription;
    private KualiDecimal shippingPrice;
    private String shippingDescription;
    private List<PaymentRequestNoteDto> notes;

    public PaymentRequestDto() {
        items = new ArrayList<>();
        notes = new ArrayList<>();
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(final String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public Integer getPoNumber() {
        return poNumber;
    }

    public String getPoNumberString() {
        return poNumber != null ? String.valueOf(poNumber) : StringUtils.EMPTY;
    }

    public void setPoNumber(final Integer poNumber) {
        this.poNumber = poNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(final LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public KualiDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(final KualiDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getSpecialHandlingLine1() {
        return specialHandlingLine1;
    }

    public void setSpecialHandlingLine1(final String specialHandlingLine1) {
        this.specialHandlingLine1 = specialHandlingLine1;
    }

    public String getSpecialHandlingLine2() {
        return specialHandlingLine2;
    }

    public void setSpecialHandlingLine2(final String specialHandlingLine2) {
        this.specialHandlingLine2 = specialHandlingLine2;
    }

    public String getSpecialHandlingLine3() {
        return specialHandlingLine3;
    }

    public void setSpecialHandlingLine3(final String specialHandlingLine3) {
        this.specialHandlingLine3 = specialHandlingLine3;
    }

    public List<PaymentRequestLineItemDto> getItems() {
        return items;
    }

    public void setItems(final List<PaymentRequestLineItemDto> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    public KualiDecimal getFreightPrice() {
        return freightPrice;
    }

    public void setFreightPrice(final KualiDecimal freightPrice) {
        this.freightPrice = freightPrice;
    }

    public String getFreightDescription() {
        return freightDescription;
    }

    public void setFreightDescription(final String freightDescription) {
        this.freightDescription = freightDescription;
    }

    public KualiDecimal getMiscellaneousPrice() {
        return miscellaneousPrice;
    }

    public void setMiscellaneousPrice(final KualiDecimal miscellaneousPrice) {
        this.miscellaneousPrice = miscellaneousPrice;
    }

    public String getMiscellaneousDescription() {
        return miscellaneousDescription;
    }

    public void setMiscellaneousDescription(final String miscellaneousDescription) {
        this.miscellaneousDescription = miscellaneousDescription;
    }

    public KualiDecimal getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(final KualiDecimal shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public String getShippingDescription() {
        return shippingDescription;
    }

    public void setShippingDescription(final String shippingDescription) {
        this.shippingDescription = shippingDescription;
    }

    public List<PaymentRequestNoteDto> getNotes() {
        return notes;
    }

    public void setNotes(final List<PaymentRequestNoteDto> notes) {
        this.notes = (notes == null) ? new ArrayList<>() : notes;
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
