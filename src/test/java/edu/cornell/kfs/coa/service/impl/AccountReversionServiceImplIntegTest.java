package edu.cornell.kfs.coa.service.impl;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.fixture.AccountReversionFixture;
import edu.cornell.kfs.coa.fixture.ReversionCategoryFixture;
import edu.cornell.kfs.coa.service.AccountReversionService;
import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;

@ConfigureContext
public class AccountReversionServiceImplIntegTest extends KualiIntegTestBase {
	private static final String GOOD_CATEGORY_CODE = "A1";
	private static final String GOOD_CATEGORY_NAME = "Reversion";
	private static final String NOT_VALID = "NOTVALID";
	
	private AccountReversionService accountReversionService;
	private BusinessObjectService businessObjectService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountReversionService = SpringContext.getBean(AccountReversionService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);

    }
    
    public void testGetByPrimaryId() {
    	//Grab data from bo service
    	AccountReversion accountReversionBaseData = AccountReversionFixture.ACCOUNT_REVERSION_GOOD.createAccountReversion(businessObjectService);
    	
    	//Grab data via service
    	AccountReversion accountReversion = accountReversionService.getByPrimaryId(2014, "IT", "G254700");
    	
    	//compare that the data is the same
    	assertEquals(accountReversionBaseData.getBudgetReversionChartOfAccountsCode(), accountReversion.getBudgetReversionChartOfAccountsCode());
    	assertEquals(accountReversionBaseData.getBudgetReversionAccountNumber(), accountReversion.getBudgetReversionAccountNumber());
    	assertEquals(accountReversionBaseData.getCashReversionFinancialChartOfAccountsCode(), accountReversion.getCashReversionFinancialChartOfAccountsCode());
    	assertEquals(accountReversionBaseData.getCashReversionChartCashObjectCode(), accountReversion.getCashReversionChartCashObjectCode());
    	assertEquals(accountReversionBaseData.getCashReversionAccountNumber(), accountReversion.getCashReversionAccountNumber());
    	assertEquals(accountReversionBaseData.isCarryForwardByObjectCodeIndicator(), accountReversion.isCarryForwardByObjectCodeIndicator());
    }
    
    public void testGetCategories() {
    	ReversionCategory goodReversionCategory = ReversionCategoryFixture.A1_CATEGORY.createReversionCategory();
    	
    	Map<String, ReversionCategoryLogic> results = accountReversionService.getCategories();
    	assertTrue("categories should not be empty", results.size() > 0);
    	assertTrue(results.containsKey(goodReversionCategory.getReversionCategoryCode()));
    	assertEquals(goodReversionCategory.getReversionCategoryName(), results.get(goodReversionCategory.getReversionCategoryCode()).getName());
    }
    
    public void testGetCategoryList() {
    	ReversionCategory goodReversionCategory = ReversionCategoryFixture.A1_CATEGORY.createReversionCategory();
    	ReversionCategory badReversionCategory = ReversionCategoryFixture.BOGUS_CATEGORY.createReversionCategory();


    	List<ReversionCategory> results = accountReversionService.getCategoryList();
    	assertTrue("categories should not be empty", results.size() > 0);
    	
    	boolean foundGoodMatch = false;
    	for (ReversionCategory reversionCategory : results) {
    		if (reversionCategory.getReversionCategoryCode().equals(goodReversionCategory.getReversionCategoryCode())) {
    			foundGoodMatch = true;
    			assertEquals(goodReversionCategory.getReversionCategoryName(), reversionCategory.getReversionCategoryName());
    			assertEquals(goodReversionCategory.getReversionSortCode(), reversionCategory.getReversionSortCode());
    			assertEquals(goodReversionCategory.isActive(), reversionCategory.isActive());
    		} else if (reversionCategory.getReversionCategoryCode().equals(badReversionCategory.getReversionCategoryCode())) {
    			fail("should not have found bogus match");
    		}
    	}
    	
    	assertTrue("should find a good match", foundGoodMatch);
    	
    }
    
    public void testIsCategoryActive() {
    	assertTrue("A1 category should be active", accountReversionService.isCategoryActive(GOOD_CATEGORY_CODE));
    	assertFalse("NOTVALID category should not be active", accountReversionService.isCategoryActive(NOT_VALID));
    }
    
    public void testIsCategoryActiveByName() {
    	assertTrue("Reversion category should be active", accountReversionService.isCategoryActiveByName(GOOD_CATEGORY_NAME));
    	assertFalse("NOTVALID category should not be active", accountReversionService.isCategoryActiveByName(NOT_VALID));
    }

}
