
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "invoiceCostDifferencePercent", "invoiceCostDifferenceAmount" })
@XmlRootElement(name = "InvoiceCostDifference")
public class InvoiceCostDifference {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "InvoiceCostDifferencePercent")
    protected String invoiceCostDifferencePercent;
    @XmlElement(name = "InvoiceCostDifferenceAmount")
    protected String invoiceCostDifferenceAmount;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getInvoiceCostDifferencePercent() {
        return invoiceCostDifferencePercent;
    }

    public void setInvoiceCostDifferencePercent(String value) {
        this.invoiceCostDifferencePercent = value;
    }

    public String getInvoiceCostDifferenceAmount() {
        return invoiceCostDifferenceAmount;
    }

    public void setInvoiceCostDifferenceAmount(String value) {
        this.invoiceCostDifferenceAmount = value;
    }

}
