package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.sys.KFSConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class CuJsonUtilsTest {

    @Test
    void testBuildEmptyJsonObjectString() throws Exception {
        String expectedJson = "{}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries();
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testBuildJsonObjectWithSingleProperty() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\"}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("netid", "abc123"));
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testBuildJsonObjectWithTwoProperties() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"firstname\":\"John\"}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("netid", "abc123"),
                Map.entry("firstname", "John"));
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testBuildJsonObjectWithMultipleProperties() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"firstname\":\"John\","
                + "\"lastname\":\"Doe\","
                + "\"age\":\"50\"}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("netid", "abc123"),
                Map.entry("firstname", "John"),
                Map.entry("lastname", "Doe"),
                Map.entry("age", "50"));
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testLastPropertyWinsWhenBuildingJsonWithDuplicatePropertyEntries() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"firstname\":\"Jane\"}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("netid", "abc123"),
                Map.entry("firstname", "John"),
                Map.entry("firstname", "Jane"));
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testHandleSpecialCharEscapingWhenBuildingJsonFromEntries() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"description\":\"John \\\" just \\r\\nwants to \\b test \\t out \\f special \\\\chars!\"}";
        String actualJson = CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("netid", "abc123"),
                Map.entry("description", "John \" just \r\nwants to \b test \t out \f special \\chars!"));
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testCannotBuildJsonFromNullArguments() throws Exception {
        Map.Entry<String, String>[] entries = null;
        Map.Entry<String, String> singleEntry = null;
        assertThrows(NullPointerException.class, () -> CuJsonUtils.buildJsonStringFromEntries(entries),
                "The JSON-building operation should have failed, due to a null varargs input");
        assertThrows(NullPointerException.class, () -> CuJsonUtils.buildJsonStringFromEntries(singleEntry),
                "The JSON-building operation should have failed, due to a null Map entry");
    }

    @Test
    void testAddPropertyToEmptyJson() throws Exception {
        String expectedJson = "{\"netid\":\"zzz999\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                "{}", "netid", "zzz999");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testAddPropertyToJsonWithSingleProperty() throws Exception {
        String expectedJson = "{\"netid\":\"zzz999\","
                + "\"firstname\":\"Jane\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                "{\"netid\":\"zzz999\"}", "firstname", "Jane");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testReplacePropertyInJsonWithSingleProperty() throws Exception {
        String expectedJson = "{\"netid\":\"yxw654\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                "{\"netid\":\"zzz999\"}", "netid", "yxw654");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testAddPropertyToJsonWithMultipleProperties() throws Exception {
        String expectedJson = "{\"netid\":\"zzz999\","
                + "\"firstname\":\"Jane\","
                + "\"lastname\":\"Doe\"}";
        String jsonToRebuild = "{\"netid\":\"zzz999\","
                + "\"firstname\":\"Jane\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                jsonToRebuild, "lastname", "Doe");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testReplacePropertyInJsonWithMultipleProperties() throws Exception {
        String expectedJson = "{\"netid\":\"zzz999\","
                + "\"firstname\":\"John\","
                + "\"lastname\":\"Doe\"}";
        String jsonToRebuild = "{\"netid\":\"zzz999\","
                + "\"firstname\":\"Jane\","
                + "\"lastname\":\"Doe\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                jsonToRebuild, "firstname", "John");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testHandleSpecialCharsWhenAddingPropertyToJson() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"description\":\"John \\\" just \\r\\nwants to \\b test \\t out \\f special \\\\chars!\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                "{\"netid\":\"abc123\"}",
                "description", "John \" just \r\nwants to \b test \t out \f special \\chars!");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @Test
    void testHandleSpecialCharsWhenReplacingPropertyInJson() throws Exception {
        String expectedJson = "{\"netid\":\"abc123\","
                + "\"description\":\"John \\\" just \\r\\nwants to \\b test \\t out \\f special \\\\chars!\"}";
        String jsonToRebuild = "{\"netid\":\"abc123\","
                + "\"description\":\"Just a test\"}";
        String actualJson = CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                jsonToRebuild,
                "description", "John \" just \r\nwants to \b test \t out \f special \\chars!");
        assertJsonStringsMatch(expectedJson, actualJson);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        KFSConstants.BLANK_SPACE,
        "\"3\"",
        "{\"netid\":\"zzz999\""
    })
    void testCannotOverrideJsonPropertyUsingInvalidJsonString(String invalidJson) throws Exception {
        Class<? extends Throwable> expectedExceptionType = StringUtils.contains(invalidJson, '{')
                ? RuntimeException.class : IllegalArgumentException.class;
        assertThrows(expectedExceptionType,
                () -> CuJsonUtils.rebuildJsonStringWithPropertyOverride(invalidJson, "firstname", "John"),
                "The JSON-property-replacing operation should have failed, due to an invalid JSON string");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { KFSConstants.BLANK_SPACE })
    void testCannotOverrideJsonPropertyUsingInvalidPropertyName(String invalidPropertyName) throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> CuJsonUtils.rebuildJsonStringWithPropertyOverride(
                        "{\"netid\":\"abc123\"}", invalidPropertyName, "John"),
                "The JSON-property-replacing operation should have failed, due to an invalid property name");
    }

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
        return CuJsonUtils.buildJsonStringFromEntries(
                Map.entry("dateValue", dateFormat.format(date)));
    }

}
