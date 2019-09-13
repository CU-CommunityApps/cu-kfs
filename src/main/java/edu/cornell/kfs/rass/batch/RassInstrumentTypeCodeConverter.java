package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.RassParameterConstants;

public class RassInstrumentTypeCodeConverter extends RassParameterMappingValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassInstrumentTypeCodeConverter.class);
    
    @Override
    public String getParameterName() {
        return RassParameterConstants.INSTRUMENT_TYPE_CODE_MAPPINGS;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
