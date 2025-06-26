package edu.cornell.kfs.sys.dataaccess.xml;

import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;

import edu.cornell.kfs.sys.util.CuMockBuilder;

public class MockFilteredDescriptorRepositoryFactoryBean {

    private static final String CLASS_DESCRIPTOR_ELEMENT = "class-descriptor";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TABLE_ATTRIBUTE = "table";

    private List<String> ojbSourceFiles;
    private Set<String> descriptorsToKeep;

    private XMLStreamReader wrapXMLStreamReader(final XMLStreamReader streamReader) throws XMLStreamException {
        return new CuMockBuilder<>(streamReader)
                .withAnswer(XMLStreamReader::next, invocation -> findNextNodeAndSkipDescriptorIfNecessary(streamReader))
                .withAnswer(XMLStreamReader::nextTag, invocation -> findNextTagAndSkipDescriptorIfNecessary(streamReader))
                .build();
    }

    private int findNextNodeAndSkipDescriptorIfNecessary(final XMLStreamReader streamReader) throws XMLStreamException {
        return -1;
    }

    private int findNextTagAndSkipDescriptorIfNecessary(final XMLStreamReader streamReader) throws XMLStreamException {
        return -1;
    }

    private int doStreamReaderActionAndSkipDescriptorIfNecessary(final XMLStreamReader streamReader,
            FailableFunction<XMLStreamReader, Integer, XMLStreamException> streamReaderAction) throws XMLStreamException {
        int currentEventType = streamReaderAction.apply(streamReader);
        while (readerIsAtStartOfSkippableClassDescriptor(streamReader)) {
            int localDepth = 1;
            while (localDepth > 0) {
                currentEventType = streamReader.next();
                if (currentEventType == XMLStreamReader.START_ELEMENT) {
                    localDepth++;
                } else if (currentEventType == XMLStreamReader.END_ELEMENT) {
                    localDepth--;
                }
            }
            currentEventType = streamReaderAction.apply(streamReader);
        }
        return currentEventType;
    }

    private boolean readerIsAtStartOfSkippableClassDescriptor(final XMLStreamReader streamReader) {
        if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT
                && StringUtils.equals(streamReader.getLocalName(), CLASS_DESCRIPTOR_ELEMENT)) {
            final String className = streamReader.getAttributeValue(null, CLASS_ATTRIBUTE);
            final String tableName = streamReader.getAttributeValue(null, TABLE_ATTRIBUTE);
            return !descriptorsToKeep.contains(className) && !descriptorsToKeep.contains(tableName);
        } else {
            return false;
        }
    }

}
