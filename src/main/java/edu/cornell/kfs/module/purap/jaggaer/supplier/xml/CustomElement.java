
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
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customElementIdentifier", "displayName", "customElementDetails" })
@XmlRootElement(name = "CustomElement")
public class CustomElement {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlAttribute(name = "isActive")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isActive;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;
    @XmlElement(name = "CustomElementIdentifier")
    private JaggaerBasicValue customElementIdentifier;
    @XmlElement(name = "DisplayName")
    private DisplayName displayName;
    @XmlElements({ @XmlElement(name = "CustomElementValueList", type = CustomElementValueList.class),
            @XmlElement(name = "Attachments", type = AttachmentList.class) })
    private List<CustomElementDetail> customElementDetails;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JaggaerBasicValue getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    public void setCustomElementIdentifier(JaggaerBasicValue customElementIdentifier) {
        this.customElementIdentifier = customElementIdentifier;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName displayName) {
        this.displayName = displayName;
    }

    public List<CustomElementDetail> getCustomElementDetails() {
        if (customElementDetails == null) {
            customElementDetails = new ArrayList<CustomElementDetail>();
        }
        return customElementDetails;
    }

}
