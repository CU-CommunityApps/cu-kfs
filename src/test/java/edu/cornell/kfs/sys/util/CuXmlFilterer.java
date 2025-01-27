package edu.cornell.kfs.sys.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableConsumer;

import edu.cornell.kfs.sys.annotation.XmlAttributeMatcher;
import edu.cornell.kfs.sys.annotation.XmlDocumentFilter;
import edu.cornell.kfs.sys.annotation.XmlElementFilter;

/**
 * Convenience class for reading an XML source, removing unwanted content, and writing the filtered contents
 * to the XML destination. Useful for unit/integration tests that want to use a portion of an existing XML file
 * without manually copying and filtering the contents.
 * 
 * To specify what content should be filtered, use the XmlDocumentFilter annotation (and the XmlElementFilter
 * and XmlAttributeMatcher sub-annotations) to configure which child elements to preserve. The root annotation
 * may be placed on a class, field or enum constant; use the appropriate CuXmlFilterer constructor to auto-retrieve
 * the XmlDocumentFilter annotation.
 * 
 * Note that this implementation currently only supports filtering based on annotation values within
 * the direct child elements.
 */
public class CuXmlFilterer {

    private enum FilterMode {
        OUTSIDE_ROOT_ELEMENT,
        PROCESSING_ROOT_ELEMENT,
        PROCESSING_CHILD_ELEMENTS,
        DISCARDING_CONTENT;
    }

    private static final FailableConsumer<CuXmlFilterer, XMLStreamException> NO_OP_HANDLER = filterer -> {};

    private static final Map<Integer, FailableConsumer<CuXmlFilterer, XMLStreamException>> EVENT_HANDLERS =
            Map.ofEntries(
                    Map.entry(XMLStreamConstants.START_ELEMENT, CuXmlFilterer::handleElementStart),
                    Map.entry(XMLStreamConstants.END_ELEMENT, CuXmlFilterer::handleElementEnd),
                    Map.entry(XMLStreamConstants.PROCESSING_INSTRUCTION, CuXmlFilterer::handleProcessingInstruction),
                    Map.entry(XMLStreamConstants.CHARACTERS, CuXmlFilterer::handleCharacters),
                    Map.entry(XMLStreamConstants.START_DOCUMENT, CuXmlFilterer::handleDocumentStart),
                    Map.entry(XMLStreamConstants.END_DOCUMENT, CuXmlFilterer::handleDocumentEnd),
                    Map.entry(XMLStreamConstants.ENTITY_REFERENCE, CuXmlFilterer::handleEntityReference),
                    Map.entry(XMLStreamConstants.NAMESPACE, CuXmlFilterer::handleNamespace)
            );

    private static final Map<FilterMode, FailableConsumer<CuXmlFilterer, XMLStreamException>> ELEMENT_START_HANDLERS =
            Map.ofEntries(
                    Map.entry(FilterMode.OUTSIDE_ROOT_ELEMENT, CuXmlFilterer::handleRootElementStart),
                    Map.entry(FilterMode.PROCESSING_ROOT_ELEMENT, CuXmlFilterer::handleChildElementStart),
                    Map.entry(FilterMode.PROCESSING_CHILD_ELEMENTS, CuXmlFilterer::handleNestedChildElementStart),
                    Map.entry(FilterMode.DISCARDING_CONTENT, NO_OP_HANDLER)
            );



    private final XMLStreamReader xmlReader;
    private final XMLStreamWriter xmlWriter;
    private final String expectedRootElementName;
    private final Map<String, ElementFilter> elementFilters;

    private StringBuilder whitespaceBetweenChildElements;
    private FilterModeStackElement modesStackTop;
    private FilterMode currentMode;
    private int currentDepth;

    public CuXmlFilterer(final XMLStreamReader xmlReader, final XMLStreamWriter xmlWriter,
            final Enum<?> enumConstantWithAnnotation) {
        this(xmlReader, xmlWriter,
                FixtureUtils.getAnnotationBasedFixture(enumConstantWithAnnotation, XmlDocumentFilter.class));
    }

    public CuXmlFilterer(final XMLStreamReader xmlReader, final XMLStreamWriter xmlWriter,
            final Class<?> classWithAnnotation) {
        this(xmlReader, xmlWriter, classWithAnnotation.getAnnotation(XmlDocumentFilter.class));
    }

    public CuXmlFilterer(final XMLStreamReader xmlReader, final XMLStreamWriter xmlWriter,
            final XmlDocumentFilter xmlFilter) {
        Validate.notNull(xmlFilter, "XmlDocumentFilter annotation was not present on the class or field");
        Validate.notBlank(xmlFilter.rootElementName(),
                "XmlDocumentFilter annotation must specify the expected name of the root element");
        this.xmlReader = xmlReader;
        this.xmlWriter = xmlWriter;
        this.expectedRootElementName = xmlFilter.rootElementName();
        this.elementFilters = createElementFilterMappings(xmlFilter);
    }

