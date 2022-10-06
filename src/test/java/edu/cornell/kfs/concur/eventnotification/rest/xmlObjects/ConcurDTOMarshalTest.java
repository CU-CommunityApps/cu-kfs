package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import jakarta.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurStandaloneEventNotificationDTO;
import edu.cornell.kfs.concur.rest.request.xmlObjects.TravelRequestDetailsDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ExpenseReportDetailsDTO;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class ConcurDTOMarshalTest {
    private static final String EXAMPLE_EVENT_NOTIFCATION_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/xmlObjects/fixture/event-notification-payload-example.xml";
    private static final String EXAMPLE_TRAVEL_REQUEST_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/xmlObjects/fixture/travel-request-detail-dto.xml";
    private static final String EXAMPLE_REPORT_DETAIL_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/xmlObjects/fixture/report-detail-dto.xml";
    
    private CUMarshalService cuMarshalService;
    
    @Before
    public void setUp() {
        cuMarshalService = new CUMarshalServiceImpl();
    }

    @After
    public void tearDown() {
        cuMarshalService = null;
    }
    
    @Test
    public void marshalEventNotificationFile() throws JAXBException {
        File xmlFile = new File(EXAMPLE_EVENT_NOTIFCATION_FILE);
        ConcurEventNotificationDTO dto = cuMarshalService.unmarshalFile(xmlFile, ConcurStandaloneEventNotificationDTO.class);
        assertEquals("EXPRPT", dto.getObjectType());
    }
    
    @Test
    public void marshalTravelRequestFile() throws JAXBException {
        File xmlFile = new File(EXAMPLE_TRAVEL_REQUEST_FILE);
        TravelRequestDetailsDTO dto = cuMarshalService.unmarshalFile(xmlFile, TravelRequestDetailsDTO.class);
        assertEquals("3DK6", dto.getRequestID());
    }

    @Test
    public void unmarshalReportDetailFile() throws JAXBException {
        File xmlFile = new File(EXAMPLE_REPORT_DETAIL_FILE);
        ExpenseReportDetailsDTO dto = cuMarshalService.unmarshalFile(xmlFile, ExpenseReportDetailsDTO.class);
        assertEquals("020406080A0C0E111315", dto.getReportId());
        assertNotNull(dto.getEntries());
        assertEquals(2, dto.getEntries().size());
    }

}
