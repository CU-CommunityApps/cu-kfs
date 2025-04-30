package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.sys.KFSConstants;

public class PurchaseOrderInvoiceDto {
    private static final SimpleDateFormat SDF = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT,
            Locale.US);

    private String invoiceDate;
    private String invoiceNumber;
    private String invoiceAmount;

    public PurchaseOrderInvoiceDto() {
    }

    public PurchaseOrderInvoiceDto(PaymentRequestView reqView) {
        this.invoiceDate = reqView.getInvoiceDate() != null ? SDF.format(reqView.getInvoiceDate()) : StringUtils.EMPTY;
        this.invoiceNumber = reqView.getInvoiceNumber();
        this.invoiceAmount = reqView.getTotalAmount() != null ? reqView.getTotalAmount().toString() : StringUtils.EMPTY;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

}
