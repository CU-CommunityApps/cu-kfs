package edu.cornell.kfs.concur.batch.xmlObjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sourceDocNbr",
    "invoiceNbr",
    "poNbr",
    "reqNbr",
    "orgDocNbr",
    "invoiceDate",
    "origInvoiceAmt",
    "netPaymentAmt",
    "invoiceTotDiscountAmt",
    "invoiceTotShipAmt",
    "invoiceTotOtherDebits",
    "invoiceTotOtherCredits",
    "fsOriginCd",
    "fdocTypCd",
    "accounting",
    "paymentText"
})
@XmlRootElement(name = "detail", namespace = "http://www.kuali.org/kfs/pdp/payment")
public class Detail {

    @XmlElement(name = "source_doc_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String sourceDocNbr;
    @XmlElement(name = "invoice_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String invoiceNbr;
    @XmlElement(name = "po_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String poNbr;
    @XmlElement(name = "req_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String reqNbr;
    @XmlElement(name = "org_doc_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String orgDocNbr;
    @XmlElement(name = "invoice_date", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String invoiceDate;
    @XmlElement(name = "orig_invoice_amt", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal origInvoiceAmt;
    @XmlElement(name = "net_payment_amt", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal netPaymentAmt;
    @XmlElement(name = "invoice_tot_discount_amt", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal invoiceTotDiscountAmt;
    @XmlElement(name = "invoice_tot_ship_amt", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal invoiceTotShipAmt;
    @XmlElement(name = "invoice_tot_other_debits", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal invoiceTotOtherDebits;
    @XmlElement(name = "invoice_tot_other_credits", namespace = "http://www.kuali.org/kfs/pdp/payment")
    protected BigDecimal invoiceTotOtherCredits;
    @XmlElement(name = "fs_origin_cd", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String fsOriginCd;
    @XmlElement(name = "fdoc_typ_cd", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String fdocTypCd;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected List<Accounting> accounting;
    @XmlElement(name = "payment_text", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected List<String> paymentText;

    public String getSourceDocNbr() {
        return sourceDocNbr;
    }

    public void setSourceDocNbr(String value) {
        this.sourceDocNbr = value;
    }

    public String getInvoiceNbr() {
        return invoiceNbr;
    }

    public void setInvoiceNbr(String value) {
        this.invoiceNbr = value;
    }

    public String getPoNbr() {
        return poNbr;
    }

    public void setPoNbr(String value) {
        this.poNbr = value;
    }

    public String getReqNbr() {
        return reqNbr;
    }

    public void setReqNbr(String value) {
        this.reqNbr = value;
    }

    public String getOrgDocNbr() {
        return orgDocNbr;
    }

    public void setOrgDocNbr(String value) {
        this.orgDocNbr = value;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String value) {
        this.invoiceDate = value;
    }

    public BigDecimal getOrigInvoiceAmt() {
        return origInvoiceAmt;
    }

    public void setOrigInvoiceAmt(BigDecimal value) {
        this.origInvoiceAmt = value;
    }

    public BigDecimal getNetPaymentAmt() {
        return netPaymentAmt;
    }

    public void setNetPaymentAmt(BigDecimal value) {
        this.netPaymentAmt = value;
    }

    public BigDecimal getInvoiceTotDiscountAmt() {
        return invoiceTotDiscountAmt;
    }

    public void setInvoiceTotDiscountAmt(BigDecimal value) {
        this.invoiceTotDiscountAmt = value;
    }

    public BigDecimal getInvoiceTotShipAmt() {
        return invoiceTotShipAmt;
    }

    public void setInvoiceTotShipAmt(BigDecimal value) {
        this.invoiceTotShipAmt = value;
    }

    public BigDecimal getInvoiceTotOtherDebits() {
        return invoiceTotOtherDebits;
    }

    public void setInvoiceTotOtherDebits(BigDecimal value) {
        this.invoiceTotOtherDebits = value;
    }

    public BigDecimal getInvoiceTotOtherCredits() {
        return invoiceTotOtherCredits;
    }

    public void setInvoiceTotOtherCredits(BigDecimal value) {
        this.invoiceTotOtherCredits = value;
    }

    public String getFsOriginCd() {
        return fsOriginCd;
    }

    public void setFsOriginCd(String value) {
        this.fsOriginCd = value;
    }

    public String getFdocTypCd() {
        return fdocTypCd;
    }

    public void setFdocTypCd(String value) {
        this.fdocTypCd = value;
    }
    
    public List<Accounting> getAccounting() {
        if (accounting == null) {
            accounting = new ArrayList<Accounting>();
        }
        return this.accounting;
    }

    public void setAccounting(List<Accounting> accounting) {
        this.accounting = accounting;
    }

    public List<String> getPaymentText() {
        if (paymentText == null) {
            paymentText = new ArrayList<String>();
        }
        return this.paymentText;
    }

    public void setPaymentText(List<String> paymentText) {
        this.paymentText = paymentText;
    }

}
