package edu.cornell.kfs.concur.batch.xmlObjects;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

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
@XmlRootElement(name = "detail", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedDetailEntry {

    @XmlElement(name = "source_doc_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String sourceDocNbr;
    @XmlElement(name = "invoice_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String invoiceNbr;
    @XmlElement(name = "po_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String poNbr;
    @XmlElement(name = "req_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String reqNbr;
    @XmlElement(name = "org_doc_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String orgDocNbr;
    @XmlElement(name = "invoice_date", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String invoiceDate;
    @XmlElement(name = "orig_invoice_amt", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal origInvoiceAmt;
    @XmlElement(name = "net_payment_amt", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal netPaymentAmt;
    @XmlElement(name = "invoice_tot_discount_amt", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal invoiceTotDiscountAmt;
    @XmlElement(name = "invoice_tot_ship_amt", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal invoiceTotShipAmt;
    @XmlElement(name = "invoice_tot_other_debits", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal invoiceTotOtherDebits;
    @XmlElement(name = "invoice_tot_other_credits", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal invoiceTotOtherCredits;
    @XmlElement(name = "fs_origin_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String fsOriginCd;
    @XmlElement(name = "fdoc_typ_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String fdocTypCd;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected List<PdpFeedAccountingEntry> accounting;
    @XmlElement(name = "payment_text", namespace = ConcurConstants.PDP_XML_NAMESPACE)
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

    public KualiDecimal getOrigInvoiceAmt() {
        return origInvoiceAmt;
    }

    public void setOrigInvoiceAmt(KualiDecimal value) {
        this.origInvoiceAmt = value;
    }

    public KualiDecimal getNetPaymentAmt() {
        return netPaymentAmt;
    }

    public void setNetPaymentAmt(KualiDecimal value) {
        this.netPaymentAmt = value;
    }

    public KualiDecimal getInvoiceTotDiscountAmt() {
        return invoiceTotDiscountAmt;
    }

    public void setInvoiceTotDiscountAmt(KualiDecimal value) {
        this.invoiceTotDiscountAmt = value;
    }

    public KualiDecimal getInvoiceTotShipAmt() {
        return invoiceTotShipAmt;
    }

    public void setInvoiceTotShipAmt(KualiDecimal value) {
        this.invoiceTotShipAmt = value;
    }

    public KualiDecimal getInvoiceTotOtherDebits() {
        return invoiceTotOtherDebits;
    }

    public void setInvoiceTotOtherDebits(KualiDecimal value) {
        this.invoiceTotOtherDebits = value;
    }

    public KualiDecimal getInvoiceTotOtherCredits() {
        return invoiceTotOtherCredits;
    }

    public void setInvoiceTotOtherCredits(KualiDecimal value) {
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
    
    public List<PdpFeedAccountingEntry> getAccounting() {
        if (accounting == null) {
            accounting = new ArrayList<PdpFeedAccountingEntry>();
        }
        return this.accounting;
    }

    public void setAccounting(List<PdpFeedAccountingEntry> accounting) {
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
