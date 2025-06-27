package edu.cornell.kfs.sys.dataaccess.xml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.util.CuMockBuilder;
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
            try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(ojbRepositoryFile)) {
                xmlReader = inputFactory.createXMLStreamReader(fileStream, StandardCharsets.UTF_8.name());
                final XMLStreamReader wrappedReader = wrapXMLStreamReader(xmlReader);
                final TestDescriptorRepositoryDto repository =
                        (TestDescriptorRepositoryDto) unmarshaller.unmarshal(wrappedReader);
                repositories.add(repository);
            } finally {
                CuXMLStreamUtils.closeQuietly(xmlReader);
            }
        }

        return repositories.build().collect(Collectors.toUnmodifiableList());
    }

    private XMLStreamReader wrapXMLStreamReader(final XMLStreamReader xmlReader) throws XMLStreamException {
        return new CuMockBuilder<>(xmlReader)
                .withAnswer(XMLStreamReader::next,
                        invocation -> advanceToNextEventAndSkipDescriptorIfNecessary(xmlReader))
                .withAnswer(XMLStreamReader::nextTag,
                        invocation -> advanceToNextTagAndSkipDescriptorIfNecessary(xmlReader))
                .build();
    }

    private int advanceToNextEventAndSkipDescriptorIfNecessary(final XMLStreamReader xmlReader)
            throws XMLStreamException {
        return doXMLStreamReaderActionAndSkipDescriptorIfNecessary(xmlReader, XMLStreamReader::next);
    }

    private int advanceToNextTagAndSkipDescriptorIfNecessary(final XMLStreamReader xmlReader)
            throws XMLStreamException {
        return doXMLStreamReaderActionAndSkipDescriptorIfNecessary(xmlReader, XMLStreamReader::nextTag);
    }

    private int doXMLStreamReaderActionAndSkipDescriptorIfNecessary(final XMLStreamReader xmlReader,
            final FailableFunction<XMLStreamReader, Integer, XMLStreamException> xmlReaderAction)
                    throws XMLStreamException {
        int currentEventType = xmlReaderAction.apply(xmlReader);
        while (readerIsAtStartOfSkippableClassDescriptor(xmlReader)) {
            skipCurrentClassDescriptorElement(xmlReader);
            currentEventType = xmlReaderAction.apply(xmlReader);
        }
        return currentEventType;
    }

    private boolean readerIsAtStartOfSkippableClassDescriptor(final XMLStreamReader xmlReader) {
        if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT
                && StringUtils.equals(xmlReader.getLocalName(), CLASS_DESCRIPTOR_ELEMENT)) {
            final String className = xmlReader.getAttributeValue(null, CLASS_ATTRIBUTE);
            final String tableName = xmlReader.getAttributeValue(null, TABLE_ATTRIBUTE);
            return !descriptorsToKeep.contains(className) && !descriptorsToKeep.contains(tableName);
        } else {
            return false;
        }
    }

    private void skipCurrentClassDescriptorElement(final XMLStreamReader xmlReader) throws XMLStreamException {
        int currentEventType;
        int localDepth = 1;
        while (localDepth > 0) {
            currentEventType = xmlReader.next();
            if (currentEventType == XMLStreamReader.START_ELEMENT) {
                localDepth++;
            } else if (currentEventType == XMLStreamReader.END_ELEMENT) {
                localDepth--;
            }
        }
    }

}