    private static Map<String, ElementFilter> createElementFilterMappings(final XmlDocumentFilter xmlFilter) {
        return Arrays.stream(xmlFilter.directChildElementsToKeep())
                .map(ElementFilter::new)
                .collect(Collectors.toUnmodifiableMap(filter -> filter.name, Function.identity()));
    }

    public void filterXml() throws XMLStreamException {
        whitespaceBetweenChildElements = new StringBuilder();
        currentMode = FilterMode.OUTSIDE_ROOT_ELEMENT;
        currentDepth = 0;
        modesStackTop = new FilterModeStackElement(-1, currentMode, null);

        while (xmlReader.hasNext()) {
            final int eventType = xmlReader.next();
            EVENT_HANDLERS.getOrDefault(eventType, NO_OP_HANDLER).accept(this);
        }
    }

    private void handleElementStart() throws XMLStreamException {
        currentDepth++;
        ELEMENT_START_HANDLERS.get(currentMode).accept(this);
    }

    private void handleRootElementStart() throws XMLStreamException {
        if (!StringUtils.equals(xmlReader.getLocalName(), expectedRootElementName)) {
            throw new XMLStreamException("Wrong name of root XML element: expected '" + expectedRootElementName
                    + "' but was actually '" + xmlReader.getLocalName() + "'");
        }
        updateModeAndPushToStack(FilterMode.PROCESSING_ROOT_ELEMENT);
        writeStartElementWithAttributes();
    }

    private void handleChildElementStart() throws XMLStreamException {
        if (shouldIncludeCurrentElement()) {
            updateModeAndPushToStack(FilterMode.PROCESSING_CHILD_ELEMENTS);
            writeAndResetTrackedWhitespaceBetweenChildElements();
            writeStartElementWithAttributes();
        } else {
            updateModeAndPushToStack(FilterMode.DISCARDING_CONTENT);
            resetTrackedWhitespaceBetweenChildElements();
        }
    }

    private boolean shouldIncludeCurrentElement() throws XMLStreamException {
        final String elementName = xmlReader.getLocalName();
        final ElementFilter filter = elementFilters.get(elementName);
        if (filter == null) {
            return false;
        }

        for (final AttributeMatcher attributeMatcher : filter.attributeMatchers) {
            final String attributeName = attributeMatcher.name;
            final String attributeValue = StringUtils.defaultString(
                    xmlReader.getAttributeValue(attributeMatcher.namespaceURI.orElse(null), attributeName));
            if (attributeMatcher.hasRegex) {
                if (!attributeMatcher.regex.matcher(attributeValue).matches()) {
                    return false;
                }
            } else if (!attributeMatcher.allowedValues.contains(attributeValue)) {
                return false;
            }
        }

        return true;
    }

    private void handleNestedChildElementStart() throws XMLStreamException {
        writeStartElementWithAttributes();
    }

    private void writeStartElementWithAttributes() throws XMLStreamException {
        writeStartElement();
        writeElementAttributes();
    }

    private void writeStartElement() throws XMLStreamException {
        final String localName = xmlReader.getLocalName();
        final String namespaceURI = xmlReader.getNamespaceURI();
        final String prefix = xmlReader.getPrefix();
        if (StringUtils.isNotBlank(namespaceURI)) {
            if (StringUtils.isNotBlank(prefix)) {
                xmlWriter.writeStartElement(prefix, localName, namespaceURI);
            } else {
                xmlWriter.writeStartElement(namespaceURI, localName);
            }
        } else {
            xmlWriter.writeStartElement(localName);
        }
    }

    private void writeElementAttributes() throws XMLStreamException {
        final int attributeCount = xmlReader.getAttributeCount();

        for (int i = 0; i < attributeCount; i++) {
            final String localName = xmlReader.getAttributeLocalName(i);
            final String namespaceURI = xmlReader.getAttributeNamespace(i);
            final String prefix = xmlReader.getAttributePrefix(i);
            final String value = xmlReader.getAttributeValue(i);
            if (StringUtils.isNotBlank(namespaceURI)) {
                if (StringUtils.isNotBlank(prefix)) {
                    xmlWriter.writeAttribute(prefix, namespaceURI, localName, value);
                } else {
                    xmlWriter.writeAttribute(namespaceURI, localName, value);
                }
            } else {
                xmlWriter.writeAttribute(localName, value);
            }
        }
    }

