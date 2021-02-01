package edu.cornell.kfs.sys.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CuJsonUtils {

    @SafeVarargs
    public static String buildJsonStringFromEntries(Map.Entry<String, String>... entries) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonRoot = objectMapper.createObjectNode();
            for (Map.Entry<String, String> entry : entries) {
                jsonRoot.put(entry.getKey(), entry.getValue());
            }
            return objectMapper.writeValueAsString(jsonRoot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String rebuildJsonStringWithPropertyOverride(
            String jsonString, String propertyName, String propertyValue) {
        requireNonBlank("jsonString", jsonString);
        requireNonBlank("propertyName", propertyName);
        String cleanedPropertyValue = StringUtils.defaultString(propertyValue);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonRoot = objectMapper.readTree(jsonString);
            if (!jsonRoot.isObject()) {
                throw new IllegalArgumentException("jsonString does not represent a JSON object");
            }
            ObjectNode jsonObject = (ObjectNode) jsonRoot;
            jsonObject.put(propertyName, cleanedPropertyValue);
            return objectMapper.writeValueAsString(jsonRoot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void requireNonBlank(String argumentName, String argumentValue) {
        if (StringUtils.isBlank(argumentValue)) {
            throw new IllegalArgumentException(argumentName + " cannot be blank");
        }
    }

}
