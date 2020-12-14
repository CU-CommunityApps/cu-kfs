package edu.cornell.kfs.sys.jsonadapters;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.cornell.kfs.sys.CUKFSConstants;

public class JsonDateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {
    private static final Logger LOG = LogManager.getLogger(JsonDateSerializer.class);

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        String dateString = convertDateToString(src);
        JsonPrimitive json = new JsonPrimitive(dateString);
        return json;
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return convertStringToDate(json.getAsString());
        } catch (ParseException e) {
            LOG.error("deserialize, unable to deserialize json: " + json.toString(), e);
            return null;
        }
    }
    
    public static String convertDateToString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        } else {
            SimpleDateFormat format = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss);
            return format.format(date);
        }
    }
    
    public static Date convertStringToDate(String dateString) throws ParseException {
        if (StringUtils.isBlank(dateString)) {
            return null;
        } else {
            SimpleDateFormat format = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss);
            return format.parse(dateString);
        }
    }

}
