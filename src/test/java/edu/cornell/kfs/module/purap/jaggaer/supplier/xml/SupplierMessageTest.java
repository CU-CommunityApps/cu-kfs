package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
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

class SupplierMessageTest  {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String BATCH_DIRECTORY = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/supplier/xml/outputtemp/";
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/supplier/xml/";
    private static final String BASIC_FILE_EXAMPLE = "jaggaerUploadSupplierBasic.xml";
    
    private File batchDirectoryFile;
    
    private SupplierMessage supplierMessage;
    private CUMarshalService marshalService;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        supplierMessage = new SupplierMessage();
        marshalService = new CUMarshalServiceImpl();
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @AfterEach
    void tearDown() throws Exception {
        supplierMessage = null;
        marshalService = null;
        FileUtils.deleteDirectory(batchDirectoryFile);
    }

    @Test
    void test() throws Exception {
        File basicFileExample = new File(INPUT_FILE_PATH + BASIC_FILE_EXAMPLE);
        String expectedXml = FileUtils.readFileToString(basicFileExample, StandardCharsets.UTF_8);
        
        supplierMessage.setHeader(buildHeader());
        supplierMessage.setVersion("1.0");
        
        Supplier supplier = new Supplier();
        
        supplierMessage.getSupplierOrResponse().add(supplier);
        
        String actualXMlResults = marshalService.marshalObjectToXmlString(supplierMessage);
        LOG.info("actualXMlResults: " + actualXMlResults);
        
        File actualXmlFile = marshalService.marshalObjectToXML(supplierMessage, BATCH_DIRECTORY + "test.xml");
        
        assertXMLFilesEqual(actualXmlFile, basicFileExample);
    }
    
    private Header buildHeader() {
        Header header = new Header();
        
        MessageId id  = new MessageId();
        id.setvalue("20140922T02:09:00.304");
        header.setMessageId(id);
        
        Timestamp ts = new Timestamp();
        ts.setvalue("20140922T02:09:00.304");
        header.setTimestamp(ts);
        
        Authentication auth = new Authentication();
        Identity identity = new Identity();
        identity.setvalue("JAGGAERQAOrg");
        auth.setIdentity(identity);
        SharedSecret secret = new SharedSecret();
        secret.setvalue("password");
        auth.setSharedSecret(secret);
        header.setAuthentication(auth);
        return header;
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
