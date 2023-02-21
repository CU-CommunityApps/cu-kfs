
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fob", "surchargeConfiguration" })
@XmlRootElement(name = "Shipping")
public class Shipping {

    @XmlElement(name = "FOB")
    protected FOB fob;
    @XmlElement(name = "SurchargeConfiguration", required = true)
    protected SurchargeConfiguration surchargeConfiguration;

    public FOB getFOB() {
        return fob;
    }

    public void setFOB(FOB value) {
        this.fob = value;
    }

    public SurchargeConfiguration getSurchargeConfiguration() {
        return surchargeConfiguration;
    }

    public void setSurchargeConfiguration(SurchargeConfiguration value) {
        this.surchargeConfiguration = value;
    }

}
