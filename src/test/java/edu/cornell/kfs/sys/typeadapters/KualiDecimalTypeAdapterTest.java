package edu.cornell.kfs.sys.typeadapters;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class KualiDecimalTypeAdapterTest {

    private static final String DECIMAL_STRING = "12345.67";
    private static final String DECIMAL_JSON_STRING = "\"12345.67\"";
    private static final String NULL_STRING = "null";
    private static final String INVALID_TOKEN = "true";

    private KualiDecimalTypeAdapter adapter;
    private StringWriter writer;
    private JsonWriter jsonWriter;

    @BeforeEach
    void setUp() throws Exception {
        adapter = new KualiDecimalTypeAdapter();
        writer = new StringWriter();
        jsonWriter = new JsonWriter(writer);
    }

    @AfterEach
    void tearDown() throws Exception {
        writer.close();
        adapter = null;
        writer = null;
        jsonWriter = null;
    }

    @Test
    void testSerializeKualiDecimal() throws Exception {
        KualiDecimal decimal = new KualiDecimal(DECIMAL_STRING);
        adapter.write(jsonWriter, decimal);
        jsonWriter.flush();
        assertEquals(DECIMAL_STRING, writer.toString());
    }

    @Test
    void testDeserializeNumber() throws Exception {
        StringReader reader = new StringReader(DECIMAL_STRING);
        JsonReader jsonReader = new JsonReader(reader);
        KualiDecimal decimal = adapter.read(jsonReader);
        assertEquals(new KualiDecimal(DECIMAL_STRING), decimal);
    }

    @Test
    void testDeserializeString() throws Exception {
        StringReader reader = new StringReader(DECIMAL_JSON_STRING);
        JsonReader jsonReader = new JsonReader(reader);
        KualiDecimal decimal = adapter.read(jsonReader);
        assertEquals(new KualiDecimal(DECIMAL_STRING), decimal);
    }

    @Test
    void testSerializeNull() throws Exception {
        adapter.write(jsonWriter, null);
        jsonWriter.flush();
        assertEquals(NULL_STRING, writer.toString());
    }

    @Test
    void testDeserializeNull() throws Exception {
        StringReader reader = new StringReader(NULL_STRING);
        JsonReader jsonReader = new JsonReader(reader);
        KualiDecimal decimal = adapter.read(jsonReader);
        assertNull(decimal);
    }

    @Test
    void testDeserializeInvalidToken() throws Exception {
        StringReader reader = new StringReader(INVALID_TOKEN);
        JsonReader jsonReader = new JsonReader(reader);
        assertThrows(IOException.class, () -> adapter.read(jsonReader));
    }
}
