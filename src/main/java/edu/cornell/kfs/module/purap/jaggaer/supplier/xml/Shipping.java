
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
