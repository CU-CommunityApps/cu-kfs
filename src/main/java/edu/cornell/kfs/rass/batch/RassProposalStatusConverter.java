package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;

public class RassProposalStatusConverter extends RassParameterMapingValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassProposalStatusConverter.class);
    
    @Override
    public String getParameterName() {
        return CuCGParameterConstants.PROPOSAL_STATUS_MAPPINGS;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
