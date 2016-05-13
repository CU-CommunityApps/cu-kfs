package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.UniversityDateService;

@ConfigureContext(session = ccs1)
public class CuGeneralLedgerPendingEntryServiceImplTest extends KualiTestBase {
    private GeneralLedgerPendingEntryService cuGeneralLedgerPendingEntryService;
    UniversityDate ud;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        cuGeneralLedgerPendingEntryService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        ud = SpringContext.getBean(UniversityDateService.class).getCurrentUniversityDate();
    }

    /*
     * test fill in current fiscal year/period for for Null fiscal year/period
     * 
     */
    public void testFillinForNullFiscalYrPeriod() {
        GeneralLedgerPendingEntry glpe = new GeneralLedgerPendingEntry();
        cuGeneralLedgerPendingEntryService.fillInFiscalPeriodYear(glpe);
        assertTrue("should fill in fiscal year", ud.getUniversityFiscalYear().equals(glpe.getUniversityFiscalYear()));            
        assertTrue("should fill in fiscal period code", StringUtils.equals(ud.getUniversityFiscalAccountingPeriod(),glpe.getUniversityFiscalPeriodCode()));            
    }
  
    public void testFillinFiscalYrPeriodForPCDOInInactiveFiscalYrPeriod() {
        GeneralLedgerPendingEntry glpe = new GeneralLedgerPendingEntry();
        glpe.setFinancialDocumentTypeCode(KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD);
        glpe.setUniversityFiscalYear(2012);
        glpe.setUniversityFiscalPeriodCode("01");
        // 2012/01 is an inactive period; so it should be set to current fiscal yr/period for PCDO
        cuGeneralLedgerPendingEntryService.fillInFiscalPeriodYear(glpe);
        assertTrue("should fill in fiscal year", ud.getUniversityFiscalYear().equals(glpe.getUniversityFiscalYear()));            
        assertTrue("should fill in fiscal period code", StringUtils.equals(ud.getUniversityFiscalAccountingPeriod(),glpe.getUniversityFiscalPeriodCode()));            
    }

    public void testNotFillinFiscalYrPeriodForNonPCDODoc() {
        GeneralLedgerPendingEntry glpe = new GeneralLedgerPendingEntry();
        glpe.setFinancialDocumentTypeCode(KFSConstants.FinancialDocumentTypeCodes.ADVANCE_DEPOSIT);
        glpe.setUniversityFiscalYear(2012);
        glpe.setUniversityFiscalPeriodCode("01");
        cuGeneralLedgerPendingEntryService.fillInFiscalPeriodYear(glpe);
        // 2012/01 is an inactive period;  it should not be set to current fiscal yr/period for AD
        assertTrue("should not fill in fiscal year", 2012 == glpe.getUniversityFiscalYear());            
        assertTrue("should not fill in fiscal period code", StringUtils.equals("01",glpe.getUniversityFiscalPeriodCode()));            
    }

    
}
