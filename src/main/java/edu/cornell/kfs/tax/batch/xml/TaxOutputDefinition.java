package edu.cornell.kfs.tax.batch.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxOutputDefinitionType", propOrder = {
    "sections"
})
@XmlRootElement(name = "taxOutputDefinition")
public class TaxOutputDefinition {

    @XmlElement(name = "section", required = true)
    private List<TaxOutputSection> sections;

    @XmlAttribute(name = "fieldSeparator", required = true)
    private String fieldSeparator;

    public List<TaxOutputSection> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        return sections;
    }

    public void setSections(final List<TaxOutputSection> sections) {
        this.sections = sections;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(final String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

}
