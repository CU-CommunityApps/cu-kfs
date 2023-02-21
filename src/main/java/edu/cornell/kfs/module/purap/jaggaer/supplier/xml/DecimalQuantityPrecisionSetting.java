
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "maxPrecisionDigits", "orgEnabledMaxPrecisionDigits" })
@XmlRootElement(name = "DecimalQuantityPrecisionSetting")
public class DecimalQuantityPrecisionSetting {

    @XmlElement(name = "MaxPrecisionDigits")
    protected MaxPrecisionDigits maxPrecisionDigits;
    @XmlElement(name = "OrgEnabledMaxPrecisionDigits")
    protected OrgEnabledMaxPrecisionDigits orgEnabledMaxPrecisionDigits;

    public MaxPrecisionDigits getMaxPrecisionDigits() {
        return maxPrecisionDigits;
    }

    public void setMaxPrecisionDigits(MaxPrecisionDigits value) {
        this.maxPrecisionDigits = value;
    }

    public OrgEnabledMaxPrecisionDigits getOrgEnabledMaxPrecisionDigits() {
        return orgEnabledMaxPrecisionDigits;
    }

    public void setOrgEnabledMaxPrecisionDigits(OrgEnabledMaxPrecisionDigits value) {
        this.orgEnabledMaxPrecisionDigits = value;
    }

}
