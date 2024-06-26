package edu.cornell.kfs.module.purap.businessobject.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

@Execution(ExecutionMode.SAME_THREAD)
public class TestIWantDocFile {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/businessobject/xml/";
    private static final String MAXIMO_EXAMPLE_INPUT_FILE_NAME = "IWantFeed-maximo-example.xml";
    private static final String MAXIMO_OUTPUT_FILE_NAME = "maximoOutputFile.xml";
    private static final String OUTPUT_FILE_PATH = "test/iwantdocxmltest/";
    
    private File outputFileDirectory;
    
    private CUMarshalServiceImpl marshalservice;

    @BeforeEach
    void setUp() throws Exception {
        marshalservice = new CUMarshalServiceImpl();
        outputFileDirectory = new File(OUTPUT_FILE_PATH);
        FileUtils.forceMkdir(outputFileDirectory);
    }

    @AfterEach
    void tearDown() throws Exception {
        marshalservice = null;
        FileUtils.forceDelete(outputFileDirectory.getAbsoluteFile());
    }

    @Test
    public void testMaximoExample() throws JAXBException, IOException {
        File maximoFile = new File(INPUT_FILE_PATH + MAXIMO_EXAMPLE_INPUT_FILE_NAME);
        IWantDocFile iWantDocFile = marshalservice.unmarshalFile(maximoFile, IWantDocFile.class);
        LOG.info("testMaximoExample, iWantDocFile: " + iWantDocFile.toString());
        String outputFileName = OUTPUT_FILE_PATH + MAXIMO_OUTPUT_FILE_NAME;
        marshalservice.marshalObjectToXMLFragment(iWantDocFile, outputFileName);
        
        File outputMaximoFile = new File(outputFileName);
        
        CuXMLUnitTestUtils.compareXML(maximoFile, outputMaximoFile);
    }
    
    @Test
    public void testCreateFromPojo() throws JAXBException {
        IWantDocFile file = new IWantDocFile();
        
        IWantDocument doc = new IWantDocument();
        doc.setAccountDescriptionTxt("account description");
        doc.setAdHocRouteToNetID("jdh34");
        doc.setBusinessPurpose("busniess purpose");
        
        file.getiWantDocuments().add(doc);
        
        String outputXml = marshalservice.marshalObjectToXmlString(file);
        
        LOG.info("testCreateFromPojo, outputXml: " + outputXml);
    }

}
