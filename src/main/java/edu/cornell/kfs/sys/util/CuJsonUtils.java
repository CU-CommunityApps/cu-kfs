package edu.cornell.kfs.sys.util;

import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class CuJsonUtils {

    public static ObjectMapper buildObjectMapperUsingDefaultTimeZone() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getDefault());
        return objectMapper;
    }

}
