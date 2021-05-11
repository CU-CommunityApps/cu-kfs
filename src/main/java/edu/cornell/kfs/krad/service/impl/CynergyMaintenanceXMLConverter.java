package edu.cornell.kfs.krad.service.impl;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.sys.util.CuXMLStreamUtils;

// TODO: KFSPTS-21135 (or follow-up ticket): Rename this class to avoid Cynergy references, or just merge it into the CuMaintenanceXMLConverter class.
/**
 * Helper class for converting maintenance XML via StAX, and which provides
 * extra conversion features beyond those in the Rice-delivered converter.
 * 
 * This class is designed for converting the whole maintenance document at once.
 * The createSectionStartTagsSet() method defines which tags contain the sections
 * requiring conversion, and it defaults to the old and new maintainable sections.
 * Subclasses can add more element names to the returned Set to convert additional
 * areas of the XML (such as BO notes).
 * 
 * Although Rice's upgrade rules XML has a "maint_doc_classname_changes" section,
 * that part of the configuration is not used by this converter. Instead, a rule
 * with a classname of "*" within the "maint_doc_changed_class_properties" section
 * is used for referencing default conversions, including classname conversions.
 * (Rice's default conversion service relies on similar "*"-mapped rules.)
 * Property changes for a specific class or element take precedence over those
 * within the default "*"-mapped changes.
 * 
 * In addition, the converter can use an element's "class" attribute to determine
 * the specific property mappings for conversion handling. Mappings that correspond
 * to the "class" attribute value will be used instead of those that correspond
 * to the element's converted name, if they exist.
 * 
 * If a mapping key ends with "(ATTR)", then the portion before that suffix will
 * be used to match the name of an *attribute* instead of the name of an element.
 * Any matching attributes will have their attribute *values* replaced accordingly.
 * If the replacement value is blank, then the attribute will be removed altogether.
 * 
 * The custom date field processing from the Rice krad-development-tools version
 * of the conversion feature has been added as well, and behaves about the same
 * as it does in base Rice.
 * 
 * Similar to element name replacement in default Rice, elements will generally
 * be renamed to the replacement value if the replacement is non-blank, and elements
 * and their children will be removed entirely if the replacement value is blank.
 * However, if the replacement equals one of the following special values (including
 * the extra parentheses), then some custom processing will be performed instead of
 * a direct name replacement:
 * 
 * (MOVE_NODES_TO_PARENT) -- The element itself will be removed, but all of its
 * child elements and text content will be preserved and converted, effectively
 * making them children of the parent of the removed element.
 * 
 * (CONVERT_TO_MAP_ENTRIES) -- Every pair of similarly-configured elements
 * in that spot will be wrapped within an "entry" element.
 * 
 * (CONVERT_LEGACY_NOTES) -- The element itself will be removed, but all of its
 * child elements and text content will be converted and will be moved into a new
 * "notes" element and OJB-list-proxy wrapper sub-element, and that content chunk
 * will be placed just prior to the end of the whole XML document. (Although Rice
 * typically places the "notes" element at the beginning of the maintainable XML
 * when saving maintenance documents, Rice is lax about that element's location
 * when it reads the notes from the XML.)
 * 
 * The above custom processing allows this class to replace some hard-coded
 * conversion behavior in Rice's XML conversion service, and also allows it
 * to implement UCD's customized legacy notes conversion in a StAX-friendly way.
 * Also, these advanced conversion rules provide greater flexibility in converting
 * more complex pieces of old maintenance XML.
 * 
 * NOTE: For the legacy notes conversion, the OJB sub-element is still needed
 * because Rice may still be placing that into the notes XML. If Rice changes
 * that code to not output the OJB element anymore, then this class will need
 * to be updated accordingly.
 */
public class CynergyMaintenanceXMLConverter {

    public static final String ROOT_ELEMENT = "maintainableDocumentContents";
    public static final String CLASS_ATTRIBUTE = "class";
    public static final String ATTRIBUTE_INDICATOR_SUFFIX = "(ATTR)";
    public static final String MOVE_NODES_TO_PARENT_INDICATOR = "(MOVE_NODES_TO_PARENT)";
    public static final String CONVERT_TO_MAP_ENTRIES_INDICATOR = "(CONVERT_TO_MAP_ENTRIES)";
    public static final String CONVERT_LEGACY_NOTES_INDICATOR = "(CONVERT_LEGACY_NOTES)";
    public static final String ENTRY_ELEMENT_NAME = "entry";
    public static final String DEFAULT_PROPERTY_RULE_KEY = "*";
    public static final String LIST_WRAPPER_ELEMENT_FOR_CONVERTED_BO_NOTES = "org.apache.ojb.broker.core.proxy.ListProxyDefaultImpl";
    public static final int DATE_LENGTH_REQUIRING_EXTRA_SUFFIX = 10;

