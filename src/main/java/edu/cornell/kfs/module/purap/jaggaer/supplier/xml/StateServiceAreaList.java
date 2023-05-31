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
@XmlType(name = "", propOrder = { "stateServiceAreaInternalNameDetails" })
@XmlRootElement(name = "StateServiceAreaList")
public class StateServiceAreaList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "StateServiceAreaInternalName")
    private List<StateServiceAreaInternalName> stateServiceAreaInternalNameDetails;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public List<StateServiceAreaInternalName> getStateServiceAreaInternalNameDetails() {
        if (stateServiceAreaInternalNameDetails == null) {
            stateServiceAreaInternalNameDetails = new ArrayList<StateServiceAreaInternalName>();
        }
        return stateServiceAreaInternalNameDetails;
    }

}
