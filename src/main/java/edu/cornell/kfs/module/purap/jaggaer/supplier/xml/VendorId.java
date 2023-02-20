
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "VendorId")
public class VendorId {

    @XmlAttribute(name = "businessUnit", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String businessUnit;
    @XmlValue
    protected String value;

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String value) {
        this.businessUnit = value;
    }

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

}
