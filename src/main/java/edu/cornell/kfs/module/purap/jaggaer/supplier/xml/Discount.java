
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
@XmlType(name = "", propOrder = {
    "discountPercentOrDiscountAmountOrIsoCurrencyCode"
})
@XmlRootElement(name = "Discount")
public class Discount {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "unit", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;
    @XmlElements({
        @XmlElement(name = "DiscountPercent", required = true, type = DiscountPercent.class),
        @XmlElement(name = "DiscountAmount", required = true, type = DiscountAmount.class),
        @XmlElement(name = "IsoCurrencyCode", required = true, type = IsoCurrencyCode.class)
    })
    protected List<Object> discountPercentOrDiscountAmountOrIsoCurrencyCode;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public String getUnit() {
        return unit;
    }

    
    public void setUnit(String value) {
        this.unit = value;
    }

    
    public List<Object> getDiscountPercentOrDiscountAmountOrIsoCurrencyCode() {
        if (discountPercentOrDiscountAmountOrIsoCurrencyCode == null) {
            discountPercentOrDiscountAmountOrIsoCurrencyCode = new ArrayList<Object>();
        }
        return this.discountPercentOrDiscountAmountOrIsoCurrencyCode;
    }

}
