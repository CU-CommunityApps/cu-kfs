package edu.cornell.kfs.module.purap.jaggaer.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

public class SupplierSyncMessageTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String BATCH_DIRECTORY = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/outputtemp/";
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    private static final String BASIC_FILE_EXAMPLE = "SupplierSyncMessageBasic.xml";
    
    private File batchDirectoryFile;
    
    private SupplierSyncMessage supplierSyncMessage;
    private CUMarshalService marshalService;

    @BeforeEach
    void setUpBeforeClass() throws Exception {
        supplierSyncMessage = new SupplierSyncMessage();
        marshalService = new CUMarshalServiceImpl();
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @AfterEach
    void tearDownAfterClass() throws Exception {
        supplierSyncMessage = null;
        marshalService = null;
        FileUtils.deleteDirectory(batchDirectoryFile);
    }

    @Test
    void test() throws JAXBException, IOException {
        File basicFileExample = new File(INPUT_FILE_PATH + BASIC_FILE_EXAMPLE);
        String expectedXml = FileUtils.readFileToString(basicFileExample, StandardCharsets.UTF_8);
        LOG.info("expectedXML: " + expectedXml);
        
        supplierSyncMessage.setVersion("1.0");
        Header header = new Header();
        Authentication auth = new Authentication();
        auth.setIdentity("Cornell");
        auth.setSharedSecret("SuperCoolPassword");
        header.setAuthentication(auth);
        header.setMessageId("message id");
        header.setRelatedMessageId("related id");
        header.setTimestamp("20210218");
        supplierSyncMessage.setHeader(header);
        
        SupplierRequestMessage srm = new SupplierRequestMessage();
        Supplier supplier = new Supplier();
        Name name = new Name();
        name.setvalue("Acme Test Company");
        supplier.setName(name);
        srm.getSupplier().add(supplier);
        
        supplierSyncMessage.getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage().add(srm);
        
        String actualResults  = marshalService.marshalObjectToXmlString(supplierSyncMessage);
        LOG.info("actualResults: " + actualResults);
        
        File actualXmlFile = marshalService.marshalObjectToXML(supplierSyncMessage, BATCH_DIRECTORY + "test.xml");
    }
    
    private void assertXMLFilesEqual(File actualXmlFile, File expectedXmlFile) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document actualDocument = db.parse(actualXmlFile);
        actualDocument.normalizeDocument();

        Document expectedDocument = db.parse(expectedXmlFile);
        expectedDocument.normalizeDocument();

        assertTrue(actualDocument.isEqualNode(expectedDocument));
    }

}
