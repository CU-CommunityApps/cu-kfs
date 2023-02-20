
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
