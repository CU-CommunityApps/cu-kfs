package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;

public class RassGrantDescriptionConverter extends RassParameterMappingValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassGrantDescriptionConverter.class);
    
    @Override
    public String getParameterName() {
        return CuCGParameterConstants.GRANT_DESCRIPTION_MAPPINGS;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

}
