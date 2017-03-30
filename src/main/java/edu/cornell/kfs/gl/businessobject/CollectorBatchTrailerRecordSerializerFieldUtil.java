package edu.cornell.kfs.gl.businessobject;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.sys.businessobject.BusinessObjectFlatFileSerializerFieldUtils;

/**
 * BO serialization utility subclass for serializing CollectorBatch BOs
 * into Collector flat-file trailer lines.
 * 
 * The "TL" (trailer) record type will be forcibly written to the line;
 * there's no need to configure the record type on the BO itself.
 */
public class CollectorBatchTrailerRecordSerializerFieldUtil extends BusinessObjectFlatFileSerializerFieldUtils {

    @Override
    protected Map<String, Function<Object, String>> getCustomSerializerFunctions() {
        return Collections.singletonMap(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, this::forciblyMarkAsTrailerRecord);
    }

    protected String forciblyMarkAsTrailerRecord(Object recordType) {
        return CuGeneralLedgerConstants.COLLECTOR_TRAILER_RECORD_TYPE;
    }

}
