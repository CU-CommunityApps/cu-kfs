package edu.cornell.kfs.sys.businessobject.serialization;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * This JSON serializer is a workaround for allowing the custom Create Done Batch File lookup
 * to be compatible with newer KualiCo versions, since KualiCo's hard-coded handling in lookup-model.ts
 * only auto-applies the needed empty-array-value setup for the standard Batch File lookup.
 */
public class FormAttributeDefaultValueSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(final String value, final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
        } else if (StringUtils.equals(value, CUKFSConstants.EMPTY_JSON_ARRAY)) {
            jsonGenerator.writeStartArray();
            jsonGenerator.writeEndArray();
        } else {
            jsonGenerator.writeString(value);
        }
    }

}
