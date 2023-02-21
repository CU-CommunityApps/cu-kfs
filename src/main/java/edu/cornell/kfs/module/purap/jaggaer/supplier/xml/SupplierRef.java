
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "baselineSupplierNumber", "baselineThirdPartyRefNumber", "error" })
@XmlRootElement(name = "SupplierRef")
public class SupplierRef {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlElement(name = "BaselineSupplierNumber", required = true)
    protected BaselineSupplierNumber baselineSupplierNumber;
    @XmlElement(name = "BaselineThirdPartyRefNumber", required = true)
    protected BaselineThirdPartyRefNumber baselineThirdPartyRefNumber;
    @XmlElement(name = "Error")
    protected List<Error> error;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public BaselineSupplierNumber getBaselineSupplierNumber() {
        return baselineSupplierNumber;
    }

    public void setBaselineSupplierNumber(BaselineSupplierNumber value) {
        this.baselineSupplierNumber = value;
    }

    public BaselineThirdPartyRefNumber getBaselineThirdPartyRefNumber() {
        return baselineThirdPartyRefNumber;
    }

    public void setBaselineThirdPartyRefNumber(BaselineThirdPartyRefNumber value) {
        this.baselineThirdPartyRefNumber = value;
    }

    public List<Error> getError() {
        if (error == null) {
            error = new ArrayList<Error>();
        }
        return this.error;
    }

}
