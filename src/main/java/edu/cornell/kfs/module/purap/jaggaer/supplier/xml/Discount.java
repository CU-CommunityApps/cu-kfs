
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "Discount")
public class Discount {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlAttribute(name = "unit", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;
    @XmlAttribute(name = "currency")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String currency;
    @XmlValue
    protected String value;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String value) {
        this.unit = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

}
