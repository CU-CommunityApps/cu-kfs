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
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customElementIdentifier", "displayName", "customElementValueListOrAttachments" })
@XmlRootElement(name = "CustomElement")
public class CustomElement {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "isActive")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isActive;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlElement(name = "CustomElementIdentifier")
    protected CustomElementIdentifier customElementIdentifier;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;
    @XmlElements({ @XmlElement(name = "CustomElementValueList", type = CustomElementValueList.class),
            @XmlElement(name = "Attachments", type = Attachments.class) })
    protected List<Object> customElementValueListOrAttachments;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String value) {
        this.isActive = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public CustomElementIdentifier getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    public void setCustomElementIdentifier(CustomElementIdentifier value) {
        this.customElementIdentifier = value;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName value) {
        this.displayName = value;
    }

    public List<Object> getCustomElementValueListOrAttachments() {
        if (customElementValueListOrAttachments == null) {
            customElementValueListOrAttachments = new ArrayList<Object>();
        }
        return this.customElementValueListOrAttachments;
    }

}
