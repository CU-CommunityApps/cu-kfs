package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.coa.fixture.AccountReversionFixture;
import edu.cornell.kfs.coa.service.AccountReversionDetailTrickleDownInactivationService;

@ConfigureContext
public class AccountReversionDetailTrickleDownInactivationServiceImplIntegTest extends KualiIntegTestBase {

    private AccountReversionDetailTrickleDownInactivationService accountReversionDetailTrickleDownInactivationService;
    private BusinessObjectService businessObjectService;
    private AccountReversion accountReversion;
    private AccountReversionDetail detail;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountReversionDetailTrickleDownInactivationService = SpringContext.getBean(AccountReversionDetailTrickleDownInactivationService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        accountReversion = AccountReversionFixture.ACCOUNT_REVERSION_GOOD.createAccountReversion(businessObjectService);
        detail = accountReversion.getAccountReversionDetails().get(0);

    }
    
    protected void setDetailActive(AccountReversionDetail detail, boolean value){
        detail.setActive(value);
        try {
            businessObjectService.save(detail);
        }
        catch (RuntimeException re) {
            fail("should be able to save detail object");
        }
    }
    
    public void testTrickleDownInactiveAccountReversionDetails() {
        setDetailActive(detail, true);
        assertTrue(detail.isActive());
        
        try {
            accountReversionDetailTrickleDownInactivationService.trickleDownInactiveAccountReversionDetails(accountReversion, "5692432");
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
            assertFalse(detail.isActive());
        }
    }
    
    public void testTrickleDownActiveAccountReversionDetails() {
        setDetailActive(detail, false);
        assertFalse(detail.isActive());
        
        try {
            accountReversionDetailTrickleDownInactivationService.trickleDownActiveAccountReversionDetails(accountReversion, "5692432");
        } catch (RuntimeException e) {
            assertTrue(detail.isActive());
        }
        

    }

}
