
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "BusinessUnit")
public class BusinessUnit {

    @XmlAttribute(name = "useParentAsPreferredFulfillmentCenter")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useParentAsPreferredFulfillmentCenter;
    @XmlValue
    protected String value;

    public String getUseParentAsPreferredFulfillmentCenter() {
        return useParentAsPreferredFulfillmentCenter;
    }

    public void setUseParentAsPreferredFulfillmentCenter(String value) {
        this.useParentAsPreferredFulfillmentCenter = value;
    }

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

}
