
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "primaryNaicsOrSecondaryNaics" })
@XmlRootElement(name = "NaicsCodes")
public class NaicsCodes {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElements({ @XmlElement(name = "PrimaryNaics", type = PrimaryNaics.class),
            @XmlElement(name = "SecondaryNaics", type = SecondaryNaics.class) })
    protected List<Object> primaryNaicsOrSecondaryNaics;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public List<Object> getPrimaryNaicsOrSecondaryNaics() {
        if (primaryNaicsOrSecondaryNaics == null) {
            primaryNaicsOrSecondaryNaics = new ArrayList<Object>();
        }
        return this.primaryNaicsOrSecondaryNaics;
    }

}
