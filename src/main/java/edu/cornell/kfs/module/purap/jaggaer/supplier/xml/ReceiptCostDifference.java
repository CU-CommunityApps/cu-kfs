
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
@XmlType(name = "", propOrder = { "receiptCostDifferencePercent", "receiptCostDifferenceAmount" })
@XmlRootElement(name = "ReceiptCostDifference")
public class ReceiptCostDifference {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "ReceiptCostDifferencePercent")
    protected String receiptCostDifferencePercent;
    @XmlElement(name = "ReceiptCostDifferenceAmount")
    protected String receiptCostDifferenceAmount;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getReceiptCostDifferencePercent() {
        return receiptCostDifferencePercent;
    }

    public void setReceiptCostDifferencePercent(String value) {
        this.receiptCostDifferencePercent = value;
    }

    public String getReceiptCostDifferenceAmount() {
        return receiptCostDifferenceAmount;
    }

    public void setReceiptCostDifferenceAmount(String value) {
        this.receiptCostDifferenceAmount = value;
    }

}
