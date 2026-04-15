package edu.cornell.kfs.sys.batch.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cemiOutputDefinitionType", propOrder = {
    "sheets"
})
@XmlRootElement(name = "cemiOutputDefinition")
public class CemiOutputDefinition {

    @XmlElement(name = "sheet", required = true)
    private List<CemiSheetDefinition> sheets;

    public List<CemiSheetDefinition> getSheets() {
        if (sheets == null) {
            sheets = new ArrayList<>();
        }
        return sheets;
    }

    public void setSheets(final List<CemiSheetDefinition> sheets) {
        this.sheets = sheets;
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
