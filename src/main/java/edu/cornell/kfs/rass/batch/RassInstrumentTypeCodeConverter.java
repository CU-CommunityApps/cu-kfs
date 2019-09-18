package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;

public class RassInstrumentTypeCodeConverter extends RassParameterMappingValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassInstrumentTypeCodeConverter.class);

    @Override
    public String getParameterName() {
        return CuCGParameterConstants.INSTRUMENT_TYPE_CODE_MAPPINGS;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
