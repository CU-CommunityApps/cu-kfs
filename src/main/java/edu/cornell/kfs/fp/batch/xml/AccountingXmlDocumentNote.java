package edu.cornell.kfs.fp.batch.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Note", namespace = StringUtils.EMPTY)
public class AccountingXmlDocumentNote {

    @XmlElement(name = "Description", namespace = StringUtils.EMPTY, required = true)
    protected String description;

    public AccountingXmlDocumentNote() {
        
    }

    public AccountingXmlDocumentNote(String description) {
        setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
