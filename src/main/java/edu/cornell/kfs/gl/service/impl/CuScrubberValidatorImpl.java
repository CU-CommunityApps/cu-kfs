package edu.cornell.kfs.gl.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.service.impl.ScrubberValidatorImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.service.MessageBuilderService;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.springframework.util.StringUtils;

public class CuScrubberValidatorImpl extends ScrubberValidatorImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    private MessageBuilderService messageBuilderService;

    @Override
    protected Message validateUniversityFiscalPeriodCode(
            final OriginEntryInformation originEntry, 
            final OriginEntryInformation workingEntry, 
            final UniversityDate universityRunDate, 
            final AccountingCycleCachingService accountingCycleCachingService) {
        LOG.debug("validateUniversityFiscalPeriodCode() started");
        
        Message retVal = null;
        
        final String periodCode = originEntry.getUniversityFiscalPeriodCode();
        if (!StringUtils.hasText(periodCode)) {
            retVal = updateFiscalAccountingPeriodToCurrent(workingEntry, universityRunDate);
        } else {
            AccountingPeriod originEntryAccountingPeriod = accountingCycleCachingService.getAccountingPeriod(
                    originEntry.getUniversityFiscalYear(), originEntry.getUniversityFiscalPeriodCode());
            if (originEntryAccountingPeriod == null) {
                retVal = messageBuilderService.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_FOUND, periodCode, Message.TYPE_FATAL);
            } else if (originEntryAccountingPeriod.getUniversityFiscalPeriodCode().equals(KFSConstants.MONTH13) && !originEntryAccountingPeriod.isOpen()) {
                retVal = updateFiscalAccountingPeriodToCurrent(workingEntry, universityRunDate);
            } else if (!originEntryAccountingPeriod.isActive()) {
                retVal = messageBuilderService.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_ACTIVE, periodCode, Message.TYPE_FATAL);
            } else {
            	    workingEntry.setUniversityFiscalPeriodCode(periodCode);
            }
        }

        return retVal;
    }
    
    /**
     * 
     * @param workingEntry
     * @param universityRunDate
     * @return
     */
    private  Message updateFiscalAccountingPeriodToCurrent(
            final OriginEntryInformation workingEntry, 
            final UniversityDate universityRunDate) {
        if (universityRunDate.getAccountingPeriod().isOpen()) {
            workingEntry.setUniversityFiscalPeriodCode(universityRunDate.getUniversityFiscalAccountingPeriod());
            workingEntry.setUniversityFiscalYear(universityRunDate.getUniversityFiscalYear());                    
        } else {
            return messageBuilderService.buildMessage(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_CLOSED, " (year " 
                    + universityRunDate.getUniversityFiscalYear() + ", period " 
                    + universityRunDate.getUniversityFiscalAccountingPeriod(), Message.TYPE_FATAL);
        }
        return null;
    }
    
    public void setMessageBuilderService(final MessageBuilderService messageBuilderService) {
        this.messageBuilderService = messageBuilderService;
    }

}
