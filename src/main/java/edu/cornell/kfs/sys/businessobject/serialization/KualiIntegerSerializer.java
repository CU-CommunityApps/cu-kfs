package edu.cornell.kfs.sys.businessobject.serialization;

import java.io.IOException;

import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * CU-specific JsonSerializer subclass that converts KualiInteger values into integer strings.
 * The superclass can potentially serialize KualiInteger values as exponential numbers
 * if they're too large, which is why this workaround is needed.
 * 
 * Some of the logic below is based on KualiCo's PersistableBusinessObjectSerializer class
 * as well as KualiCo's ProcessSummaryBoToLookupJsonConverter class (introduced by FINP-11364).
 */
public class KualiIntegerSerializer extends JsonSerializer<KualiInteger> {

    @Override
    public void serialize(final KualiInteger value, final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider) throws IOException {
        if (ObjectUtils.isNotNull(value)) {
            final String stringValue = CUKFSConstants.NoScientificNotationFormat.DECIMAL_FORMAT.format(value);
            jsonGenerator.writeString(stringValue);
        } else {
            jsonGenerator.writeNull();
        }
    }

}
