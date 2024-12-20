package edu.cornell.kfs.tax.batch.xml;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxFieldType")
@XmlRootElement(name = "field")
public class TaxOutputField {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "length", required = true)
    private int length;

    @XmlAttribute(name = "type", required = true)
    private TaxOutputFieldType type;

    @XmlAttribute(name = "key")
    private String key;

    @XmlAttribute(name = "value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public TaxOutputFieldType getType() {
        return type;
    }

    public void setType(final TaxOutputFieldType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

}
