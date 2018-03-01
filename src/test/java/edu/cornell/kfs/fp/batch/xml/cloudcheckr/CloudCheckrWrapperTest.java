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
    private static final String BASE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/cloudcheckr/";
    private static final String BASIC_CORNELL_TEST = "cloudcheckr_basic_cornell_test.xml";
    private static final String CLOUDCHECK_EXAMPLE = "cloudcheckerr_example.xml";
    
    private CUMarshalService marshalService;

    @Before
    public void setUp() throws Exception {
        this.marshalService = new CUMarshalServiceImpl();
    }
    
    @After
    public void tearDown() {
        this.marshalService = null;
    }
    
    @Test
    public void verifyCornellExampleWorks() throws JAXBException {
        File cornellBasicTestFile = new File(BASE_TEST_FILE_PATH + BASIC_CORNELL_TEST);
        CloudCheckrWrapper actualCloudCheckerWrapper = marshalService.unmarshalFile(cornellBasicTestFile, CloudCheckrWrapper.class); 
        CloudCheckrWrapper expectedResults = CloudCheckrWrapperFixture.BASIC_CORNELL_TEST.toCloudCheckrWrapper();
        
        assertEquals(expectedResults, actualCloudCheckerWrapper);
    }
    
    @Test
    public void verifyCloudcheckrExampleWorks() throws JAXBException {
        File cloudcheckrFile = new File(BASE_TEST_FILE_PATH + CLOUDCHECK_EXAMPLE);
        CloudCheckrWrapper actualCloudCheckerWrapper = marshalService.unmarshalFile(cloudcheckrFile, CloudCheckrWrapper.class);
        CloudCheckrWrapper expectedResults = CloudCheckrWrapperFixture.CLOUDCHECKR_TEST.toCloudCheckrWrapper();
        assertEquals(expectedResults.getCostsByAccounts(), actualCloudCheckerWrapper.getCostsByAccounts());
    }
}
