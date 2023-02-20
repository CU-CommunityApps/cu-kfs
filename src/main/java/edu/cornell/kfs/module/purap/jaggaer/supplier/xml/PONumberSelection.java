
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
@XmlType(name = "", propOrder = { "numberWheel", "allowFreeForm" })
@XmlRootElement(name = "PONumberSelection")
public class PONumberSelection {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlElement(name = "NumberWheel")
    protected NumberWheel numberWheel;
    @XmlElement(name = "AllowFreeForm")
    protected AllowFreeForm allowFreeForm;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public NumberWheel getNumberWheel() {
        return numberWheel;
    }

    public void setNumberWheel(NumberWheel value) {
        this.numberWheel = value;
    }

    public AllowFreeForm getAllowFreeForm() {
        return allowFreeForm;
    }

    public void setAllowFreeForm(AllowFreeForm value) {
        this.allowFreeForm = value;
    }

}
