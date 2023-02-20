
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
