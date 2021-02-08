package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class CuJsonUtilsTest {

    @Test
    void testSerializeDateDTOWithConvenienceObjectMapper() throws Exception {
        JsonDTOWithDate jsonDTO = buildJsonDTOContainingCurrentDate();
        Date currentDate = new Date(jsonDTO.getDateValue().getTime());
        
        String expectedJsonString = buildJsonDTOStringContainingDate(currentDate);
        assertTrue(StringUtils.isNotBlank(expectedJsonString),
                "Manually built JSON string should have been non-blank");
        
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        assertNotNull(objectMapper, "Object mapper should have been non-null");
        
        String actualJsonString = objectMapper.writeValueAsString(jsonDTO);
        assertTrue(StringUtils.isNotBlank(actualJsonString), "Serialized DTO should have been non-blank");
        assertJsonStringsMatch(expectedJsonString, actualJsonString);
    }

    @Test
    void testParseDateDTOWithConvenienceObjectMapper() throws Exception {
        Date currentDate = new Date();
        String jsonDTOString = buildJsonDTOStringContainingDate(currentDate);
        assertTrue(StringUtils.isNotBlank(jsonDTOString),
                "Manually built JSON string should have been non-blank");
        
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        assertNotNull(objectMapper, "Object mapper should have been non-null");
        
        JsonDTOWithDate jsonDTO = objectMapper.readValue(jsonDTOString, JsonDTOWithDate.class);
        assertNotNull(jsonDTO, "Parsed DTO should have been non-null");
        assertEquals(currentDate, jsonDTO.getDateValue(), "Wrong date value on parsed DTO");
    }

    private void assertJsonStringsMatch(String expectedJson, String actualJson) {
        assertEquals(expectedJson, actualJson, "The wrong JSON content was generated");
    }

    private JsonDTOWithDate buildJsonDTOContainingCurrentDate() {
        Date currentDate = new Date();
        JsonDTOWithDate jsonDTO = new JsonDTOWithDate();
        jsonDTO.setDateValue(currentDate);
        return jsonDTO;
    }

    private String buildJsonDTOStringContainingDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS, Locale.US);
        return CuJsonTestUtils.buildJsonStringFromEntries(
                Map.entry("dateValue", dateFormat.format(date)));
    }

}
