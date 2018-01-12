package edu.cornell.kfs.gl.service.impl;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.service.impl.ScrubberValidatorImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.MessageBuilder;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.springframework.util.StringUtils;

import edu.cornell.kfs.gl.service.CuSharedScrubberValidatorFixes;

public class CuScrubberValidatorImpl extends ScrubberValidatorImpl implements CuSharedScrubberValidatorFixes {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuScrubberValidatorImpl.class);

    @Override
    protected Message validateUniversityFiscalPeriodCode(OriginEntryInformation originEntry, OriginEntryInformation workingEntry, 
            UniversityDate universityRunDate, AccountingCycleCachingService accountingCycleCachingService) {
        LOG.debug("validateUniversityFiscalPeriodCode() started");
        
        Message retVal = null;
        
        String periodCode = originEntry.getUniversityFiscalPeriodCode();
        if (!StringUtils.hasText(periodCode)) {
            retVal = updateFiscalAccountingPeriodToCurrent(workingEntry, universityRunDate);
        } else {
            AccountingPeriod originEntryAccountingPeriod = accountingCycleCachingService.getAccountingPeriod(
                    originEntry.getUniversityFiscalYear(), originEntry.getUniversityFiscalPeriodCode());
            if (originEntryAccountingPeriod == null) {
                retVal = MessageBuilder.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_FOUND, periodCode, Message.TYPE_FATAL);
            } else if (originEntryAccountingPeriod.getUniversityFiscalPeriodCode().equals(KFSConstants.MONTH13) && !originEntryAccountingPeriod.isOpen()) {
                retVal = updateFiscalAccountingPeriodToCurrent(workingEntry, universityRunDate);
            } else if (!originEntryAccountingPeriod.isActive()) {
                retVal = MessageBuilder.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_ACTIVE, periodCode, Message.TYPE_FATAL);
            }

            workingEntry.setUniversityFiscalPeriodCode(periodCode);
        }

        return retVal;
    }
    
    /**
     * 
     * @param workingEntry
     * @param universityRunDate
     * @return
     */
    private  Message updateFiscalAccountingPeriodToCurrent(OriginEntryInformation workingEntry, UniversityDate universityRunDate) {
        if (universityRunDate.getAccountingPeriod().isOpen()) {
            workingEntry.setUniversityFiscalPeriodCode(universityRunDate.getUniversityFiscalAccountingPeriod());
            workingEntry.setUniversityFiscalYear(universityRunDate.getUniversityFiscalYear());                    
        } else {
            return MessageBuilder.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_CLOSED, " (year " 
                    + universityRunDate.getUniversityFiscalYear() + ", period " 
                    + universityRunDate.getUniversityFiscalAccountingPeriod(), Message.TYPE_FATAL);
        }
        return null;
    }

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
