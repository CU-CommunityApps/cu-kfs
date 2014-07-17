package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.coa.service.AccountDelegateService;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

public class CuAccountDelegateServiceImplTest extends KualiTestBase {

    private AccountDelegateService accountDelegateService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountDelegateService = SpringContext.getBean(AccountDelegateService.class);
    }
    
    public void testUpdateDelegationRole() {
        //does nothing, for coverage
        accountDelegateService.updateDelegationRole();
    }

}
