package edu.cornell.kfs.sys.typeadapters;

import java.io.IOException;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class KualiDecimalTypeAdapter extends TypeAdapter<KualiDecimal> {
    @Override
    public void write(JsonWriter out, KualiDecimal value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.bigDecimalValue());
        }
    }

    @Override
    public KualiDecimal read(JsonReader in) throws IOException {
        switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            case NUMBER:
                return new KualiDecimal(in.nextString());
            case STRING:
                return new KualiDecimal(in.nextString());
            default:
                throw new IOException("Unexpected token for KualiDecimal: " + in.peek());
        }
    }
}
