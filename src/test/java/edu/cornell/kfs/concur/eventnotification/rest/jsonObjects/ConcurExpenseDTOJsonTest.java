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

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;

class ConcurExpenseDTOJsonTest {
    private static final String REPORT_2_NAME = "Expense Report 2";
    private static final String REPORT_1_NAME = "Expense Report 1";
    private static final String ACCOUNT_EXO4769 = "EXO4769";
    private static final String CHART_EX = "EX";
    private static final String REPORTID2 = "reportid2";
    private static final String ACCOUNT_G224700 = "G224700";
    private static final String CHART_IT = "IT";
    private static final String REPORTID1 = "reportid1";
    private static final Logger LOG = LogManager.getLogger();
    private ObjectMapper objectMapper;
    
    private static final String EXPENSE_REPORT_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_report.json";
    private static final String EXPENSE_LISTING_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_listing.json";

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    @AfterEach
    void tearDown() throws Exception {
        objectMapper = null;
    }

    @Test
    void testConcurV3ExpenseReportDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(EXPENSE_REPORT_JSON_EXAMPLE_FILE); 
        ConcurExpenseV3ReportDTO dto = objectMapper.readValue(jsonFile, ConcurExpenseV3ReportDTO.class);
        LOG.info("testConcurV3ExpenseReportDTO, dto: " + dto.toString());
        assertEquals(CHART_IT, dto.getChart().getCode());
        assertEquals(ACCOUNT_G224700, dto.getAccount().getCode());
    }
    
    @Test
    void testConcurV3ExpenseListingDTO() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(EXPENSE_LISTING_JSON_EXAMPLE_FILE); 
        ConcurExpenseV3ListingDTO dto = objectMapper.readValue(jsonFile, ConcurExpenseV3ListingDTO.class);
        LOG.info("testConcurV3ExpenseListingDTO, dto: " + dto.toString());
        ConcurExpenseV3ListItemDTO firstExpenseItem = dto.getItems().stream()
                .filter(item -> item.getName().equals(REPORT_1_NAME))
                .collect(Collectors.toList()).get(0);
        assertEquals(REPORTID1, firstExpenseItem.getiD());
        assertEquals(CHART_IT, firstExpenseItem.getChart().getCode());
        assertEquals(ACCOUNT_G224700, firstExpenseItem.getAccount().getCode());
        
        ConcurExpenseV3ListItemDTO secondExpenseItem = dto.getItems().stream()
                .filter(item -> item.getName().equals(REPORT_2_NAME))
                .collect(Collectors.toList()).get(0);
        assertEquals(REPORTID2, secondExpenseItem.getiD());
        assertEquals(CHART_EX, secondExpenseItem.getChart().getCode());
        assertEquals(ACCOUNT_EXO4769, secondExpenseItem.getAccount().getCode());
        
    }

}
