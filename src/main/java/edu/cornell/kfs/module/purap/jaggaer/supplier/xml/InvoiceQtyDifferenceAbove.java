
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
@XmlType(name = "", propOrder = { "percent", "quantity" })
@XmlRootElement(name = "InvoiceQtyDifferenceAbove")
public class InvoiceQtyDifferenceAbove {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "Percent")
    protected String percent;
    @XmlElement(name = "Quantity")
    protected String quantity;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String value) {
        this.percent = value;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String value) {
        this.quantity = value;
    }

}
