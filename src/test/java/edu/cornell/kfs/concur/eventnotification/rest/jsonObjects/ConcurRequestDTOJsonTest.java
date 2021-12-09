package edu.cornell.kfs.concur.eventnotification.rest.jsonObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;

class ConcurRequestDTOJsonTest {
    
    private static final Logger LOG = LogManager.getLogger();
    private ObjectMapper objectMapper;
    
    private static final String TRAVEL_REPORT_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/travelV4/v4_travel_report.json";
    private static final String TRAVEL_LIST_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/travelV4/v4_travel_request_listing.json";
    private static final String REPORT_ID = "reportId";
    private static final String PURPOSE1 = "purpose1";
    private static final String DOE = "Doe";
    private static final String EZRA     = "Ezra";
    private static final Double REPORT_EXPECTED_TOTAL_REMAINING_AMOUNT = 1290.0;
    private static final Double REPORT_EXPECTED_TOTAL_APPROVED_AMOUNT = 879.07;
    private static final Integer LISTING_EXPECTED_TOTAL_COUNT = 5;

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    @AfterEach
    public void tearDown() throws Exception {
        objectMapper = null;
    }

    @Test
    public void testConcurRequestV4ReportDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(TRAVEL_REPORT_JSON_EXAMPLE_FILE);
        ConcurRequestV4ReportDTO dto = objectMapper.readValue(jsonFile, ConcurRequestV4ReportDTO.class);
        LOG.info("testConcurRequestV4ReportDTO, dto: " + dto.toString());
        assertEquals(REPORT_ID, dto.getId());
        assertFalse(dto.getApproved());
        assertEquals(DOE, dto.getApprover().getLastName());
        assertEquals(REPORT_EXPECTED_TOTAL_REMAINING_AMOUNT, dto.getTotalRemainingAmount().getValue());
        assertEquals(REPORT_EXPECTED_TOTAL_APPROVED_AMOUNT, dto.getTotalApprovedAmount().getValue());
    }
    
    @Test
    public void testConcurRequestV4ListingDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(TRAVEL_LIST_JSON_EXAMPLE_FILE);
        ConcurRequestV4ListingDTO dto = objectMapper.readValue(jsonFile, ConcurRequestV4ListingDTO.class);
        LOG.info("testConcurRequestV4ListingDTO, dto: " + dto.toString());
        assertEquals(LISTING_EXPECTED_TOTAL_COUNT, dto.getTotalCount());
        ConcurRequestV4ListItemDTO purposeOneItem = dto.getListItems()
                .stream()
                .filter(item -> item.getBusinessPurpose().equals(PURPOSE1))
                .collect(Collectors.toList()).get(0);
        assertEquals(EZRA, purposeOneItem.getApprover().getFirstName());
    }

}
