package edu.cornell.kfs.sys.typeadapters;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.time.LocalDate;
import java.io.StringReader;
import java.io.StringWriter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class LocalDateTypeAdapterTest {

    private static final String DATE_STRING = "\"2023-12-08\"";
    private static final LocalDate EXPECTED_DATE = LocalDate.of(2023, 12, 8);
    private static final String NULL_JSON = "null";

    private LocalDateTypeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalDateTypeAdapter(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
    }

    @AfterEach
    void tearDown() {
        adapter = null;
    }

    @Test
    void testSerializeLocalDate() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, EXPECTED_DATE);
        jsonWriter.flush();
        assertEquals(DATE_STRING, writer.toString());
    }

    @Test
    void testDeserializeLocalDate() throws Exception {
        StringReader reader = new StringReader(DATE_STRING);
        JsonReader jsonReader = new JsonReader(reader);
        LocalDate date = adapter.read(jsonReader);
        assertEquals(EXPECTED_DATE, date);
    }

    @Test
    void testDeserializeNull() throws Exception {
        StringReader reader = new StringReader(NULL_JSON);
        JsonReader jsonReader = new JsonReader(reader);
        LocalDate date = adapter.read(jsonReader);
        assertNull(date);
    }
}
