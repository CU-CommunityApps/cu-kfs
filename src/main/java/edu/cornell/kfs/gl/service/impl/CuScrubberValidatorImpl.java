package edu.cornell.kfs.gl.service.impl;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.service.impl.ScrubberValidatorImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.MessageBuilder;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.springframework.util.StringUtils;

public class CuScrubberValidatorImpl extends ScrubberValidatorImpl {
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
     * Overridden to use a method version similar to the one from KualiCo's 01/11/2018 patch,
     * to fix an issue where invalid sub-object codes were not being scrubbed properly.
     * 
     * @see org.kuali.kfs.gl.service.impl.ScrubberValidatorImpl#validateSubObjectCode(
     * org.kuali.kfs.gl.businessobject.OriginEntryInformation, org.kuali.kfs.gl.businessobject.OriginEntryInformation,
     * org.kuali.kfs.gl.batch.service.AccountingCycleCachingService)
     */
    @Override
    protected Message validateSubObjectCode(
            OriginEntryInformation originEntry, OriginEntryInformation workingEntry, AccountingCycleCachingService accountingCycleCachingService) {
        LOG.debug("validateFinancialSubObjectCode() started");

        if (!StringUtils.hasText(originEntry.getFinancialSubObjectCode())) {
            workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            return null;
        }

        if (!KFSConstants.getDashFinancialSubObjectCode().equals(originEntry.getFinancialSubObjectCode())) {
            SubObjectCode originEntrySubObject = accountingCycleCachingService.getSubObjectCode(
                    originEntry.getUniversityFiscalYear(), originEntry.getChartOfAccountsCode(), originEntry.getAccountNumber(),
                    originEntry.getFinancialObjectCode(), originEntry.getFinancialSubObjectCode());
            if (originEntrySubObject != null) {
                if (!originEntrySubObject.isActive()) {
                    workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                    return null;
                }
            } else {
                workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                return null;
            }
        }
        workingEntry.setFinancialSubObjectCode(originEntry.getFinancialSubObjectCode());
        return null;
    }

}
