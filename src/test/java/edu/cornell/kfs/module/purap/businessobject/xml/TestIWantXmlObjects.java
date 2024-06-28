package edu.cornell.kfs.module.purap.businessobject.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.module.purap.businessobject.xml.fixture.IWantAttachmentFixture;
import edu.cornell.kfs.module.purap.businessobject.xml.fixture.IWantDocumentFixture;
import edu.cornell.kfs.module.purap.businessobject.xml.fixture.IWantItemFixture;
import edu.cornell.kfs.module.purap.businessobject.xml.fixture.IWantNoteFixture;
import edu.cornell.kfs.module.purap.businessobject.xml.fixture.IWantTransactionLineFixture;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

@Execution(ExecutionMode.SAME_THREAD)
public class TestIWantXmlObjects {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/businessobject/xml/";
    private static final String MAXIMO_INPUT_FILE_NAME = "maximo-iwant-example.xml";
    private static final String IWANT_INPUT_FILE_NAME = "iwant-example.xml";
    
    private static final String OUTPUT_FILE_PATH = "test/iwantdocxmltest/";
    private static final String MAXIMO_OUTPUT_FILE_NAME = "maximoOutputFile.xml";
    private static final String IWANT_OUTPUT_FILE_NAME = "iwantOutputFile.xml";
    
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
        File maximoFile = new File(INPUT_FILE_PATH + MAXIMO_INPUT_FILE_NAME);
        IWantDocumentWrapperXml iWantDocFile = marshalservice.unmarshalFile(maximoFile, IWantDocumentWrapperXml.class);
        LOG.info("testMaximoExample, iWantDocFile: " + iWantDocFile.toString());
        String outputFileName = OUTPUT_FILE_PATH + MAXIMO_OUTPUT_FILE_NAME;
        marshalservice.marshalObjectToXMLFragment(iWantDocFile, outputFileName);
        
        File outputMaximoFile = new File(outputFileName);
        
        CuXMLUnitTestUtils.compareXML(maximoFile, outputMaximoFile);
    }
    
    @Test
    public void testIwantDocFromObject() throws JAXBException, IOException {
        File iwantExampleFile = new File(INPUT_FILE_PATH + IWANT_INPUT_FILE_NAME);
        
        IWantDocumentWrapperXml docFile = buildIWantDocFile();
        LOG.info("testIwantDocFromObject. docFile : " + docFile.toString());
        
        String outputFileName = OUTPUT_FILE_PATH + IWANT_OUTPUT_FILE_NAME;
        marshalservice.marshalObjectToXMLFragment(docFile, outputFileName);
        
        File outputMaximoFile = new File(outputFileName);
        
        CuXMLUnitTestUtils.compareXML(iwantExampleFile, outputMaximoFile);
        
    }
    
    private IWantDocumentWrapperXml buildIWantDocFile() {
        IWantDocumentWrapperXml file = new IWantDocumentWrapperXml();
        file.getiWantDocuments().add(buildIWantDocument());
        return file;
    }
    
    private IWantDocumentXml buildIWantDocument() {
        IWantDocumentXml doc = IWantDocumentFixture.FULL_EXAMPLE.toIWantDocumentXml();
        doc.getAccounts().add(buildAccount());
        doc.getAttachments().add(buildAttachment());
        doc.getItems().add(buildItem());
        doc.getNotes().add(buildNote());
        return doc;
        
    }
    
    private IWantTransactionLineXml buildAccount() {
        return IWantTransactionLineFixture.TEST_LINE.toIWantTransactionLineXml();
    }
    
    private IWantAttachmentXml buildAttachment() {
        return IWantAttachmentFixture.ATTACH_TEST.toIWantAttachmentXml();
    }
    
    private IWantItemXml buildItem() {
        return IWantItemFixture.ITEM_TEST.toIWantItemXml();
    }
    
    private IWantNoteXml buildNote() {
        return IWantNoteFixture.NOTE_TEXT.toIWantNoteXml();
    }

}
