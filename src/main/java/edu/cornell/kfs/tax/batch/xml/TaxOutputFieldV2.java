package edu.cornell.kfs.tax.batch.xml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxFieldType")
@XmlRootElement(name = "field")
public class TaxOutputFieldV2 {

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

    @XmlAttribute(name = "mask")
    private String mask;

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

    public String getMask() {
        return mask;
    }

    public void setMask(final String mask) {
        this.mask = mask;
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

    void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {
        if (key != null) {
            if (value != null) {
                throw new IllegalStateException("'key' and 'value' attributes on 'field' tags are mutually exclusive");
            } else if (StringUtils.isBlank(key)) {
                throw new IllegalStateException(
                        "'field' tags that specify the 'key' attribute must not set a blank key");
            } else if (type == TaxOutputFieldType.STATIC) {
                throw new IllegalStateException(
                        "'field' tags that specify the 'key' attribute must not set 'type' to STATIC");
            } else if ((type == TaxOutputFieldType.SENSITIVE_STRING) != StringUtils.isNotBlank(mask)) {
                throw new IllegalStateException(
                        "On 'field' tags, the 'mask' attribute must be non-blank for SENSITIVE_STRING types "
                                + "and blank/unset for all other types");
            }
        } else if (StringUtils.isNotBlank(value)) {
            if (type != TaxOutputFieldType.STATIC) {
                throw new IllegalStateException(
                        "'field' tags that specify the 'value' attribute must set 'type' to STATIC");
            }
        } else if (type == TaxOutputFieldType.STATIC) {
            if (value == null) {
                throw new IllegalStateException("'field' tags with 'type' of STATIC that are meant to have empty data "
                        + "must specify an empty 'value' attribute");
            }
        } else {
            throw new IllegalStateException("'field' tags must specify either the 'key' or 'value' attribute");
        }
    }

}
