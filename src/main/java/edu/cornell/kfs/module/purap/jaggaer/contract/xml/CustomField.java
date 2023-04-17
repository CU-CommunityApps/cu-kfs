package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "internalName",
    "customFieldValues"
})
@XmlRootElement(name = "CustomField")
public class CustomField {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;

    @XmlElement(name = "InternalName", required = true)
    private String internalName;

    @XmlElementWrapper(name = "CustomFieldValueList")
    @XmlElement(name = "CustomFieldValue")
    private List<String> customFieldValues;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<String> getCustomFieldValues() {
        if (customFieldValues == null) {
            customFieldValues = new ArrayList<>();
        }
        return customFieldValues;
    }

    public void setCustomFieldValues(List<String> customFieldValues) {
        this.customFieldValues = customFieldValues;
    }

}
