package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.sql.Date;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.UniversityDateService;

@ConfigureContext(session = ccs1)
public class CuGeneralLedgerPendingEntryServiceImplTest extends TestCase {
	private AccountingPeriodService accountingPeriodService;
    private CuGeneralLedgerPendingEntryServiceImpl cuGeneralLedgerPendingEntryService;
    private UniversityDateService universityDateService;  
    private UniversityDate ud;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountingPeriodService = new MockAccountingPeriodService();
        cuGeneralLedgerPendingEntryService = new CuGeneralLedgerPendingEntryServiceImpl();
        cuGeneralLedgerPendingEntryService.setAccountingPeriodService(accountingPeriodService);
        universityDateService = new MockUniversityDateService();   
        cuGeneralLedgerPendingEntryService.setUniversityDateService(universityDateService);
        
        ud = new UniversityDate();
        ud.setUniversityFiscalYear(2016);
        ud.setUniversityFiscalAccountingPeriod("5");
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

    private class MockAccountingPeriodService implements AccountingPeriodService {

        @Override
        public Collection<AccountingPeriod> getAllAccountingPeriods() {
            return null;
        }

        @Override
        public Collection<AccountingPeriod> getOpenAccountingPeriods() {
            return null;
        }

        @Override
        public AccountingPeriod getByPeriod(String s, Integer integer) {
        	AccountingPeriod accountingPeriod = new AccountingPeriod();
        	accountingPeriod.setUniversityFiscalPeriodCode(s);
        	accountingPeriod.setUniversityFiscalYear(integer);
            return accountingPeriod;
        }

        @Override
        public AccountingPeriod getByStringDate(String s) {
            return null;
        }

        @Override
        public AccountingPeriod getByDate(Date date) {
            return new AccountingPeriod();
        }

        @Override
        public int compareAccountingPeriodsByDate(AccountingPeriod accountingPeriod, AccountingPeriod accountingPeriod1) {
            return 0;
        }

        @Override
        public void clearCache() {

        }
    }
    
    private class MockUniversityDateService implements UniversityDateService{

		@Override
		public UniversityDate getCurrentUniversityDate() {
			UniversityDate universityDate = new UniversityDate();
			universityDate.setUniversityFiscalYear(2016);
			universityDate.setUniversityFiscalAccountingPeriod("5");
			return universityDate;
		}

		@Override
		public Integer getFiscalYear(java.util.Date date) {
			return null;
		}

		@Override
		public java.util.Date getFirstDateOfFiscalYear(Integer fiscalYear) {
			return null;
		}

		@Override
		public java.util.Date getLastDateOfFiscalYear(Integer fiscalYear) {
			return null;
		}

		@Override
		public Integer getCurrentFiscalYear() {
			return null;
		}
    	
    }
    
}
