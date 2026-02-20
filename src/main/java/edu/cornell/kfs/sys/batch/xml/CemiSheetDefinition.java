package edu.cornell.kfs.sys.batch.xml;

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
@XmlType(name = "cemiSheetType", propOrder = {
    "fields"
})
@XmlRootElement(name = "sheet")
public class CemiSheetDefinition {

    @XmlElement(name = "field", required = true)
    private List<CemiFieldDefinition> fields;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "num-header-rows", required = true)
    private int numHeaderRows;

    @XmlAttribute(name = "start-column-index", required = true)
    private int startColumnIndex;

    public List<CemiFieldDefinition> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    public void setFields(final List<CemiFieldDefinition> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getNumHeaderRows() {
        return numHeaderRows;
    }

    public void setNumHeaderRows(final int numHeaderRows) {
        this.numHeaderRows = numHeaderRows;
    }

    public int getStartColumnIndex() {
        return startColumnIndex;
    }

    public void setStartColumnIndex(final int startColumnIndex) {
        this.startColumnIndex = startColumnIndex;
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
