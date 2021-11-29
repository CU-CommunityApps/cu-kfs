package edu.cornell.kfs.concur.eventnotification.rest.jsonObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurV3ExpenseReportDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;

class ConcurExpenseDTOJsonTest {
    
    private ObjectMapper objectMapper;
    
    private static final String EXPENSE_REPORT_JSON_EXAMPLE_FILE = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/expenseV3/v3_expense_report.json";

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
        ConcurV3ExpenseReportDTO DTO = objectMapper.readValue(jsonFile, ConcurV3ExpenseReportDTO.class);
    }

}
