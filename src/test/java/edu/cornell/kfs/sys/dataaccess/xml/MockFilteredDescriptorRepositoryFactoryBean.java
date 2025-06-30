package edu.cornell.kfs.sys.dataaccess.xml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.util.CuXMLStreamUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

/**
 * Helper factory bean for constructing a mock OJB repository DTO from a list of OJB repository files.
 * Only the class descriptors specified in the "descriptorsToKeep" Set will be preserved; the rest
 * will be filtered out during the XML unmarshalling process. The "descriptorsToKeep" items can be
 * either classnames or table names.
 */
public class MockFilteredDescriptorRepositoryFactoryBean extends AbstractFactoryBean<DescriptorRepository> {

    private static final String CLASS_DESCRIPTOR_ELEMENT = "class-descriptor";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TABLE_ATTRIBUTE = "table";

    private List<String> ojbRepositoryFiles;
    private Set<String> descriptorsToKeep;

    public List<String> getOjbRepositoryFiles() {
        return ojbRepositoryFiles;
    }

    public void setOjbRepositoryFiles(final List<String> ojbRepositoryFiles) {
        this.ojbRepositoryFiles = ojbRepositoryFiles;
    }

    public Set<String> getDescriptorsToKeep() {
        return descriptorsToKeep;
    }

    public void setDescriptorsToKeep(final Set<String> descriptorsToKeep) {
        this.descriptorsToKeep = descriptorsToKeep;
    }

    @Override
    protected DescriptorRepository createInstance() throws Exception {
        final List<TestDescriptorRepositoryDto> repositories = readOjbRepositories();
        return TestDescriptorRepositoryDto.createCombinedOjbDescriptorRepository(repositories);
    }

    @Override
    public Class<?> getObjectType() {
        return DescriptorRepository.class;
    }

    private List<TestDescriptorRepositoryDto> readOjbRepositories() throws Exception {
        final Stream.Builder<TestDescriptorRepositoryDto> repositories = Stream.builder();
        final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        final JAXBContext jaxbContext = JAXBContext.newInstance(TestDescriptorRepositoryDto.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        
        for (final String ojbRepositoryFile : ojbRepositoryFiles) {
            XMLStreamReader xmlReader = null;
            XMLStreamReader filteredReader = null;
            try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(ojbRepositoryFile)) {
                xmlReader = inputFactory.createXMLStreamReader(fileStream, StandardCharsets.UTF_8.name());
                filteredReader = inputFactory.createFilteredReader(xmlReader,
                        new ClassDescriptorFilter(descriptorsToKeep));
                final TestDescriptorRepositoryDto repository =
                        (TestDescriptorRepositoryDto) unmarshaller.unmarshal(filteredReader);
                repositories.add(repository);
            } finally {
                CuXMLStreamUtils.closeQuietly(filteredReader);
                CuXMLStreamUtils.closeQuietly(xmlReader);
            }
        }

        return repositories.build().collect(Collectors.toUnmodifiableList());
    }

    private static final class ClassDescriptorFilter implements StreamFilter {

        private final Set<String> descriptorsToKeep;
        private boolean currentlySkippingClassDescriptor = false;
        private int localDepth;

        private ClassDescriptorFilter(final Set<String> descriptorsToKeep) {
            this.descriptorsToKeep = descriptorsToKeep;
        }

        @Override
        public boolean accept(final XMLStreamReader reader) {
            switch (reader.getEventType()) {
                case XMLStreamReader.START_ELEMENT:
                    return shouldAcceptElementStart(reader);
                case XMLStreamReader.END_ELEMENT:
                    return shouldAcceptElementEnd(reader);
                default:
                    return !currentlySkippingClassDescriptor;
            }
        }

        private boolean shouldAcceptElementStart(final XMLStreamReader reader) {
            if (currentlySkippingClassDescriptor) {
                localDepth++;
                return false;
            } else if (StringUtils.equals(reader.getLocalName(), CLASS_DESCRIPTOR_ELEMENT)) {
                final String className = reader.getAttributeValue(null, CLASS_ATTRIBUTE);
                final String tableName = reader.getAttributeValue(null, TABLE_ATTRIBUTE);
                if (descriptorsToKeep.contains(className) || descriptorsToKeep.contains(tableName)) {
                    return true;
                } else {
                    currentlySkippingClassDescriptor = true;
                    localDepth = 1;
                    return false;
                }
            } else {
                return true;
            }
        }

        private boolean shouldAcceptElementEnd(final XMLStreamReader reader) {
            if (currentlySkippingClassDescriptor) {
                localDepth--;
                if (localDepth <= 0) {
                    currentlySkippingClassDescriptor = false;
                }
                return false;
            } else {
                return true;
            }
        }
    }

}
