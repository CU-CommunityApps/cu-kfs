package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext
public class CuAccountServiceImplIntegTest extends KualiIntegTestBase {

	private AccountService accountService;
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountService = SpringContext.getBean(AccountService.class);
    }
    
    public void testGetDefaultLaborBenefitRateCategoryCodeForAccountType() {
    	assertEquals("--", accountService.getDefaultLaborBenefitRateCategoryCodeForAccountType("EX"));
    	assertEquals("EN", accountService.getDefaultLaborBenefitRateCategoryCodeForAccountType("EN"));
    }
    
}
