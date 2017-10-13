package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = StringUtils.EMPTY, propOrder = {
    "description"
})
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
