
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "logoFileName", "punchoutConfiguration" })
@XmlRootElement(name = "HiddenConfiguration")
public class HiddenConfiguration {

    @XmlElement(name = "LogoFileName")
    protected LogoFileName logoFileName;
    @XmlElement(name = "PunchoutConfiguration")
    protected PunchoutConfiguration punchoutConfiguration;

    public LogoFileName getLogoFileName() {
        return logoFileName;
    }

    public void setLogoFileName(LogoFileName value) {
        this.logoFileName = value;
    }

    public PunchoutConfiguration getPunchoutConfiguration() {
        return punchoutConfiguration;
    }

    public void setPunchoutConfiguration(PunchoutConfiguration value) {
        this.punchoutConfiguration = value;
    }

}