    protected XMLStreamReader xmlReader;
    protected XMLStreamWriter xmlWriter;

    protected Map<String,Map<String,String>> classPropertyRuleMaps;
    protected Map<String,String> dateRuleMap;
    protected Map<String,String> defaultPropertyRuleMap;

    protected Set<String> sectionStartTags;

    protected int relativeDepth;
    protected XMLStreamStackElement ruleMapStackTop;
    protected MapEntryPrintState mapEntryPrintState;
    protected String newDateSuffix;
    protected int dateLength;
    protected String legacyNotesXml;

    public CynergyMaintenanceXMLConverter(Map<String,Map<String,String>> classPropertyRuleMaps,
            Map<String,String> dateRuleMap) {
        this.classPropertyRuleMaps = classPropertyRuleMaps;
        this.dateRuleMap = dateRuleMap;
        this.sectionStartTags = createSectionStartTagsSet();
        this.defaultPropertyRuleMap = classPropertyRuleMaps.get(DEFAULT_PROPERTY_RULE_KEY);
    }

    protected Set<String> createSectionStartTagsSet() {
        return Stream
                .of(MaintenanceDocumentBase.OLD_MAINTAINABLE_TAG_NAME, MaintenanceDocumentBase.NEW_MAINTAINABLE_TAG_NAME)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    public void initialize(XMLStreamReader xmlReader, XMLStreamWriter xmlWriter) {
        this.xmlReader = xmlReader;
        this.xmlWriter = xmlWriter;
    }

    public void performConversion() throws XMLStreamException {
        legacyNotesXml = null;
        
        while (xmlReader.hasNext()) {
            convertNextElement(
                    this::writeStartElementAndConvertMaintainableIfNecessary,
                    this::writeEndElementAndAddLegacyNotesIfNecessary,
                    this::writeCharacters);
        }
    }

    public void convertNextElement(XMLStreamEventHandler startElementHandler, XMLStreamEventHandler endElementHandler,
            XMLStreamEventHandler charactersHandler) throws XMLStreamException {
        switch (xmlReader.next()) {
            case XMLStreamConstants.START_ELEMENT :
                startElementHandler.handleEvent();
                break;
            case XMLStreamConstants.END_ELEMENT :
                endElementHandler.handleEvent();
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION :
                if (StringUtils.isNotBlank(xmlReader.getPIData())) {
                    xmlWriter.writeProcessingInstruction(xmlReader.getPITarget(), xmlReader.getPIData());
                } else {
                    xmlWriter.writeProcessingInstruction(xmlReader.getPITarget());
                }
                break;
            case XMLStreamConstants.CHARACTERS :
                charactersHandler.handleEvent();
                break;
            case XMLStreamConstants.START_DOCUMENT :
                if (StringUtils.isBlank(legacyNotesXml)) {
                    xmlWriter.writeStartDocument();
                }
                break;
            case XMLStreamConstants.END_DOCUMENT :
                if (StringUtils.isBlank(legacyNotesXml)) {
                    xmlWriter.writeEndDocument();
                }
                break;
            case XMLStreamConstants.ENTITY_REFERENCE :
                xmlWriter.writeEntityRef(xmlReader.getLocalName());
                break;
            case XMLStreamConstants.NAMESPACE :
                xmlWriter.writeNamespace(xmlReader.getPrefix(), xmlReader.getNamespaceURI());
                break;
        }
    }

    protected void writeStartElementAndConvertMaintainableIfNecessary() throws XMLStreamException {
        writeStartElement();
        if (sectionStartTags.contains(xmlReader.getLocalName())) {
            convertMaintainable();
        }
    }

    protected void writeStartElement() throws XMLStreamException {
        xmlWriter.writeStartElement(xmlReader.getLocalName());
        int numAttributes = xmlReader.getAttributeCount();
        for (int i = 0; i < numAttributes; i++) {
            xmlWriter.writeAttribute(
                    xmlReader.getAttributeLocalName(i), xmlReader.getAttributeValue(i));
        }
    }

    protected void writeEndElementAndAddLegacyNotesIfNecessary() throws XMLStreamException {
        if (ROOT_ELEMENT.equals(xmlReader.getLocalName()) && StringUtils.isNotBlank(legacyNotesXml)) {
            writeConvertedLegacyNotesXml();
            legacyNotesXml = null;
        }
        xmlWriter.writeEndElement();
    }

    protected void writeCharacters() throws XMLStreamException {
        xmlWriter.writeCharacters(xmlReader.getText());
    }

    protected void convertMaintainable() throws XMLStreamException {
        ruleMapStackTop = new XMLStreamStackElement();
        relativeDepth = 0;
        mapEntryPrintState = MapEntryPrintState.NONE;
        newDateSuffix = null;
        dateLength = -1;
        
        while (relativeDepth >= 0) {
            convertNextElement(
                    this::handleMaintainableStartElement,
                    this::handleMaintainableEndElement,
                    this::handleMaintainableCharacters);
        }
    }

    protected void handleMaintainableStartElement() throws XMLStreamException {
        relativeDepth++;
        
        String classAttributeValue = xmlReader.getAttributeValue(null, CLASS_ATTRIBUTE);
        String newClassAttributeValue = findReplacementOrReturnExisting(classAttributeValue);
        String elementName = xmlReader.getLocalName();
        String elementNameForOutput = elementName;
        
        OutputMode outputMode = getOutputModeForClassNames(classAttributeValue, newClassAttributeValue);
        
        if (outputMode.writeCurrentElement) {
            String potentialNewElementName = findReplacementOrReturnExisting(elementName);
            OutputMode elementOutputMode = getOutputModeForClassNames(elementName, potentialNewElementName);
            if (elementOutputMode == OutputMode.DEFAULT) {
                elementNameForOutput = potentialNewElementName;
            }
            if (outputMode == OutputMode.DEFAULT) {
                outputMode = elementOutputMode;
            }
        }
        
        if (outputMode.allowsStackUpdate) {
            pushStackElementIfNecessary(elementNameForOutput, newClassAttributeValue, outputMode);
        }
        
        if (outputMode.writeCurrentElement) {
            newDateSuffix = dateRuleMap.get(elementNameForOutput);
            if (StringUtils.isNotBlank(newDateSuffix)) {
                dateLength = 0;
            }
        }
        
        handleMaintainableStartElementOutput(elementNameForOutput, newClassAttributeValue, outputMode);
    }

    protected OutputMode getOutputModeForClassNames(String className, String newClassName) throws XMLStreamException {
        if (!StringUtils.equals(className, newClassName)) {
            switch (newClassName) {
                case MOVE_NODES_TO_PARENT_INDICATOR :
                    return OutputMode.MOVE_NODES_TO_PARENT;
                case CONVERT_TO_MAP_ENTRIES_INDICATOR :
                    return OutputMode.CONVERT_TO_MAP_ENTRIES;
                case CONVERT_LEGACY_NOTES_INDICATOR :
                    return OutputMode.CONVERT_LEGACY_NOTES;
                default :
                    return StringUtils.isBlank(newClassName) ? OutputMode.SKIP : OutputMode.DEFAULT;
            }
        }
        return OutputMode.DEFAULT;
    }

    protected String findReplacementOrReturnExisting(String className) throws XMLStreamException {
        if (relativeDepth == ruleMapStackTop.rulesDepth + 1 && ruleMapStackTop.propertyRuleMap.containsKey(className)) {
            return ruleMapStackTop.propertyRuleMap.get(className);
        } else if (defaultPropertyRuleMap.containsKey(className)) {
            return defaultPropertyRuleMap.get(className);
        } else {
            return className;
        }
    }

    protected void handleMaintainableStartElementOutput(String newElementName, String newClassAttributeValue,
            OutputMode outputMode) throws XMLStreamException {
        switch (outputMode) {
            case DEFAULT :
                writeStartElementForMaintainable(newElementName, newClassAttributeValue);
                break;
            case SKIP :
                skipCurrentElement();
                break;
            case MOVE_NODES_TO_PARENT :
                // Do nothing.
                break;
            case CONVERT_TO_MAP_ENTRIES :
                handleMapEntryPrintStateForStartElement();
                writeStartElementForMaintainable(newElementName, newClassAttributeValue);
                break;
            case CONVERT_LEGACY_NOTES :
                recordAndConvertLegacyNotesXML();
                break;
        }
    }

    protected void handleMapEntryPrintStateForStartElement() throws XMLStreamException {
        switch (mapEntryPrintState) {
            case NONE :
                xmlWriter.writeStartElement(ENTRY_ELEMENT_NAME);
                mapEntryPrintState = MapEntryPrintState.OPEN_TAG_WRITTEN;
                break;
            case OPEN_TAG_WRITTEN :
                mapEntryPrintState = MapEntryPrintState.READY_TO_CLOSE;
                break;
            default :
                throw new XMLStreamException("Encountered unexpected state when wrapping key-value pairs in 'entry' elements");
        }
    }

    protected void writeStartElementForMaintainable(String newElementName, String newClassAttributeValue) throws XMLStreamException {
        xmlWriter.writeStartElement(newElementName);
        
        int attributeCount = xmlReader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xmlReader.getAttributeLocalName(i);
            String attributeKey = attributeName + ATTRIBUTE_INDICATOR_SUFFIX;
            Map<String,String> propertyMapWithAttributeReplacement = findPropertyMapForAttributeUpdate(attributeKey);
            String attributeValue;
            
            if (propertyMapWithAttributeReplacement == null) {
                attributeValue = xmlReader.getAttributeValue(i);
            } else {
                attributeValue = propertyMapWithAttributeReplacement.get(attributeKey);
            }
            
            if (StringUtils.isNotBlank(attributeValue)) {
                if (CLASS_ATTRIBUTE.equals(attributeName)) {
                    attributeValue = newClassAttributeValue;
                }
                xmlWriter.writeAttribute(attributeName, attributeValue);
            }
        }
    }

