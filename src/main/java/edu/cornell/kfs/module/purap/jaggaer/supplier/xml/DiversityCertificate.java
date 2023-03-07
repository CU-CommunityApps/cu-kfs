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
@XmlType(name = "", propOrder = {
    "attachments"
})
@XmlRootElement(name = "DiversityCertificate")
public class DiversityCertificate {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Attachments")
    protected Attachments attachments;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public Attachments getAttachments() {
        return attachments;
    }

    
    public void setAttachments(Attachments value) {
        this.attachments = value;
    }

}
