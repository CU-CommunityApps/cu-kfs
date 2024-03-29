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
@XmlType(name = "", propOrder = { "additionalDataItems" })
@XmlRootElement(name = "AdditionalDataList")
public class AdditionalDataList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "AdditionalData")
    private List<AdditionalDataItem> additionalDataItems;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public List<AdditionalDataItem> getAdditionalDataItems() {
        if (additionalDataItems == null) {
            additionalDataItems = new ArrayList<AdditionalDataItem>();
        }
        return additionalDataItems;
    }

}
