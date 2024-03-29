package edu.cornell.kfs.concur.eventnotification.rest.jsonObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDetailDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;

class ConcurExpenseDTOJsonTest {
    private static final Logger LOG = LogManager.getLogger();
    private ObjectMapper objectMapper;
    
    private static final String EXPENSE_REPORT_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_report.json";
    private static final String EXPENSE_LISTING_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_listing.json";
    private static final String EXPENSE_ALLOCATION_LISTING_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_allocation_listing.json";
    
    private static final String REPORT_2_NAME = "Expense Report 2";
    private static final String REPORT_1_NAME = "Expense Report 1";
    private static final String ACCOUNT_EXO4769 = "EXO4769";
    private static final String CHART_EX = "EX";
    private static final String REPORTID2 = "reportid2";
    private static final String ACCOUNT_G224700 = "G224700";
    private static final String CHART_IT = "IT";
    private static final String REPORTID1 = "reportid1";
    private static final Date REPORT_1_LAST_MODIFIED_DATE = buildDateFromString("2020-02-06T18:30:11.247");
    private static final KualiDecimal REPORT_1_AMOUNT_DUE_EMPLOYEE = new KualiDecimal(5.00000000);
    private static final Date REPORT_2_CREATE_DATE = buildDateFromString("2020-03-05T18:21:54.143");
    private static final KualiDecimal REPORT_2_TOTAL = new KualiDecimal(1800.00000000);
    
    private static final String IT = "IT";
    private static final String ITHACA_CAMPUS = "ITHACA CAMPUS";
    private static final String S565015 = "S565015";
    private static final String MCNAIR_GRANT = "McNair Grant funding - US Dept Edu 83280 FEDCGFEDL";
    private static final String MARYLAND = "MARYLAND";
    
    
    private static Date buildDateFromString(String stringDate) {
        DateTime jodaDate = new DateTime(stringDate);
        return new Date(jodaDate.getMillis());
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    @AfterEach
    public void tearDown() throws Exception {
        objectMapper = null;
    }

    @Test
    public void testConcurV3ExpenseReportDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(EXPENSE_REPORT_JSON_EXAMPLE_FILE);
        ConcurExpenseV3ListItemDTO dto = objectMapper.readValue(jsonFile, ConcurExpenseV3ListItemDTO.class);
        LOG.info("testConcurV3ExpenseReportDTO, dto: " + dto.toString());
        assertEquals(CHART_IT, dto.getChart().getCode());
        assertEquals(ACCOUNT_G224700, dto.getAccount().getCode());
        assertEquals(REPORT_1_LAST_MODIFIED_DATE, dto.getLastModifiedDate());
        assertEquals(REPORT_1_AMOUNT_DUE_EMPLOYEE, dto.getAmountDueEmployee());
    }
    
    @Test
    public void testConcurV3ExpenseListingDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(EXPENSE_LISTING_JSON_EXAMPLE_FILE); 
        ConcurExpenseV3ListingDTO dto = objectMapper.readValue(jsonFile, ConcurExpenseV3ListingDTO.class);
        LOG.info("testConcurV3ExpenseListingDTO, dto: " + dto.toString());
        ConcurExpenseV3ListItemDTO firstExpenseItem = dto.getItems().stream()
                .filter(item -> item.getName().equals(REPORT_1_NAME))
                .findFirst().orElseThrow();
        assertEquals(REPORTID1, firstExpenseItem.getId());
        assertEquals(CHART_IT, firstExpenseItem.getChart().getCode());
        assertEquals(ACCOUNT_G224700, firstExpenseItem.getAccount().getCode());
        assertEquals(REPORT_1_LAST_MODIFIED_DATE, firstExpenseItem.getLastModifiedDate());
        assertEquals(REPORT_1_AMOUNT_DUE_EMPLOYEE, firstExpenseItem.getAmountDueEmployee());
        
        ConcurExpenseV3ListItemDTO secondExpenseItem = dto.getItems().stream()
                .filter(item -> item.getName().equals(REPORT_2_NAME))
                .findFirst().orElseThrow();
        assertEquals(REPORTID2, secondExpenseItem.getId());
        assertEquals(CHART_EX, secondExpenseItem.getChart().getCode());
        assertEquals(ACCOUNT_EXO4769, secondExpenseItem.getAccount().getCode());
        assertEquals(REPORT_2_TOTAL, secondExpenseItem.getTotal());
        assertEquals(REPORT_2_CREATE_DATE, secondExpenseItem.getCreateDate());
        
    }
    
    @Test
    public void testConcurExpenseAllocationV3ListingDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(EXPENSE_ALLOCATION_LISTING_JSON_EXAMPLE_FILE);
        ConcurExpenseAllocationV3ListingDTO dto = objectMapper.readValue(jsonFile, ConcurExpenseAllocationV3ListingDTO.class);
        LOG.info("testConcurExpenseAllocationV3ListingDTO, dto: " + dto.toString());
        assertEquals(3, dto.getItems().size());
        
        ConcurExpenseAllocationV3ListItemDTO allocationItem = dto.getItems().get(2);
        assertEquals(new KualiDecimal(50), allocationItem.getPercentage());
        assertFalse(allocationItem.isHidden());
        assertEquals("6760", allocationItem.getObjectCode());
        validateAllocationDetail(allocationItem.getChart(), IT, ITHACA_CAMPUS, IT);
        validateAllocationDetail(allocationItem.getAccount(), S565015, MCNAIR_GRANT, S565015);
        validateAllocationDetail(allocationItem.getSubObject(), null, MARYLAND, MARYLAND);
    }
    
    private void validateAllocationDetail(ConcurExpenseAllocationV3ListItemDetailDTO dto, String expectedCode, String expectedValue, String expectedCodeOrValue) {
        assertEquals(expectedCode, dto.getCode());
        assertEquals(expectedValue, dto.getValue());
        assertEquals(expectedCodeOrValue, dto.getCodeOrValue());
    }

}
