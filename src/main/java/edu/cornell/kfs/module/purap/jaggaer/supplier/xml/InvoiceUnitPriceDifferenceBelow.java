
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
@XmlType(name = "", propOrder = { "percent", "amount" })
@XmlRootElement(name = "InvoiceUnitPriceDifferenceBelow")
public class InvoiceUnitPriceDifferenceBelow {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "Percent")
    protected String percent;
    @XmlElement(name = "Amount")
    protected Amount amount;

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

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount value) {
        this.amount = value;
    }

}
