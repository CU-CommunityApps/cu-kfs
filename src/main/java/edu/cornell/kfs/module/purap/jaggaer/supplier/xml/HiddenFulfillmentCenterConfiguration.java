
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "poConfiguration", "priceAndAvailability", "addressType" })
@XmlRootElement(name = "HiddenFulfillmentCenterConfiguration")
public class HiddenFulfillmentCenterConfiguration {

    @XmlElement(name = "POConfiguration")
    protected POConfiguration poConfiguration;
    @XmlElement(name = "PriceAndAvailability")
    protected PriceAndAvailability priceAndAvailability;
    @XmlElement(name = "AddressType")
    protected String addressType;

    public POConfiguration getPOConfiguration() {
        return poConfiguration;
    }

    public void setPOConfiguration(POConfiguration value) {
        this.poConfiguration = value;
    }

    public PriceAndAvailability getPriceAndAvailability() {
        return priceAndAvailability;
    }

    public void setPriceAndAvailability(PriceAndAvailability value) {
        this.priceAndAvailability = value;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String value) {
        this.addressType = value;
    }

}
