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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxOutputDefinitionType", propOrder = {
    "sections"
})
@XmlRootElement(name = "taxOutputDefinition")
public class TaxOutputDefinitionV2 {

    @XmlElement(name = "section", required = true)
    private List<TaxOutputSectionV2> sections;

    @XmlAttribute(name = "fieldSeparator", required = true)
    private String fieldSeparator;

    @XmlAttribute(name = "amountFormat", required = false)
    private String amountFormat;

    @XmlAttribute(name = "percentFormat", required = false)
    private String percentFormat;

    @XmlAttribute(name = "includeQuotes", required = false)
    private boolean includeQuotes;

    public TaxOutputDefinitionV2() {
        this.includeQuotes = true;
    }

    public List<TaxOutputSectionV2> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        return sections;
    }

    public void setSections(final List<TaxOutputSectionV2> sections) {
        this.sections = sections;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(final String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getAmountFormat() {
        return amountFormat;
    }

    public void setAmountFormat(final String amountFormat) {
        this.amountFormat = amountFormat;
    }

    public String getPercentFormat() {
        return percentFormat;
    }

    public void setPercentFormat(final String percentFormat) {
        this.percentFormat = percentFormat;
    }

    public boolean isIncludeQuotes() {
        return includeQuotes;
    }

    public void setIncludeQuotes(final boolean includeQuotes) {
        this.includeQuotes = includeQuotes;
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
