package edu.cornell.kfs.sys.dataaccess.xml;

import java.sql.JDBCType;

import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;
import org.apache.ojb.broker.metadata.FieldDescriptor;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;
import edu.cornell.kfs.sys.xmladapters.JDBCTypeXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.UppercasedStringXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Convenience DTO for parsing custom OJB field descriptors.
 * This DTO currently only preserves the following data:
 * 
 * -- The "name" attribute
 * -- The "column" attribute
 * -- The "jdbc-type" attribute
 * -- The "conversion" attribute
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "field-descriptor")
public class TestFieldDescriptorDto {

    @XmlAttribute(name = "name")
    private String fieldName;

    @XmlAttribute(name = "column")
    @XmlJavaTypeAdapter(UppercasedStringXmlAdapter.class)
    private String columnName;

    @XmlAttribute(name = "jdbc-type")
    @XmlJavaTypeAdapter(JDBCTypeXmlAdapter.class)
    private JDBCType jdbcType;

    @XmlAttribute(name = "conversion")
    private Class<? extends FieldConversion> conversionClass;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(final JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public Class<? extends FieldConversion> getConversionClass() {
        return conversionClass;
    }

    public void setConversionClass(final Class<? extends FieldConversion> conversionClass) {
        this.conversionClass = conversionClass;
    }

    public FieldDescriptor toOjbFieldDescriptor() {
        return TestOjbMetadataUtils.createMockFieldDescriptor(fieldName, columnName, jdbcType, conversionClass);
    }

}
