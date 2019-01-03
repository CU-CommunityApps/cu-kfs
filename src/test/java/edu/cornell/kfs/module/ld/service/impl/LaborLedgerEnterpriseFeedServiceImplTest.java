package edu.cornell.kfs.module.ld.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Date;
import java.util.Collection;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

@ConfigureContext
public class LaborLedgerEnterpriseFeedServiceImplTest extends TestCase {
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/ld/fixture/SMGROS.data";
    private static final String BAD_DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/ld/fixture/SMGROSBAD.data";

    private static final String DISENCUMBRANCE_ACCOUNTING_LINE = "2014IT6258326-----5020---AC";
    
    private LaborLedgerEnterpriseFeedServiceImpl ldService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ldService = new LaborLedgerEnterpriseFeedServiceImpl();
        ldService.setDateTimeService(new DateTimeServiceImpl());
        ldService.setAccountingPeriodService(new MockAccountingPeriodService());
    }

    public void testCreateDisencumbrance() throws IOException {
    	File dataFile = new File(DATA_FILE_PATH);
    	InputStream disencumFileInputStream = null;
        disencumFileInputStream = ldService.createDisencumbrance(new FileInputStream(dataFile));
        
        assertNotNull(disencumFileInputStream);
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(disencumFileInputStream, writer, "UTF-8");
        String theString = writer.toString();
        assertTrue(theString.contains(DISENCUMBRANCE_ACCOUNTING_LINE));

    }
    
    public void testCreateDisencumbranceBadFile() throws FileNotFoundException {
    	File dataFile = new File(BAD_DATA_FILE_PATH);
    	InputStream disencumFileInputStream = null;
        disencumFileInputStream = ldService.createDisencumbrance(new FileInputStream(dataFile));
        
        assertNull(disencumFileInputStream);
   
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
            return null;
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

        @Override
        public Date getAccountingPeriodReversalDateByType(
                String avTypeCode, String selectedPostingPeriodCode, Integer selectedPostingYear, Date documentCreateDate) {
            return null;
        }

		@Override
		public AccountingPeriod getPreviousAccountingPeriod(AccountingPeriod currentPeriod) {
			return null;
		}
    }
}
