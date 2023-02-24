package edu.cornell.kfs.module.purap.jaggaer.xml;

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
@XmlType(name = "", propOrder = { "primaryNaicsOrSecondaryNaicsList" })
@XmlRootElement(name = "NaicsCodes")
public class NaicsCodes {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElements({ @XmlElement(name = "PrimaryNaics", type = PrimaryNaics.class),
            @XmlElement(name = "SecondaryNaicsList", type = SecondaryNaicsList.class) })
    protected List<PimaryNaicsOrSecondaryNaicsListItemInterface> primaryNaicsOrSecondaryNaicsList;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public List<PimaryNaicsOrSecondaryNaicsListItemInterface> getPrimaryNaicsOrSecondaryNaicsList() {
        if (primaryNaicsOrSecondaryNaicsList == null) {
            primaryNaicsOrSecondaryNaicsList = new ArrayList<PimaryNaicsOrSecondaryNaicsListItemInterface>();
        }
        return this.primaryNaicsOrSecondaryNaicsList;
    }

}
