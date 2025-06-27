package edu.cornell.kfs.sys.dataaccess.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.ojb.broker.metadata.ClassDescriptor;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;
import edu.cornell.kfs.sys.xmladapters.UppercasedStringXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Convenience DTO for parsing custom OJB class descriptors.
 * This DTO currently only preserves the following data:
 * 
 * -- The "class" attribute
 * -- The "table" attribute
 * -- The "field-descriptor" sub-elements
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fieldDescriptors" })
@XmlRootElement(name = "class-descriptor")
public class TestClassDescriptorDto {

    @XmlAttribute(name = "class")
    private Class<?> mappedClass;

    @XmlAttribute(name = "table")
    @XmlJavaTypeAdapter(UppercasedStringXmlAdapter.class)
    private String tableName;

    @XmlElement(name = "field-descriptor")
    private List<TestFieldDescriptorDto> fieldDescriptors;

    public Class<?> getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(final Class<?> mappedClass) {
        this.mappedClass = mappedClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public List<TestFieldDescriptorDto> getFieldDescriptors() {
        if (fieldDescriptors == null) {
            fieldDescriptors = new ArrayList<>();
        }
        return fieldDescriptors;
    }

    public void setFieldDescriptors(final List<TestFieldDescriptorDto> fieldDescriptors) {
        this.fieldDescriptors = fieldDescriptors;
    }

    public ClassDescriptor toOjbClassDescriptor() {
        return TestOjbMetadataUtils.createMockClassDescriptor(mappedClass, tableName,
                getFieldDescriptors(), TestFieldDescriptorDto::toOjbFieldDescriptor);
    }

}
