package edu.cornell.kfs.sys.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

// TODO: Remove any unneeded methods from this copied-over Cynergy class.
/**
 * Utility class for performing various streaming operations on XML,
 * as well as for quietly closing StAX readers/writers.
 */
public final class CuXMLStreamUtils {

    private CuXMLStreamUtils() { throw new UnsupportedOperationException("Do not call this method!"); }

    /**
     * Utility method for retrieving recipient names (or other text content) from the given XML.
     * 
     * <p>Accepts a special String array parameter that roughly functions like an "//elem1/elem2/.../elemN" XPath expression,
     * and whose contents resemble this expression if it excluded the "//" and was split on each "/". This parser does not
     * permit the final elements in each chain to contain other elements.</p>
     * 
     * <p>Optionally, an attribute name can be specified in a separate parameter, in in which case the search is similar
     * to an "//elem1/elem2/.../elemN/@attributeName" XPath expression.</p>
     * 
     * <p>This method will continue to read data from the stream reader until there is none left to retrieve.</p>
     * 
     * <p>TODO: This method currently does not store results from "//elem1/elem2/.../elemN" blocks that are inside of
     * a separate partial "//elem1/elem2/.../elemN" block. Should we update it to handle such cases?</p>
     * 
     * @param pathToNames The relative "//elem1/elem2/.../elemN" element path to the desired recipient names, as a String array.
     * @param attributeName The name of the attribute on the destination element that contains the recipient name; can be null.
     * @param xmlIn The stream reader to read the XML from.
     * @return The list of trimmed recipient names from the XML, or an empty list if none were found.
     * @throws XMLStreamException
     */
    public static List<String> getRecipientNamesFromXml(String[] pathToNames, String attributeName, XMLStreamReader xmlIn) throws XMLStreamException {
        List<String> recipientNames = new ArrayList<String>();
        
        // Variables for determining whether we're processing an actual recipient name element.
        int depth = 0;
        int pathDepth = 0;
        int nameDepth = 0;
        int finalNameIndex = pathToNames.length - 1;
        String nameToCheck = pathToNames[0];
        
        // Variables for reading and writing character data.
        boolean useChars = false;
        StringBuilder elementContents = new StringBuilder(50);
        char[] charBuffer = new char[256];
        int charLen = 0;
        int charStart = 0;
        
        if (StringUtils.isBlank(attributeName)) {
            attributeName = null;
        }
        
        // Read the XML.
        while (xmlIn.hasNext()) {
            switch (xmlIn.next()) {
                
                case XMLStreamConstants.START_ELEMENT :
                    if (useChars) {
                        throw new XMLStreamException("Cannot have other elements inside of recipient name elements");
                    }
                    depth++;
                    
                    // Determine whether the parser is in the position of a recipient name element.
                    if (nameToCheck.equals(xmlIn.getLocalName())) {
                        if (pathDepth == 0) {
                            pathDepth = depth;
                        }
                        if (depth == pathDepth + nameDepth) {
                            if (nameDepth == finalNameIndex) {
                                useChars = true;
                                // Retrieve the netId from the element's attribute, if specified.
                                if (attributeName != null && StringUtils.isNotBlank(xmlIn.getAttributeValue(null, attributeName))) {
                                    recipientNames.add(xmlIn.getAttributeValue(null, attributeName));
                                }
                            } else {
                                nameToCheck = pathToNames[nameDepth + 1];
                            }
                            nameDepth++;
                        }
                    }
                    
                    break;
                
                case XMLStreamConstants.END_ELEMENT :
                    
                    // Update tracking of recipient name depth if necessary.
                    if (depth < pathDepth + nameDepth) {
                        if (nameDepth == 1) {
                            pathDepth = 0;
                        }
                        nameDepth--;
                        nameToCheck = pathToNames[nameDepth];
                        // If ending the recipient name element, then store the name.
                        if (useChars) {
                            if (attributeName == null) {
                                String newRecipient = elementContents.toString();
                                if (StringUtils.isNotBlank(newRecipient)) {
                                    recipientNames.add(newRecipient.trim());
                                }
                                elementContents.delete(0, elementContents.length());
                            }
                            useChars = false;
                        }
                    }
                    
                    depth--;
                    break;
                        
                case XMLStreamConstants.PROCESSING_INSTRUCTION :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.CHARACTERS :
                    // If within a recipient name element, then read in the character data.
                    if (useChars && attributeName == null) {
                        charStart = 0;
                        do {
                            charLen = xmlIn.getTextCharacters(charStart, charBuffer, 0, 256);
                            if (charLen != 0) {
                                elementContents.append(charBuffer, 0, charLen);
                            }
                            charStart += charLen;
                        } while (charLen == 256);
                    }
                    break;
                
                case XMLStreamConstants.COMMENT :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.SPACE :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.START_DOCUMENT :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.END_DOCUMENT :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.ENTITY_REFERENCE :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.ATTRIBUTE :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.DTD :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.CDATA :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.NAMESPACE :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.NOTATION_DECLARATION :
                    // Ignore.
                    break;
                
                case XMLStreamConstants.ENTITY_DECLARATION :
                    // Ignore.
                    break;
                
                default:
                    // Ignore.
                    break;
            }
        }
        
        return recipientNames;
    }

    public static void closeQuietly(XMLStreamReader xmlReader) {
        if (xmlReader != null) {
            try {
                xmlReader.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    public static void closeQuietly(XMLStreamWriter xmlWriter) {
        if (xmlWriter != null) {
            try {
                xmlWriter.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

}
