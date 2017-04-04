package edu.cornell.kfs.concur.batch.businessobject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.concur.ConcurConstants;

/**
 * BO serialization utility subclass for serializing CollectorBatch BOs
 * into Collector flat-file trailer lines.
 * 
 * The "TL" (trailer) record type will be forcibly written to the line;
 * there's no need to configure the record type on the BO itself.
 * 
 * Also, in the base KFS parser util class that this serializer depends on,
 * much of the whitespace configuration at the start of the line
 * is actually deriving its text lengths from certain batch header fields.
 * This implementation will forcibly write out the affected properties
 * as whitespace, to comply with the Collector flat file specs.
 */
public class CollectorBatchTrailerRecordSerializerFieldUtil extends BusinessObjectFlatFileSerializerFieldUtils {

    @Override
    protected Map<String,Function<Object,String>> getCustomSerializerFunctions() {
        Map<String,Function<Object,String>> customFunctions = new HashMap<>();
        
        customFunctions.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, this::convertToWhitespace);
        customFunctions.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, this::convertToWhitespace);
        customFunctions.put(KFSPropertyConstants.ORGANIZATION_CODE, this::convertToWhitespace);
        customFunctions.put(KFSPropertyConstants.TRANSMISSION_DATE, this::convertToWhitespace);
        customFunctions.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, this::forciblyMarkAsTrailerRecord);
        
        return customFunctions;
    }

    protected String convertToWhitespace(Object propertyValue) {
        return StringUtils.EMPTY;
    }

    protected String forciblyMarkAsTrailerRecord(Object recordType) {
        return ConcurConstants.COLLECTOR_TRAILER_RECORD_TYPE;
    }

}