    protected Map<String,String> findPropertyMapForAttributeUpdate(String attributeKey) throws XMLStreamException {
        if (relativeDepth == ruleMapStackTop.rulesDepth && ruleMapStackTop.propertyRuleMap.containsKey(attributeKey)) {
            return ruleMapStackTop.propertyRuleMap;
        } else if (defaultPropertyRuleMap.containsKey(attributeKey)) {
            return defaultPropertyRuleMap;
        } else {
            return null;
        }
    }

    protected void skipCurrentElement() throws XMLStreamException {
        int elementDepth = relativeDepth;
        do {
            switch (xmlReader.next()) {
                case XMLStreamConstants.START_ELEMENT :
                    relativeDepth++;
                    break;
                case XMLStreamConstants.END_ELEMENT :
                    relativeDepth--;
                    break;
            }
        } while (relativeDepth >= elementDepth);
    }

    protected void handleMaintainableEndElement() throws XMLStreamException {
        if (relativeDepth != ruleMapStackTop.actualDepth || ruleMapStackTop.outputMode.writeCurrentElement) {
            if (dateLength == DATE_LENGTH_REQUIRING_EXTRA_SUFFIX) {
                xmlWriter.writeCharacters(KRADConstants.BLANK_SPACE + newDateSuffix);
            }
            xmlWriter.writeEndElement();
        }
        
        if (mapEntryPrintState == MapEntryPrintState.READY_TO_CLOSE) {
            xmlWriter.writeEndElement();
            mapEntryPrintState = MapEntryPrintState.NONE;
        }
        
        popStackElementIfNecessary();
        dateLength = -1;
        relativeDepth--;
    }

