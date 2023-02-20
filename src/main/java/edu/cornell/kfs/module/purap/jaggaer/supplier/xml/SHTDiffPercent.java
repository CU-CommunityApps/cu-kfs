
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "shtDiffPercentTotal", "shtDiffPercentAmount" })
@XmlRootElement(name = "SHTDiffPercent")
public class SHTDiffPercent {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "SHTDiffPercentTotal")
    protected String shtDiffPercentTotal;
    @XmlElement(name = "SHTDiffPercentAmount")
    protected String shtDiffPercentAmount;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public String getSHTDiffPercentTotal() {
        return shtDiffPercentTotal;
    }

    public void setSHTDiffPercentTotal(String value) {
        this.shtDiffPercentTotal = value;
    }

    public String getSHTDiffPercentAmount() {
        return shtDiffPercentAmount;
    }

    public void setSHTDiffPercentAmount(String value) {
        this.shtDiffPercentAmount = value;
    }

}
