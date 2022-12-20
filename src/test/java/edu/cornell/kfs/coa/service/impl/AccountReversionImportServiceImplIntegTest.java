package edu.cornell.kfs.coa.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.fixture.AccountReversionFixture;
import edu.cornell.kfs.coa.service.AccountReversionImportService;

public class AccountReversionImportServiceImplIntegTest extends KualiIntegTestBase {
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/coa/fixture/AccountReversion.csv";

    private AccountReversionImportService accountReversionImportService;
    private BusinessObjectService businessObjectService;
    private AccountReversion accountReversion;
    private File dataFileSrc;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountReversionImportService = SpringContext.getBean(AccountReversionImportService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        accountReversion = AccountReversionFixture.ACCOUNT_REVERSION_UPLOAD.createAccountReversion();
        dataFileSrc = new File(DATA_FILE_PATH);
    }
    
    public void testImportAccountReversions() {
        accountReversionImportService.importAccountReversions(dataFileSrc);
        
        Map<String, String> pks = new HashMap<String, String>();
        pks.put("universityFiscalYear", accountReversion.getUniversityFiscalYear().toString());
        pks.put("chartOfAccountsCode", accountReversion.getChartOfAccountsCode());
        pks.put("accountNumber", accountReversion.getAccountNumber());
        
        AccountReversion accountReversionDB = businessObjectService.findByPrimaryKey(AccountReversion.class, pks);
        assertTrue(ObjectUtils.isNotNull(accountReversionDB));
        
        assertEquals(accountReversion.getBudgetReversionChartOfAccountsCode(), accountReversionDB.getBudgetReversionChartOfAccountsCode());
        assertEquals(accountReversion.getBudgetReversionAccountNumber(), accountReversionDB.getBudgetReversionAccountNumber());
        assertEquals(accountReversion.getCashReversionFinancialChartOfAccountsCode(), accountReversionDB.getCashReversionFinancialChartOfAccountsCode());
        assertEquals(accountReversion.getCashReversionChartCashObjectCode(), accountReversionDB.getCashReversionChartCashObjectCode());
        assertEquals(accountReversion.getCashReversionAccountNumber(), accountReversionDB.getCashReversionAccountNumber());
        assertEquals(accountReversion.isCarryForwardByObjectCodeIndicator(), accountReversionDB.isCarryForwardByObjectCodeIndicator());
    }

}
