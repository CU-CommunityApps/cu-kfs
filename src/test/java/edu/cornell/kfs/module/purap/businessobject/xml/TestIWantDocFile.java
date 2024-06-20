package edu.cornell.kfs.module.purap.businessobject.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

public class TestIWantDocFile {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/businessobject/xml/";
    private static final String MAXIMO_EXAMPLE_FILE_NAME = "IWantFeed-maximo-example.xml";
    private static final String TEST_FILE_NAME = "iwant-test.xml";
    
    private CUMarshalServiceImpl marshalservice;

    @BeforeEach
    void setUp() throws Exception {
        marshalservice = new CUMarshalServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        marshalservice = null;
    }

    //@Test
    public void testMaximoExample() throws JAXBException {
        File maximoFile = new File(INPUT_FILE_PATH + MAXIMO_EXAMPLE_FILE_NAME);
        IWantDocFile iWantDocFile = marshalservice.unmarshalFile(maximoFile, IWantDocFile.class);
        LOG.info("testMaximoExample, iWantDocFile: " + iWantDocFile.toString());
    }
    
    //@Test
    public void testExample() throws JAXBException {
        File testFile = new File(INPUT_FILE_PATH + TEST_FILE_NAME);
        IWantDocFile iWantDocFile = marshalservice.unmarshalFile(testFile, IWantDocFile.class);
        LOG.info("testMaximoExample, iWantDocFile: " + iWantDocFile.toString());
    }
    
    @Test
    public void testCreateFromPojo() throws JAXBException {
        IWantDocFile iWant = new IWantDocFile();
        IWantDocument iWantDoc = new IWantDocument();
        iWantDoc.setAccountDescriptionTxt("account Description");
        iWantDoc.setAdHocRouteToNetID("jdh34");
        iWantDoc.setBusinessPurpose("busiess purpose");
        iWant.getiWantDocument().add(iWantDoc);
        String xmlOutput = marshalservice.marshalObjectToXmlString(iWant);
        LOG.info("testCreateFromPojo, xmlOutput: " + xmlOutput);
    }
    
    

}
