package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

class SupplierMessageTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/supplier/xml/";
    private static final String BASSIC_FILE_EXAMPLE = "jaggaerUploadSupplierBasic.xml";
    
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
    }

    @AfterEach
    void tearDown() throws Exception {
        supplierMessage = null;
        marshalService = null;
    }

    @Test
    void test() throws JAXBException, IOException {
        File file = new File(FILE_PATH + BASSIC_FILE_EXAMPLE);
        String expectedXml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        //LOG.info("expectedXml: " + expectedXml);
        
        Header header = new Header();
        
        Authentication auth = new Authentication();
        Identity identity = new Identity();
        identity.setvalue("JAGGAERQAOrg");
        auth.setIdentity(identity);
        SharedSecret secret = new SharedSecret();
        secret.setvalue("password");
        auth.setSharedSecret(secret);
        header.setAuthentication(auth);
        supplierMessage.setHeader(header);
        
        supplierMessage.setVersion("1.0");
        
        Supplier supplier = new Supplier();
        
        supplierMessage.getSupplierOrResponse().add(supplier);
        
        Supplier supplier2 = new Supplier();
        
        supplierMessage.getSupplierOrResponse().add(supplier2);
        
        
        String actualXMlResults = marshalService.marshalObjectToXmlString(supplierMessage);
        LOG.info("actualXMlResults: " + actualXMlResults);
    }

}
