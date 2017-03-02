package edu.cornell.kfs.concur.batch.xmlObjects;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import edu.cornell.kfs.concur.batch.fixture.PdpFeedFileBaseEntryFixture;

public class TestPdpMarshal {

    private static final String BATCH_DIRECTORY = "test/opt/work/staging/concur/standardAccountingExtract/pdpOutput/";

    private File batchDirectoryFile;

    @Before
    public void setUp() throws Exception {
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(batchDirectoryFile);
    }

    @Test
    public void test() throws JAXBException, IOException, SAXException {
        PdpFeedFileBaseEntry pdpFile = PdpFeedFileBaseEntryFixture.buildPdpFile();
        JAXBContext jaxbContext = JAXBContext.newInstance(PdpFeedFileBaseEntry.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal( pdpFile, System.out );

        File marchalledXml = new File(batchDirectoryFile.getAbsolutePath() + "generatedXML.xml");
        FileUtils.touch(marchalledXml);
        jaxbMarshaller.marshal(pdpFile, marchalledXml);

        assertTrue("The marshalled XML should be greater than 0", FileUtils.sizeOf(marchalledXml) > 0);
    }

}
