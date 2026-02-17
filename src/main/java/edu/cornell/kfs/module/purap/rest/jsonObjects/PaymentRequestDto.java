package edu.cornell.kfs.module.purap.rest.jsonObjects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

public class PaymentRequestDto {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
    
    private String vendorNumber;
    private String poNumber;
    private String invoiceDate;
    private String receivedDate;
    private String invoiceNumber;
    private String invoiceAmount;
    private String specialHandlingLine1;
    private String specialHandlingLine2;
    private String specialHandlingLine3;
    private List<PaymentRequestLineItemDto> items;
    private String freightPrice;
    private String freightDescription;
    private String miscellaneousPrice;
    private String miscellaneousDescription;
    private String shippingPrice;
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

    public String getPoNumber() {
        return poNumber;
    }

    public Integer getPoNumberAsInteger() {
        return StringUtils.isNotBlank(poNumber) ? Integer.valueOf(poNumber) : null;
    }

    public void setPoNumber(final String poNumber) {
        this.poNumber = poNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getInvoiceDateAsLocalDate() {
        return StringUtils.isNotBlank(invoiceDate) ? LocalDate.parse(invoiceDate, DATE_FORMATTER) : null;
    }

    public void setInvoiceDate(final String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public LocalDate getReceivedDateAsLocalDate() {
        return StringUtils.isNotBlank(receivedDate) ? LocalDate.parse(receivedDate, DATE_FORMATTER) : null;
    }

    public void setReceivedDate(final String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public KualiDecimal getInvoiceAmountAsKualiDecimal() {
        return StringUtils.isNotBlank(invoiceAmount) ? new KualiDecimal(invoiceAmount) : null;
    }

    public void setInvoiceAmount(final String invoiceAmount) {
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

    public String getFreightPrice() {
        return freightPrice;
    }

    public KualiDecimal getFreightPriceAsKualiDecimal() {
        return StringUtils.isNotBlank(freightPrice) ? new KualiDecimal(freightPrice) : null;
    }

    public void setFreightPrice(final String freightPrice) {
        this.freightPrice = freightPrice;
    }

    public String getFreightDescription() {
        return freightDescription;
    }

    public void setFreightDescription(final String freightDescription) {
        this.freightDescription = freightDescription;
    }

    public String getMiscellaneousPrice() {
        return miscellaneousPrice;
    }

    public KualiDecimal getMiscellaneousPriceAsKualiDecimal() {
        return StringUtils.isNotBlank(miscellaneousPrice) ? new KualiDecimal(miscellaneousPrice) : null;
    }

    public void setMiscellaneousPrice(final String miscellaneousPrice) {
        this.miscellaneousPrice = miscellaneousPrice;
    }

    public String getMiscellaneousDescription() {
        return miscellaneousDescription;
    }

    public void setMiscellaneousDescription(final String miscellaneousDescription) {
        this.miscellaneousDescription = miscellaneousDescription;
    }

    public String getShippingPrice() {
        return shippingPrice;
    }

    public KualiDecimal getShippingPriceAsKualiDecimal() {
        return StringUtils.isNotBlank(shippingPrice) ? new KualiDecimal(shippingPrice) : null;
    }

    public void setShippingPrice(final String shippingPrice) {
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
