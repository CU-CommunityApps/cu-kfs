package edu.cornell.kfs.tax.batch.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxSectionType", propOrder = {
    "fields"
})
public class TaxOutputSection {

    @XmlElement(name="field", required = true)
    private List<TaxOutputField> fields;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "hasHeaderRow")
    private boolean hasHeaderRow;

    @XmlAttribute(name = "useExactFieldLengths")
    private boolean useExactFieldLengths;

    public List<TaxOutputField> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    public void setFields(final List<TaxOutputField> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isHasHeaderRow() {
        return hasHeaderRow;
    }

    public void setHasHeaderRow(final boolean hasHeaderRow) {
        this.hasHeaderRow = hasHeaderRow;
    }

    public boolean isUseExactFieldLengths() {
        return useExactFieldLengths;
    }

    public void setUseExactFieldLengths(final boolean useExactFieldLengths) {
        this.useExactFieldLengths = useExactFieldLengths;
    }

}
