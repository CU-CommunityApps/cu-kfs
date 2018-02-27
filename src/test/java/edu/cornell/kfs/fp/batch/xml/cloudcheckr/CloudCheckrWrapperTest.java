package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

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
    
    @Test
    public void testBasicMzrshal() throws JAXBException {
        CloudCheckrWrapper pojo = marshalService.unmarshalFile(clojudCheckrBillingExmaple, CloudCheckrWrapper.class); 
        
        System.out.println(pojo);
    }
}
