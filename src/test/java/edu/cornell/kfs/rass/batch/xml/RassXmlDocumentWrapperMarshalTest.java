package edu.cornell.kfs.rass.batch.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class RassXmlDocumentWrapperMarshalTest {
    public static final String RASS_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/rass/rass_example.xml";
    
    private CUMarshalService cuMarshalService;
    
    @Before
    public void setup() {
        cuMarshalService = new CUMarshalServiceImpl();
    }
    
    @After
    public void tearDown() {
        cuMarshalService = null;
    }
    
    @Test
    public void testUnMarshallingExample() throws JAXBException {
        File xmlFile = new File(RASS_EXAMPLE_FILE_PATH);
        RassXmlDocumentWrapper actualWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        RassXmlDocumentWrapper expectedWrapper = RassXmlDocumentWrapperFixture.RASS_EXAMPLE.toRassXmlDocumentWrapper();
        assertEquals("Wrappers should match", expectedWrapper, actualWrapper);
        assertEquals("Wrappers' hash code should match", expectedWrapper.hashCode(), actualWrapper.hashCode());
    }

}