    protected void handleMaintainableCharacters() throws XMLStreamException {
        String text = xmlReader.getText();
        if (dateLength != -1) {
            dateLength += text.length();
        }
        xmlWriter.writeCharacters(text);
    }

    protected void pushStackElementIfNecessary(String elementClassName, String attributeClassName, OutputMode outputMode) {
        Map<String,String> newPropertyRuleMap = classPropertyRuleMaps.get(attributeClassName);
        if (newPropertyRuleMap == null) {
            newPropertyRuleMap = classPropertyRuleMaps.get(elementClassName);
        }
        
        if (newPropertyRuleMap != null) {
            ruleMapStackTop = new XMLStreamStackElement(ruleMapStackTop, relativeDepth, newPropertyRuleMap, outputMode);
        } else if (outputMode == OutputMode.MOVE_NODES_TO_PARENT) {
            ruleMapStackTop = new XMLStreamStackElement(ruleMapStackTop, relativeDepth, Collections.emptyMap(), outputMode);
        }
    }

    protected void popStackElementIfNecessary() {
        if (relativeDepth == ruleMapStackTop.actualDepth) {
            ruleMapStackTop = ruleMapStackTop.parent;
        }
    }

    protected void recordAndConvertLegacyNotesXML() throws XMLStreamException {
        XMLStreamWriter existingWriter = xmlWriter;
        StringBuilderWriter notesWriter = null;
        XMLStreamWriter notesXmlWriter = null;
        
        try {
            notesWriter = new StringBuilderWriter();
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            notesXmlWriter = outFactory.createXMLStreamWriter(notesWriter);
            int boNotesDepth = relativeDepth;
            xmlWriter = notesXmlWriter;
            ruleMapStackTop = new XMLStreamStackElement(
                    ruleMapStackTop, relativeDepth, Collections.emptyMap(), OutputMode.CONVERT_LEGACY_NOTES);
            
            xmlWriter.writeStartElement(MaintenanceDocumentBase.NOTES_TAG_NAME);
            xmlWriter.writeStartElement(LIST_WRAPPER_ELEMENT_FOR_CONVERTED_BO_NOTES);
            
            do {
                convertNextElement(
                        this::handleMaintainableStartElement,
                        this::handleMaintainableEndElement,
                        this::handleMaintainableCharacters);
            } while (relativeDepth >= boNotesDepth);
            
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            xmlWriter.flush();
            
            legacyNotesXml = notesWriter.toString();
        } finally {
            xmlWriter = existingWriter;
            CuXMLStreamUtils.closeQuietly(notesXmlWriter);
            IOUtils.closeQuietly(notesWriter);
        }
    }

