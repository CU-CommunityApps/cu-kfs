package edu.cornell.kfs.sys.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.GeneralLedgerPendingEntryServiceImpl;

public class CuGeneralLedgerPendingEntryServiceImpl extends GeneralLedgerPendingEntryServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuGeneralLedgerPendingEntryServiceImpl.class);
    
    /**
     * This method takes a GLPE and updates the fiscal year and fiscal accounting period to the current open values if either or both of them are not currently
     * assigned to the provided GLPE.
     * 
     * Additionally, in support of the Procurement Card backpost feature, this method will also reset the accounting period on the GLPE to the current period
     * if the accounting period currently assigned to the PCard GLPE is no longer active.
     * 
     * @param glpe Instance of the GeneralLedgerPendingEntry object that will be analyzed and modified by this method.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void fillInFiscalPeriodYear(GeneralLedgerPendingEntry glpe) {
        LOG.debug("fillInFiscalPeriodYear() started");

        if ((glpe.getUniversityFiscalPeriodCode() == null) || (glpe.getUniversityFiscalYear() == null)) {
            setFiscalPeriodYearToToday(glpe);
        } else if (glpe.getFinancialDocumentTypeCode().equals(KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD)) {
         // Need to handle backposted PCard transactions if they don't get auto-approved before the CLOSING accounting period is closed.
            if ((glpe.getUniversityFiscalPeriodCode() != null) && (glpe.getUniversityFiscalYear() != null)) {
                AccountingPeriod acctPer = SpringContext.getBean(AccountingPeriodService.class)
                        .getByPeriod(glpe.getUniversityFiscalPeriodCode(), glpe.getUniversityFiscalYear());
                // Reset accounting period on GLPE if period assigned is no longer active
                if (!acctPer.isActive()) {
                    setFiscalPeriodYearToToday(glpe);
                }
            }
        }
    }
    
    /**
     * This method takes updates the provided GLPE with the current fiscal period and fiscal year.
     * @param glpe
     */
    private void setFiscalPeriodYearToToday(GeneralLedgerPendingEntry glpe) {
        UniversityDate ud = SpringContext.getBean(UniversityDateService.class).getCurrentUniversityDate();

        glpe.setUniversityFiscalYear(ud.getUniversityFiscalYear());
        glpe.setUniversityFiscalPeriodCode(ud.getUniversityFiscalAccountingPeriod());
    }

}
