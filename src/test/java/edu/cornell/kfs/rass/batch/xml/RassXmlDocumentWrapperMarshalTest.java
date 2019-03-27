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
    public static final String RASS_DELTA_FILE_BASE_PATH = "src/test/resources/edu/cornell/kfs/rass/"; 
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
    public void testUnMarshallingExample() throws JAXBException {
        File xmlFile = new File(RASS_EXAMPLE_FILE_PATH);
        RassXmlDocumentWrapper actualWrapper = cuMarshaalSdervice.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        RassXmlDocumentWrapper expectedWrapper = RassXmlDocumentWrapperFixture.RASS_EXAMPLE.toRassXmlDocumentWrapper();
        assertEquals(expectedWrapper, actualWrapper);
    }
    
    public static final DateTimeFormatter getRASSLongDateTimeFormatter() {
        DateTimeFormatter dateformatter = DateTimeFormat.forPattern(CUKFSConstants.DATE_FOMRAT_yyyy_MM_dd_T_HH_mm_ss_SSS);
        return dateformatter;
    }
    
    public static final DateTimeFormatter getRASSShortDateTimeFormatter() {
        DateTimeFormatter dateformatter = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
        return dateformatter;
    }

}
