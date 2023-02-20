
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
@XmlRootElement(name = "Currency")
public class Currency {

    @XmlAttribute(name = "isoCurrencyCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String isoCurrencyCode;
    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlValue
    protected String value;

    public String getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(String value) {
        this.isoCurrencyCode = value;
    }

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

}
