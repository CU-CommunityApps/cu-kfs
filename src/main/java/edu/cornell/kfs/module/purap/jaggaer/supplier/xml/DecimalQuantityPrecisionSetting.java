
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
