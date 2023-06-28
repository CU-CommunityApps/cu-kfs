package edu.cornell.kfs.concur.batch.xmlObjects;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import jakarta.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.fixture.PdpFeedFileBaseEntryFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;

public class TestPdpMarshal {

    private static final String BATCH_DIRECTORY = "test/opt/work/staging/concur/standardAccountingExtract/pdpOutput/";
    private static final String EXAMPLE_PDP_FILE_PATH = "src/test/resources/edu/cornell/kfs/concur/batch/fixture/PdpExample.xml";

    private File batchDirectoryFile;
    private CUMarshalService cUMarshalService;

    @Before
    public void setUp() throws Exception {
        Configurator.setLevel(CUMarshalServiceImpl.class, Level.DEBUG);
        cUMarshalService = new CUMarshalServiceImpl();
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(batchDirectoryFile);
    }
    
    @Test
    public void verifyConcurPDPFileCanBeMarshalled() throws JAXBException, IOException {
        File marshalledXMLFile = cUMarshalService.marshalObjectToXML(PdpFeedFileBaseEntryFixture.MARSHAL_TEST.toPdpFeedFileBaseEntry(), 
                batchDirectoryFile.getAbsolutePath() + "generatedXML.xml");
        assertTrue("The marshalled XML should be greater than 0", FileUtils.sizeOf(marshalledXMLFile) > 0);
        
        File expectedFile = new File(EXAMPLE_PDP_FILE_PATH);
        CuXMLUnitTestUtils.compareXML(expectedFile, marshalledXMLFile);
    }

    private String convertFileToFomattedString(File file) throws IOException {
        byte[] fileByteArray = FileUtils.readFileToByteArray(file);
        String formattedString = new String(fileByteArray);
        formattedString = StringUtils.remove(formattedString, "\n");
        formattedString = formattedString.replace(" ", "");
        formattedString = formattedString.replace("\t", "");
        return formattedString;
    }

}
