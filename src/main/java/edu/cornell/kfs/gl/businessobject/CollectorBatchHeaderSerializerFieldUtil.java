package edu.cornell.kfs.gl.businessobject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;

/**
 * BO serialization utility subclass for serializing CollectorBatch BOs
 * into Collector flat-file header lines.
 * 
 * The "HD" (header) record type will be forcibly written to the line;
 * there's no need to configure the record type on the BO itself.
 * 
 * Also, to get the transmission date to appear at the expected location
 * as outlined in the Collector specs, this implementation will left-pad
 * the transmission date with spaces accordingly. This is necessary
 * because of how the CollectorBatch fields and lengths are currently
 * being configured in the data dictionary.
 */
public class CollectorBatchHeaderSerializerFieldUtil extends CollectorSerializerFieldUtilsWithDate {

    protected static final String LEFT_PADDING_FOR_TRANSMISSION_DATE = "     ";

    @Override
    protected Map<String,Function<Object,String>> getCustomSerializerFunctions() {
        Map<String,Function<Object,String>> customFunctions = new HashMap<>();
        
        customFunctions.put(KFSPropertyConstants.TRANSMISSION_DATE, this::formatTransmissionDateWithLeftPadding);
        customFunctions.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, this::forciblyMarkAsHeaderRecord);
        
        return customFunctions;
    }

    protected String formatTransmissionDateWithLeftPadding(Object transmissionDate) {
        return LEFT_PADDING_FOR_TRANSMISSION_DATE + formatSqlDateForCollectorFile(transmissionDate);
    }

    protected String forciblyMarkAsHeaderRecord(Object recordType) {
        return CuGeneralLedgerConstants.COLLECTOR_HEADER_RECORD_TYPE;
    }

}
