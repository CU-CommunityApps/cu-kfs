package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "discountItems" })
@XmlRootElement(name = "Discount")
public class Discount {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlAttribute(name = "unit", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String unit;
    @XmlElements({ @XmlElement(name = "DiscountPercent", required = true, type = DiscountPercent.class),
            @XmlElement(name = "DiscountAmount", required = true, type = DiscountAmount.class),
            @XmlElement(name = "IsoCurrencyCode", required = true, type = IsoCurrencyCode.class) })
    private List<DiscountItem> discountItems;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<DiscountItem> getDiscountItems() {
        if (discountItems == null) {
            discountItems = new ArrayList<DiscountItem>();
        }
        return discountItems;
    }

}
