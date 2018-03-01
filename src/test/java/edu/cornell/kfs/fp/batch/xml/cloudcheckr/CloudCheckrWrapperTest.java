package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture.CloudCheckrWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class CloudCheckrWrapperTest {
    private static final String BASE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/";
    
    private File clojudCheckrBillingExmaple;
    private CUMarshalService marshalService;

    @Before
    public void setUp() throws Exception {
        this.marshalService = new CUMarshalServiceImpl();
        clojudCheckrBillingExmaple = new File (BASE_TEST_FILE_PATH + "cloudcheckr-billing-result.xml");
    }
    
    @After
    public void tearDown() {
        this.marshalService = null;
    }
    
    @Test
    public void testBasicMzrshal() throws JAXBException {
        CloudCheckrWrapper actualCloudCheckerWrapper = marshalService.unmarshalFile(clojudCheckrBillingExmaple, CloudCheckrWrapper.class); 
        CloudCheckrWrapper expectedResults = CloudCheckrWrapperFixture.BILL_RESULT_1.toCloudCheckrWrapper();
        
        assertEquals(expectedResults, actualCloudCheckerWrapper);
    }
}