    protected void writeConvertedLegacyNotesXml() throws XMLStreamException {
        XMLStreamReader existingReader = xmlReader;
        StringReader notesReader = null;
        XMLStreamReader notesXmlReader = null;
        
        try {
            notesReader = new StringReader(legacyNotesXml);
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            notesXmlReader = inFactory.createXMLStreamReader(notesReader);
            xmlReader = notesXmlReader;
            
            while (xmlReader.hasNext()) {
                convertNextElement(
                        this::writeStartElement,
                        xmlWriter::writeEndElement,
                        this::writeCharacters);
            }
        } finally {
            xmlReader = existingReader;
            CuXMLStreamUtils.closeQuietly(notesXmlReader);
            IOUtils.closeQuietly(notesReader);
        }
    }

    /**
     * Functional interface whose only method is void and can potentially throw XMLStreamException,
     * to help with using lambda expressions or method references to simplify the parser's setup.
     */
    @FunctionalInterface
    protected static interface XMLStreamEventHandler {
        void handleEvent() throws XMLStreamException; 
    }

    /**
     * Helper enum representing the various XML output modes
     * that the parser should be concerned with.
     */
    protected enum OutputMode {
        DEFAULT(true, true),
        SKIP(false, false),
        MOVE_NODES_TO_PARENT(false, true),
        CONVERT_TO_MAP_ENTRIES(true, true),
        CONVERT_LEGACY_NOTES(false, false);
        
        public final boolean writeCurrentElement;
        public final boolean allowsStackUpdate;
        
        private OutputMode(boolean writeCurrentElement, boolean allowsStackUpdate) {
            this.writeCurrentElement = writeCurrentElement;
            this.allowsStackUpdate = allowsStackUpdate;
        }
    }

    /**
     * Helper enum representing the current state
     * of encapsulating two adjacent elements
     * in an "entry" wrapper element.
     */
    protected enum MapEntryPrintState {
        NONE, OPEN_TAG_WRITTEN, READY_TO_CLOSE;
    }

    /**
     * Helper class for implementing a "stack" of certain points of interest
     * in the XML parsing, usually categorized by sets of property rules.
     */
    protected static class XMLStreamStackElement {
        public final XMLStreamStackElement parent;
        public final int rulesDepth;
        public final int actualDepth;
        public final Map<String,String> propertyRuleMap;
        public final OutputMode outputMode;
        
        public XMLStreamStackElement() {
            this(null, 0, Collections.emptyMap(), OutputMode.DEFAULT);
        }
        
        public XMLStreamStackElement(XMLStreamStackElement parent, int depth, Map<String,String> propertyRuleMap, OutputMode outputMode) {
            boolean useCustomRules = (outputMode != OutputMode.MOVE_NODES_TO_PARENT || !propertyRuleMap.isEmpty());
            
            this.parent = parent;
            this.rulesDepth = useCustomRules ? depth : parent.rulesDepth;
            this.actualDepth = depth;
            this.propertyRuleMap = propertyRuleMap;
            this.outputMode = outputMode;
        }
    }

}
