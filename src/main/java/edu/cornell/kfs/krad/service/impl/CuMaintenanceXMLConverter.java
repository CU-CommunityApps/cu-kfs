package edu.cornell.kfs.krad.service.impl;

import edu.cornell.kfs.krad.service.impl.CynergyMaintenanceXMLConverter;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.krad.util.KRADConstants;

import javax.xml.stream.XMLStreamException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CuMaintenanceXMLConverter extends CynergyMaintenanceXMLConverter {

    public static final int DATE_LENGTH_REQUIRING_MILLIS_SUFFIX = 19;

    public CuMaintenanceXMLConverter(Map<String, Map<String, String>> classPropertyRuleMaps, Map<String, String> dateRuleMap) {
        super(classPropertyRuleMaps, dateRuleMap);
    }

    protected Set<String> createSectionStartTagsSet() {
        return Stream
                .of(MaintenanceDocumentBase.OLD_MAINTAINABLE_TAG_NAME, MaintenanceDocumentBase.NEW_MAINTAINABLE_TAG_NAME, MaintenanceDocumentBase.NOTES_TAG_NAME)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    protected void handleMaintainableEndElement() throws XMLStreamException {
        if (relativeDepth != ruleMapStackTop.actualDepth || ruleMapStackTop.outputMode.writeCurrentElement) {
            if (dateLength == DATE_LENGTH_REQUIRING_EXTRA_SUFFIX) {
                xmlWriter.writeCharacters(KRADConstants.BLANK_SPACE + newDateSuffix);
            }
            if (dateLength == DATE_LENGTH_REQUIRING_MILLIS_SUFFIX) {
                xmlWriter.writeCharacters(newDateSuffix);
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

}
