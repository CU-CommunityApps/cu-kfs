package edu.cornell.kfs.gl.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.gl.fixture.BalanceFixture;
import edu.cornell.kfs.gl.service.CuBalanceService;


/**
 * various tests for CuBalanceService
 */
@ConfigureContext
public class CuBalanceServiceTest extends KualiTestBase {
	
	private CuBalanceService balanceService;
	private static int closingFiscalYear;
	private static List<String> charts;
	
    private final static String CHART_IT = "IT";

    private static String DELETE_BALANCES = "delete from GL_BALANCE_T where ";
    private static String RAW_BALANCES = "select * from GL_BALANCE_T where ";
    private UnitTestSqlDao unitTestSqlDao;
    private static boolean runOnce = true;
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        balanceService = SpringContext.getBean(CuBalanceService.class);
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        
        if (runOnce) {
        	closingFiscalYear = new Integer(SpringContext.getBean(ParameterService.class).getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));
        	charts = new ArrayList<>(); 
        	charts.add(CHART_IT);
        	
            DELETE_BALANCES += "UNIV_FISCAL_YR=" + closingFiscalYear ;
            RAW_BALANCES += "UNIV_FISCAL_YR=" + closingFiscalYear;
            
            runOnce = false; // do not run again
        }

    }
    
    /**
     * 
     * This method generates and calls and SQL command to remove all test data from the database.
     */
    public void purgeTestData() {
        unitTestSqlDao.sqlCommand(DELETE_BALANCES);

        List results = unitTestSqlDao.sqlSelect(RAW_BALANCES);
        assertNotNull("List shouldn't be null", results);
        assertEquals("Should return 0 results", 0, results.size());

    }
    
    public void testCountBalancesForFiscalYear(){
    	purgeTestData();
    	
    	BalanceFixture.BALANCE_CHART_IT_COUNT_BALANCES.createBalance(closingFiscalYear);
     	BalanceFixture.BALANCE_CHART_CS_COUNT_BALANCES.createBalance(closingFiscalYear);

    	assertEquals("Tehere should be 1 balance with chanrt IT", 1, balanceService.countBalancesForFiscalYear(closingFiscalYear, charts));
    }
    
    public void testFindNominalActivityBalancesForFiscalYear(){
        
    	purgeTestData();
    	
        BalanceFixture.BALANCE_CHART_IT_NOMINAL_ACTIVITY_BALANCES.createBalance(closingFiscalYear);
      	BalanceFixture.BALANCE_CHART_CS_NOMINAL_ACTIVITY_BALANCES.createBalance(closingFiscalYear);
     	
    	Iterator<Balance> balanceIterator = balanceService.findNominalActivityBalancesForFiscalYear(closingFiscalYear, charts);
    	int nominalActivityBalancesCounter = 0;
    	
    	while (balanceIterator.hasNext()) {
    		Balance balance = balanceIterator.next();
    		nominalActivityBalancesCounter++;
    		if(!(nominalActivityBalancesCounter==0 || nominalActivityBalancesCounter==1)){
    			break;
    		}
    	}
    	
    	assertEquals(1,nominalActivityBalancesCounter);
    }
    
    public void testFindGeneralBalancesToForwardForFiscalYear(){
        
    	purgeTestData();
    	BalanceFixture.BALANCE_CHART_IT_GENERAL_BALANCES_FORWARD.createBalance(closingFiscalYear);
    	BalanceFixture.BALANCE_CHART_CS_GENERAL_BALANCES_FORWARD.createBalance(closingFiscalYear);
     	
    	Iterator<Balance> balanceIterator = balanceService.findGeneralBalancesToForwardForFiscalYear(closingFiscalYear, charts);
    	int generalBalancesToForwardCounter = 0;
    	
    	while (balanceIterator.hasNext()) {
    		Balance balance = balanceIterator.next();
    		generalBalancesToForwardCounter++;
    		if(!(generalBalancesToForwardCounter==0 || generalBalancesToForwardCounter==1)){
    			break;
    		}
    	}
    	
    	assertEquals(1, generalBalancesToForwardCounter);
    }
    
    public void testFindCumulativeBalancesToForwardForFiscalYear(){
        
    	purgeTestData();
    	
    	BalanceFixture.BALANCE_CHART_IT_CUMULATIVE_BALANCES_FORWARD.createBalance(closingFiscalYear);
    	BalanceFixture.BALANCE_CHART_CS_CUMULATIVE_BALANCES_FORWARD.createBalance(closingFiscalYear);
    	
    	Iterator<Balance> balanceIterator = balanceService.findCumulativeBalancesToForwardForFiscalYear(closingFiscalYear, charts);
    	int cumulativeBalancesToForwardCounter = 0;
    	
    	while (balanceIterator.hasNext()) {
    		Balance balance = balanceIterator.next();
    		cumulativeBalancesToForwardCounter++;
    		if(!(cumulativeBalancesToForwardCounter==0 || cumulativeBalancesToForwardCounter==1)){
    			break;
    		}
    	}
    	
    	assertEquals(1, cumulativeBalancesToForwardCounter);
    }

}
