package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;

public class RassAgencyTypeConverter extends RassParameterMapingValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassAgencyTypeConverter.class);
    
    @Override
    public String getParameterName() {
        return CuCGParameterConstants.AGENCY_TYPE_MAPPINGS;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
