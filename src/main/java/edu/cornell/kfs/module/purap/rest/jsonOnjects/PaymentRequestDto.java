package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class PaymentRequestDto {
    private String vendorNumber;
    private Integer poNumber;
    private Date invoiceDate;
    private Date receivedDate;
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

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public Integer getPoNumber() {
        return poNumber;
    }

    public String getPoNumberString() {
        return String.valueOf(poNumber);
    }

    public void setPoNumber(Integer poNumber) {
        this.poNumber = poNumber;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public KualiDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(KualiDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getSpecialHandlingLine1() {
        return specialHandlingLine1;
    }

    public void setSpecialHandlingLine1(String specialHandlingLine1) {
        this.specialHandlingLine1 = specialHandlingLine1;
    }

    public String getSpecialHandlingLine2() {
        return specialHandlingLine2;
    }

    public void setSpecialHandlingLine2(String specialHandlingLine2) {
        this.specialHandlingLine2 = specialHandlingLine2;
    }

    public String getSpecialHandlingLine3() {
        return specialHandlingLine3;
    }

    public void setSpecialHandlingLine3(String specialHandlingLine3) {
        this.specialHandlingLine3 = specialHandlingLine3;
    }

    public List<PaymentRequestLineItemDto> getItems() {
        return items;
    }

    public void setItems(List<PaymentRequestLineItemDto> items) {
        this.items = items;
    }

    public KualiDecimal getFreightPrice() {
        return freightPrice;
    }

    public void setFreightPrice(KualiDecimal freightPrice) {
        this.freightPrice = freightPrice;
    }

    public String getFreightDescription() {
        return freightDescription;
    }

    public void setFreightDescription(String freightDescription) {
        this.freightDescription = freightDescription;
    }

    public KualiDecimal getMiscellaneousPrice() {
        return miscellaneousPrice;
    }

    public void setMiscellaneousPrice(KualiDecimal miscellaneousPrice) {
        this.miscellaneousPrice = miscellaneousPrice;
    }

    public String getMiscellaneousDescription() {
        return miscellaneousDescription;
    }

    public void setMiscellaneousDescription(String miscellaneousDescription) {
        this.miscellaneousDescription = miscellaneousDescription;
    }

    public KualiDecimal getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(KualiDecimal shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public String getShippingDescription() {
        return shippingDescription;
    }

    public void setShippingDescription(String shippingDescription) {
        this.shippingDescription = shippingDescription;
    }

    public List<PaymentRequestNoteDto> getNotes() {
        return notes;
    }

    public void setNotes(List<PaymentRequestNoteDto> notes) {
        this.notes = notes;
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
