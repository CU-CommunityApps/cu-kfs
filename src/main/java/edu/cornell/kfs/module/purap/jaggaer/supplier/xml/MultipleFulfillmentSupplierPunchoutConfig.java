
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "multipleFulfillmentSupplier", "linkValueLocation", "overiddenLinkValue" })
@XmlRootElement(name = "MultipleFulfillmentSupplierPunchoutConfig")
public class MultipleFulfillmentSupplierPunchoutConfig {

    @XmlElement(name = "MultipleFulfillmentSupplier")
    protected MultipleFulfillmentSupplier multipleFulfillmentSupplier;
    @XmlElement(name = "LinkValueLocation")
    protected LinkValueLocation linkValueLocation;
    @XmlElement(name = "OveriddenLinkValue")
    protected List<OveriddenLinkValue> overiddenLinkValue;

    public MultipleFulfillmentSupplier getMultipleFulfillmentSupplier() {
        return multipleFulfillmentSupplier;
    }

    public void setMultipleFulfillmentSupplier(MultipleFulfillmentSupplier value) {
        this.multipleFulfillmentSupplier = value;
    }

    public LinkValueLocation getLinkValueLocation() {
        return linkValueLocation;
    }

    public void setLinkValueLocation(LinkValueLocation value) {
        this.linkValueLocation = value;
    }

    public List<OveriddenLinkValue> getOveriddenLinkValue() {
        if (overiddenLinkValue == null) {
            overiddenLinkValue = new ArrayList<OveriddenLinkValue>();
        }
        return this.overiddenLinkValue;
    }

}
