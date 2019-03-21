package edu.cornell.kfs.rass.batch.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class RassXmlDocumentWrapperMarshalTest {
    public static final String RASS_DELTA_FILE_BASE_PATH = "src/test/resources/edu/cornell/kfs/rass/"; 
    public static final String DELTA_RASS_EXAMPLE_FILE_PATH = RASS_DELTA_FILE_BASE_PATH + "delta-2019-03-04-01.xml";
    public static final String RASS_EXAMPLE_FILE_PATH = RASS_DELTA_FILE_BASE_PATH + "rass_example.xml";
    
    private CUMarshalService cuMarshaalSdervice;
    
    @Before
    public void setup() {
        cuMarshaalSdervice = new CUMarshalServiceImpl();
    }
    
    @After
    public void tearDown() {
        cuMarshaalSdervice = null;
    }
    
    @Test
    public void testUnMarshallingDeltaExample() throws JAXBException {
        File xmlFile = new File(DELTA_RASS_EXAMPLE_FILE_PATH);
        RassXmlDocumentWrapper wrapper = cuMarshaalSdervice.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        assertTrue(wrapper!=null);
    }
    
    @Test
    public void testUnMarshallingExample() throws JAXBException {
        File xmlFile = new File(RASS_EXAMPLE_FILE_PATH);
        RassXmlDocumentWrapper actualWrapper = cuMarshaalSdervice.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        RassXmlDocumentWrapper expectedWrapper = RassXmlDocumentWrapperFixture.RASS_EXAMPLE.toRassXmlDocumentWrapper();
        assertEquals(expectedWrapper, actualWrapper);
    }

}
