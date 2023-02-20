
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "invoiceQtyDifferencePercent", "invoiceQtyDifferenceQuantity" })
@XmlRootElement(name = "InvoiceQtyDifference")
public class InvoiceQtyDifference {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "InvoiceQtyDifferencePercent")
    protected String invoiceQtyDifferencePercent;
    @XmlElement(name = "InvoiceQtyDifferenceQuantity")
    protected String invoiceQtyDifferenceQuantity;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getInvoiceQtyDifferencePercent() {
        return invoiceQtyDifferencePercent;
    }

    public void setInvoiceQtyDifferencePercent(String value) {
        this.invoiceQtyDifferencePercent = value;
    }

    public String getInvoiceQtyDifferenceQuantity() {
        return invoiceQtyDifferenceQuantity;
    }

    public void setInvoiceQtyDifferenceQuantity(String value) {
        this.invoiceQtyDifferenceQuantity = value;
    }

}
