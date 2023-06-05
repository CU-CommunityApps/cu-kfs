package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "secondaryNaicsItems" })
@XmlRootElement(name = "SecondaryNaicsList")
public class SecondaryNaicsList implements NaicsCodeListItem {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "SecondaryNaics")
    private List<SecondaryNaicsItem> secondaryNaicsItems;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public List<SecondaryNaicsItem> getSecondaryNaicsItems() {
        if (secondaryNaicsItems == null) {
            secondaryNaicsItems = new ArrayList<SecondaryNaicsItem>();
        }
        return secondaryNaicsItems;
    }

}
