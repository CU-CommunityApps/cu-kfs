package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;

import edu.cornell.kfs.coa.service.AccountVerificationWebService;

@ConfigureContext
public class AccountVerificationWebServiceImplTest extends KualiIntegTestBase {
    private AccountVerificationWebService accountVerificationWebService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountVerificationWebService = new AccountVerificationWebServiceImpl();
    }

    public void testIsValidObjectCode() throws Exception {
        assertFalse("should not be a valid object code", accountVerificationWebService.isValidObjectCode("IT", "NOTVALID"));
        assertTrue("should be a valid object code", accountVerificationWebService.isValidObjectCode("IT", "6500"));
    }

    public void testIsValidSubAccount() throws Exception {
        assertFalse("should not be a valid sub account", accountVerificationWebService.isValidSubAccount("IT", "1003724", "NOTVALID"));
        assertTrue("should be a valid sub account", accountVerificationWebService.isValidSubAccount("IT", "1003724", "00018"));
    }

    public void testIsValidSubObjectCode() throws Exception {
        assertFalse("should not be a valid sub object code", accountVerificationWebService.isValidSubObjectCode("IT", "G904714", "6115", "NOTVALID"));
        assertTrue("should be a valid sub object code", accountVerificationWebService.isValidSubObjectCode("IT", "G904714", "6115", "150"));
    }

    public void testIsValidProjectCode() throws Exception {
        assertFalse("should not be a valid project code", accountVerificationWebService.isValidProjectCode("NOTVALID"));
        assertTrue("should be a valid project code", accountVerificationWebService.isValidProjectCode("F-ARUBA"));
    }

    public void testIsValidAccountString() throws Exception {
        assertFalse("should not be a valid account string", accountVerificationWebService.isValidAccountString("IT", "G904714", "", "6115", "NOTVALID", "" ));
        assertTrue("should be a valid account string", accountVerificationWebService.isValidAccountString("IT", "G904714", "", "6115", "150", ""));
    }

}