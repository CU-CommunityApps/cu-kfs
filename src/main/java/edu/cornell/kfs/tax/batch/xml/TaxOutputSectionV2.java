package edu.cornell.kfs.tax.batch.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxSectionType", propOrder = {
    "fields"
})
public class TaxOutputSectionV2 {

    @XmlElement(name="field", required = true)
    private List<TaxOutputFieldV2> fields;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "useExactFieldLengths")
    private boolean useExactFieldLengths;

    public List<TaxOutputFieldV2> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    public void setFields(final List<TaxOutputFieldV2> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isUseExactFieldLengths() {
        return useExactFieldLengths;
    }

    public void setUseExactFieldLengths(final boolean useExactFieldLengths) {
        this.useExactFieldLengths = useExactFieldLengths;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
