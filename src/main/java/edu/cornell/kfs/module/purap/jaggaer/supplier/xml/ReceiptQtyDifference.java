
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
@XmlType(name = "", propOrder = { "receiptQtyDifferencePercent", "receiptQtyDifferenceQuantity" })
@XmlRootElement(name = "ReceiptQtyDifference")
public class ReceiptQtyDifference {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "ReceiptQtyDifferencePercent")
    protected String receiptQtyDifferencePercent;
    @XmlElement(name = "ReceiptQtyDifferenceQuantity")
    protected String receiptQtyDifferenceQuantity;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getReceiptQtyDifferencePercent() {
        return receiptQtyDifferencePercent;
    }

    public void setReceiptQtyDifferencePercent(String value) {
        this.receiptQtyDifferencePercent = value;
    }

    public String getReceiptQtyDifferenceQuantity() {
        return receiptQtyDifferenceQuantity;
    }

    public void setReceiptQtyDifferenceQuantity(String value) {
        this.receiptQtyDifferenceQuantity = value;
    }

}
