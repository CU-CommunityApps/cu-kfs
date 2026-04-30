package edu.cornell.kfs.cemi.sys.batch.xml;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants.CemiFieldDefinitionType;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cemiFieldType")
@XmlRootElement(name = "field")
public class CemiFieldDefinition implements Serializable {

    private static final long serialVersionUID = -1143143856634053621L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "type", required = true)
    private CemiFieldDefinitionType type;

    @XmlAttribute(name = "length")
    private int length;

    @XmlAttribute(name = "key")
    private String key;

    @XmlAttribute(name = "value")
    private String value;

    @XmlAttribute(name = "indexes")
    private String indexes;

    public CemiFieldDefinition() {
        this.length = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public CemiFieldDefinitionType getType() {
        return type;
    }

    public void setType(final CemiFieldDefinitionType type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
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

    public String getIndexes() {
        return indexes;
    }

    public void setIndexes(final String indexes) {
        this.indexes = indexes;
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
            } else if (type == CemiFieldDefinitionType.STATIC) {
                throw new IllegalStateException(
                        "'field' tags that specify the 'key' attribute must not set 'type' to STATIC");
            }
        } else if (StringUtils.isNotBlank(value)) {
            if (type != CemiFieldDefinitionType.STATIC) {
                throw new IllegalStateException(
                        "'field' tags that specify the 'value' attribute must set 'type' to STATIC");
            }
        } else if (type == CemiFieldDefinitionType.STATIC) {
            if (value == null) {
                throw new IllegalStateException("'field' tags with 'type' of STATIC that are meant to have empty data "
                        + "must specify an empty 'value' attribute");
            }
        } else {
            throw new IllegalStateException("'field' tags must specify either the 'key' or 'value' attribute");
        }
    }

}
