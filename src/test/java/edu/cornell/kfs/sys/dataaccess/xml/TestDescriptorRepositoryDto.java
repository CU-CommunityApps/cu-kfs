package edu.cornell.kfs.sys.dataaccess.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ojb.broker.metadata.DescriptorRepository;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Convenience DTO for parsing custom OJB descriptor repositories.
 * This DTO currently only preserves the following data:
 * 
 * -- The "class-descriptor" sub-elements
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "classDescriptors" })
@XmlRootElement(name = "descriptor-repository")
public class TestDescriptorRepositoryDto {

    @XmlElement(name = "class-descriptor")
    private List<TestClassDescriptorDto> classDescriptors;

    public List<TestClassDescriptorDto> getClassDescriptors() {
        if (classDescriptors == null) {
            classDescriptors = new ArrayList<>();
        }
        return classDescriptors;
    }

    public void setClassDescriptors(final List<TestClassDescriptorDto> classDescriptors) {
        this.classDescriptors = classDescriptors;
    }

    public DescriptorRepository toOjbDescriptorRepository() {
        return TestOjbMetadataUtils.createMockDescriptorRepository(getClassDescriptors(),
                TestClassDescriptorDto::toOjbClassDescriptor);
    }

    public static DescriptorRepository createCombinedOjbDescriptorRepository(
            final List<TestDescriptorRepositoryDto> repositories) {
        final List<TestClassDescriptorDto> xmlClassDescriptors = repositories.stream()
                .map(TestDescriptorRepositoryDto::getClassDescriptors)
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());

        return TestOjbMetadataUtils.createMockDescriptorRepository(xmlClassDescriptors,
                TestClassDescriptorDto::toOjbClassDescriptor);
    }

}