    private void handleElementEnd() throws XMLStreamException {
        if (currentMode != FilterMode.DISCARDING_CONTENT) {
            if (currentMode == FilterMode.PROCESSING_ROOT_ELEMENT) {
                writeAndResetTrackedWhitespaceBetweenChildElements();
            }
            xmlWriter.writeEndElement();
        }

        currentDepth--;
        if (currentDepth == modesStackTop.depth) {
            popModeFromStack();
        }
    }

    private void handleProcessingInstruction() throws XMLStreamException {
        if (currentMode == FilterMode.DISCARDING_CONTENT) {
            return;
        }
    }

    private void handleCharacters() throws XMLStreamException {
        if (currentMode == FilterMode.DISCARDING_CONTENT) {
            return;
        }

        final String text = xmlReader.getText();
        if (currentMode == FilterMode.PROCESSING_ROOT_ELEMENT) {
            if (StringUtils.isBlank(text)) {
                whitespaceBetweenChildElements.append(text);
            } else {
                writeAndResetTrackedWhitespaceBetweenChildElements();
                xmlWriter.writeCharacters(text);
                
            }
        } else {
            xmlWriter.writeCharacters(text);
        }
    }

    private void writeAndResetTrackedWhitespaceBetweenChildElements() throws XMLStreamException {
        xmlWriter.writeCharacters(whitespaceBetweenChildElements.toString());
        resetTrackedWhitespaceBetweenChildElements();
    }

    private void resetTrackedWhitespaceBetweenChildElements() {
        whitespaceBetweenChildElements.delete(0, whitespaceBetweenChildElements.length());
    }

    private void handleDocumentStart() throws XMLStreamException {
        xmlWriter.writeStartDocument();
    }

    private void handleDocumentEnd() throws XMLStreamException {
        xmlWriter.writeEndDocument();
    }

    private void handleEntityReference() throws XMLStreamException {
        if (currentMode == FilterMode.DISCARDING_CONTENT) {
            return;
        }
        xmlWriter.writeEntityRef(xmlReader.getLocalName());
    }

    private void handleNamespace() throws XMLStreamException {
        if (currentMode == FilterMode.DISCARDING_CONTENT) {
            return;
        }
        xmlWriter.writeNamespace(xmlReader.getPrefix(), xmlReader.getNamespaceURI());
    }

    private void updateModeAndPushToStack(final FilterMode newMode) {
        currentMode = newMode;
        modesStackTop = new FilterModeStackElement(currentDepth - 1, currentMode, modesStackTop);
    }

    private void popModeFromStack() {
        modesStackTop = modesStackTop.parent;
        currentMode = modesStackTop.mode;
    }



    private static final class FilterModeStackElement {
        private final int depth;
        private final FilterMode mode;
        private final FilterModeStackElement parent;

        private FilterModeStackElement(final int depth, final FilterMode mode, final FilterModeStackElement parent) {
            this.depth = depth;
            this.mode = mode;
            this.parent = parent;
        }
    }



    private static final class ElementFilter {
        private final String name;
        private final List<AttributeMatcher> attributeMatchers;

        private ElementFilter(final XmlElementFilter elementFilter) {
            Validate.notBlank(elementFilter.name(), "XmlElementFilter must specify an element name");
            Validate.notEmpty(elementFilter.matchConditions(),
                    "XmlElementFilter must specify at least one set of attribute-matching criteria");

            this.name = elementFilter.name();
            this.attributeMatchers = Arrays.stream(elementFilter.matchConditions())
                    .map(AttributeMatcher::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }



    private static final class AttributeMatcher {
        private final String name;
        private final Optional<String> namespaceURI;
        private final Set<String> allowedValues;
        private final Pattern regex;
        private final boolean hasRegex;

        private AttributeMatcher(final XmlAttributeMatcher attributeMatcher) {
            Validate.notBlank(attributeMatcher.name(), "XmlAttributeMatcher must specify an attribute name");
            Validate.isTrue(
                    ArrayUtils.isEmpty(attributeMatcher.values()) || StringUtils.isBlank(attributeMatcher.regex()),
                    "XmlAttributeMatcher must specify either a values list or a regex, but not both");

            this.name = attributeMatcher.name();
            this.namespaceURI = Optional.ofNullable(attributeMatcher.namespaceURI())
                    .filter(StringUtils::isNotBlank);

            if (StringUtils.isNotBlank(attributeMatcher.regex())) {
                this.allowedValues = null;
                this.regex = Pattern.compile(attributeMatcher.regex());
                this.hasRegex = true;
            } else {
                this.allowedValues = Set.of(attributeMatcher.values());
                this.regex = null;
                this.hasRegex = false;
            }
        }
    }

}
