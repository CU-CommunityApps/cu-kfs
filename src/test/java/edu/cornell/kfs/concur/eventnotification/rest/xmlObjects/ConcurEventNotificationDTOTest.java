package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class ConcurEventNotificationDTOTest {
    private static final String EXAMPLE_FILE_NAME = "src/test/resources/edu/cornell/kfs/concur/rest/xmlObjects/fixture/event-notification-payload-example.xml";
    private static final String EQUAL_ASSERT_STATEMENT = "Expected value should equal the actual value.";
    
    private CUMarshalService cuMarshalService;
    private File xmlFile;
    
    @Before
    public void setUp() throws Exception {
        cuMarshalService = new CUMarshalServiceImpl();
        xmlFile = new File(EXAMPLE_FILE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        cuMarshalService = null;
        xmlFile = null;
    }
    
    @Test
    public void marshalEventNoticationListFile() throws JAXBException {
        ConcurEventNotificationDTO dto = cuMarshalService.unmarshalFile(xmlFile, ConcurEventNotificationDTO.class);
        assertEquals("EXPRPT", dto.getObjectType());
        
    }
}
