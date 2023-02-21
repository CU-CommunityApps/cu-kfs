
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
