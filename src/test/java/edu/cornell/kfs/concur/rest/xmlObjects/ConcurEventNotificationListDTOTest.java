package edu.cornell.kfs.concur.rest.xmlObjects;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class ConcurEventNotificationListDTOTest {
    private static final String EXAMPLE_FILE_NAME = "src/test/resources/edu/cornell/kfs/concur/rest/xmlObjects/fixture/failed-event-queue-response-example.xml";
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
        ConcurEventNotificationListDTO concurEventNotificationList = cuMarshalService.unmarshalFile(xmlFile, ConcurEventNotificationListDTO.class);
        List<ConcurEventNotificationDTO> concurEventNotificationDTOs = concurEventNotificationList.getConcurEventNotificationDTOs();
        assertFalse(CollectionUtils.isEmpty(concurEventNotificationDTOs));
        validateConcurEventNotificationDTO(concurEventNotificationDTOs.get(0), "1");
        validateConcurEventNotificationDTO(concurEventNotificationDTOs.get(1), "2");
        
    }
    
    @Test
    public void marshalEventNoticationListString() throws JAXBException, IOException {
        String xmlString = FileUtils.readFileToString(xmlFile);
        ConcurEventNotificationListDTO concurEventNotificationList = cuMarshalService.unmarshalString(xmlString, ConcurEventNotificationListDTO.class);
        List<ConcurEventNotificationDTO> concurEventNotificationDTOs = concurEventNotificationList.getConcurEventNotificationDTOs();
        assertFalse(CollectionUtils.isEmpty(concurEventNotificationDTOs));
        validateConcurEventNotificationDTO(concurEventNotificationDTOs.get(0), "1");
        validateConcurEventNotificationDTO(concurEventNotificationDTOs.get(1), "2");
        
    }
    
    private void validateConcurEventNotificationDTO(ConcurEventNotificationDTO dto, String indexNumber) {
        String expectedEventType = "eventType" + indexNumber;
        assertEquals(EQUAL_ASSERT_STATEMENT, expectedEventType, dto.getEventType());
        
        String notificationUri = "noticationUri" + indexNumber;
        assertEquals(EQUAL_ASSERT_STATEMENT, notificationUri, dto.getNotificationURI());
    }

}
