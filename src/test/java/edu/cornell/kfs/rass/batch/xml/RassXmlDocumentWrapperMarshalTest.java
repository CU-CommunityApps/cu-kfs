package edu.cornell.kfs.rass.batch.xml;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class RassXmlDocumentWrapperMarshalTest {
    
    public static final String RASS_DELTA_FILE_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/rass/delta-2019-03-04-01.xml";
    
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
        File xmlFile = new File(RASS_DELTA_FILE_EXAMPLE_FILE_PATH);
        RassXmlDocumentWrapper wrapper = cuMarshaalSdervice.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        assertTrue(wrapper!=null);
    }

}
