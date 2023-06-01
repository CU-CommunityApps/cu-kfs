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
@XmlType(name = "", propOrder = { "naicsCodeListItems" })
@XmlRootElement(name = "NaicsCodes")
public class NaicsCodeList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElements({ @XmlElement(name = "PrimaryNaics", type = PrimaryNaicsItem.class),
            @XmlElement(name = "SecondaryNaicsList", type = SecondaryNaicsList.class) })
    private List<NaicsCodeListItem> naicsCodeListItems;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public List<NaicsCodeListItem> getNaicsCodeListItems() {
        if (naicsCodeListItems == null) {
            naicsCodeListItems = new ArrayList<NaicsCodeListItem>();
        }
        return naicsCodeListItems;
    }

}
