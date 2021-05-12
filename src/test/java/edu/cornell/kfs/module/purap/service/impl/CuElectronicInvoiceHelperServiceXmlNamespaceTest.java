package edu.cornell.kfs.module.purap.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad;

import edu.cornell.kfs.sys.util.CuXMLStreamUtils;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.fixture.CuElectronicInvoiceHelperServiceFixture;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuElectronicInvoiceHelperServiceXmlNamespaceTest {
    private static final String TEST_EINVOICE_DIRECTORY_PATH = "test/purap/electronicInvoice/";
    private static final String ACCEPT_FILENAME = "accept.xml";
    private static final String REJECT_FILENAME = "reject.xml";
    private static final String TEST_DUNS_NUMBER = "12345678";
    private static final String TEST_PO_NUMBER = "99999999";
    private static final String BAD_XMLNS_ATTRIBUTE = "xmlns:cornellNS";
    private static final String BAD_XMLNS_XSI_ATTRIBUTE = "xmlns:otherNS";
    private static final int EXPECTED_ROOT_ELEMENT_ATTRIBUTE_COUNT = 6;

    private TestCuElectronicInvoiceHelperServiceImpl cuElectronicInvoiceHelperService;

    @Before
    public void setUp() throws Exception {
        this.cuElectronicInvoiceHelperService = new TestCuElectronicInvoiceHelperServiceImpl();
        
        File eInvoiceTestDirectory = new File(TEST_EINVOICE_DIRECTORY_PATH);
        FileUtils.forceMkdir(eInvoiceTestDirectory);
    }

    @After
    public void tearDown() throws Exception {
        File eInvoiceTestDirectory = new File(TEST_EINVOICE_DIRECTORY_PATH);
        if (eInvoiceTestDirectory.exists() && eInvoiceTestDirectory.isDirectory()) {
            FileUtils.forceDelete(eInvoiceTestDirectory.getAbsoluteFile());
        }
    }

    @Test
    public void testAttributeCleanupWhenXmlnsAttributesAreAbsent() throws Exception {
        generateAndSaveValidFile(StringUtils.EMPTY, StringUtils.EMPTY);
        assertHelperServiceRemovesUnsupportedXmlnsAttributes();
    }

    @Test
    public void testAttributeCleanupWhenValidXmlnsAttributesArePresent() throws Exception {
        generateAndSaveValidFile(CuPurapTestConstants.XMLNS_ATTRIBUTE, CuPurapTestConstants.XMLNS_XSI_ATTRIBUTE);
        assertHelperServiceRemovesUnsupportedXmlnsAttributes();
    }

    @Test
    public void testAttributeCleanupWhenInvalidXmlnsAttributesArePresent() throws Exception {
        generateAndSaveValidFile(BAD_XMLNS_ATTRIBUTE, BAD_XMLNS_XSI_ATTRIBUTE);
        assertHelperServiceRemovesUnsupportedXmlnsAttributes();
    }

    @Test
    public void testRejectionOfCorruptedFileWithoutXmlnsAttributes() throws Exception {
        generateAndSaveCorruptedFile(StringUtils.EMPTY, StringUtils.EMPTY);
        assertHelperServiceRejectsInvalidInvoiceFile();
    }

    @Test
    public void testRejectionOfCorruptedFileWithValidXmlnsAttributes() throws Exception {
        generateAndSaveCorruptedFile(CuPurapTestConstants.XMLNS_ATTRIBUTE, CuPurapTestConstants.XMLNS_XSI_ATTRIBUTE);
        assertHelperServiceRejectsInvalidInvoiceFile();
    }

    @Test
    public void testRejectionOfCorruptedFileWithInvalidXmlnsAttributes() throws Exception {
        generateAndSaveCorruptedFile(BAD_XMLNS_ATTRIBUTE, BAD_XMLNS_XSI_ATTRIBUTE);
        assertHelperServiceRejectsInvalidInvoiceFile();
    }

    private void generateAndSaveValidFile(String xmlnsAttributeName, String xmlnsXsiAttributeName) throws Exception {
        String xml = CuElectronicInvoiceHelperServiceFixture.getCXMLForPaymentDocCreation(
                TEST_DUNS_NUMBER, TEST_PO_NUMBER, xmlnsAttributeName, xmlnsXsiAttributeName);
        saveXmlFile(ACCEPT_FILENAME, xml);
    }

    private void generateAndSaveCorruptedFile(String xmlnsAttributeName, String xmlnsXsiAttributeName) throws Exception {
        String xml = CuElectronicInvoiceHelperServiceFixture.getCorruptedCXML(
                TEST_DUNS_NUMBER, TEST_PO_NUMBER, xmlnsAttributeName, xmlnsXsiAttributeName);
        saveXmlFile(REJECT_FILENAME, xml);
    }

    private void saveXmlFile(String fileName, String xml) throws Exception {
        File file = new File(TEST_EINVOICE_DIRECTORY_PATH + fileName);
        FileWriter fileWriter = null;
        
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(xml);
            fileWriter.flush();
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    private void assertHelperServiceRemovesUnsupportedXmlnsAttributes() throws Exception {
        byte[] xmlContent = updateXmlNamespaces(ACCEPT_FILENAME);
        assertRootElementOnlyHasSupportedAttributes(xmlContent);
    }

    private void assertHelperServiceRejectsInvalidInvoiceFile() throws Exception {
        byte[] xmlContent = updateXmlNamespaces(REJECT_FILENAME);
        assertNull("The eInvoice XML file should have been rejected due to bad formatting", xmlContent);
    }

    private byte[] updateXmlNamespaces(String fileName) throws Exception {
        File invoiceFile = new File(TEST_EINVOICE_DIRECTORY_PATH + fileName);
        ElectronicInvoiceLoad eInvoiceLoad = new ElectronicInvoiceLoad();
        return cuElectronicInvoiceHelperService.addNamespaceDefinition(eInvoiceLoad, invoiceFile);
    }

    private void assertRootElementOnlyHasSupportedAttributes(byte[] xmlContent) throws Exception {
        Map<String, String> attributes = getAttributesFromRootElement(xmlContent);
        
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            switch (attribute.getKey()) {
                case CuPurapTestConstants.PAYLOAD_ID_ATTRIBUTE :
                case CuPurapTestConstants.TIMESTAMP_ATTRIBUTE :
                case CuPurapTestConstants.VERSION_ATTRIBUTE :
                case CuPurapTestConstants.XML_LANG_ATTRIBUTE :
                    assertTrue("Non-xmlns attribute " + attribute.getKey() + "should have had a non-blank value",
                            StringUtils.isNotBlank(attribute.getValue()));
                    break;
                
                case CuPurapTestConstants.XMLNS_ATTRIBUTE :
                    assertEquals("Wrong eInvoice namespace URL", CuPurapTestConstants.EINVOICE_NAMESPACE_URL, attribute.getValue());
                    break;
                
                case CuPurapTestConstants.XMLNS_XSI_ATTRIBUTE :
                    assertEquals("Wrong XSI namespace URL", CuPurapTestConstants.XSI_NAMESPACE_URL, attribute.getValue());
                    break;
                
                default :
                    fail("Root CXML element had an unsupported attribute: " + attribute.getKey());
            }
        }
        
        assertEquals("Wrong attribute count", EXPECTED_ROOT_ELEMENT_ATTRIBUTE_COUNT, attributes.size());
    }

    private Map<String, String> getAttributesFromRootElement(byte[] xmlContent) throws Exception {
        InputStream xmlStream = null;
        Reader reader = null;
        XMLStreamReader streamReader = null;
        
        try {
            xmlStream = new ByteArrayInputStream(xmlContent);
            reader = new InputStreamReader(xmlStream, StandardCharsets.UTF_8);
            
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            streamReader = inputFactory.createXMLStreamReader(reader);
            return getAttributesFromRootElement(streamReader);
        } finally {
            CuXMLStreamUtils.closeQuietly(streamReader);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(xmlStream);
        }
    }

    private Map<String, String> getAttributesFromRootElement(XMLStreamReader streamReader) throws Exception {
        streamReader.nextTag();
        
        Map<String, String> attributes = new HashMap<>();
        addAllAttributesToMap(streamReader, attributes::put);
        addAllNamespacesToMap(streamReader, attributes::put);
        
        return attributes;
    }

    private void addAllAttributesToMap(XMLStreamReader streamReader, BiConsumer<String, String> entryInserter) throws Exception {
        int attributeCount = streamReader.getAttributeCount();
        
        for (int i = 0; i < attributeCount; i++) {
            String prefix = streamReader.getAttributePrefix(i);
            String attributeName = streamReader.getAttributeLocalName(i);
            String attributeFullName = StringUtils.isNotBlank(prefix)
                    ? prefix + CUKFSConstants.COLON + attributeName
                    : attributeName;
            entryInserter.accept(attributeFullName, streamReader.getAttributeValue(i));
        }
    }

    private void addAllNamespacesToMap(XMLStreamReader streamReader, BiConsumer<String, String> entryInserter) throws Exception {
        int namespaceCount = streamReader.getNamespaceCount();
        
        for (int i = 0; i < namespaceCount; i++) {
            String prefix = streamReader.getNamespacePrefix(i);
            String xmlnsAttributeName = StringUtils.isNotBlank(prefix)
                    ? CuPurapTestConstants.XMLNS_ATTRIBUTE + CUKFSConstants.COLON + prefix
                    : CuPurapTestConstants.XMLNS_ATTRIBUTE;
            entryInserter.accept(xmlnsAttributeName, streamReader.getNamespaceURI(i));
        }
    }

    private static class TestCuElectronicInvoiceHelperServiceImpl extends CuElectronicInvoiceHelperServiceImpl {
        @Override
        protected void rejectElectronicInvoiceFile(
                ElectronicInvoiceLoad eInvoiceLoad, String fileDunsNumber, File invoiceFile,
                String extraDescription, String rejectReasonTypeCode) {
            // Do nothing.
        }
    }

}
