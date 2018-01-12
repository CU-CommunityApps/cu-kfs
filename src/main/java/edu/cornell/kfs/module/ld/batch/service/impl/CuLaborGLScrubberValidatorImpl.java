package edu.cornell.kfs.module.ld.batch.service.impl;

import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.module.ld.batch.service.impl.LaborGLScrubberValidatorImpl;
import org.kuali.kfs.sys.Message;

import edu.cornell.kfs.gl.service.CuSharedScrubberValidatorFixes;

public class CuLaborGLScrubberValidatorImpl extends LaborGLScrubberValidatorImpl implements CuSharedScrubberValidatorFixes {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuLaborGLScrubberValidatorImpl.class);

    /**
     * Overridden to include a sub-object validation fix from KualiCo's 01/11/2018 patch.
     * 
     * @see org.kuali.kfs.gl.service.impl.ScrubberValidatorImpl#validateSubObjectCode(
     * org.kuali.kfs.gl.businessobject.OriginEntryInformation, org.kuali.kfs.gl.businessobject.OriginEntryInformation,
     * org.kuali.kfs.gl.batch.service.AccountingCycleCachingService)
     */
    @Override
    protected Message validateSubObjectCode(
            OriginEntryInformation originEntry, OriginEntryInformation workingEntry, AccountingCycleCachingService accountingCycleCachingService) {
        LOG.debug("validateFinancialSubObjectCode() started");
        
        return validateSubObjectCodeInternal(originEntry, workingEntry, accountingCycleCachingService);
    }

}
