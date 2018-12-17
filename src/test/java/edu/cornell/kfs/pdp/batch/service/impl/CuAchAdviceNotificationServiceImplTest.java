package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.*;

import javax.mail.internet.AddressException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuAchAdviceNotificationServiceImplTest extends CuAchAdviceNotificationServiceImpl {
    
    private CuAchAdviceNotificationServiceImpl cuAchAdviceNotificationService;

    @Before
    public void setUp() throws Exception {
        cuAchAdviceNotificationService = new CuAchAdviceNotificationServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        cuAchAdviceNotificationService = null;
    }

    @Test
    public void validateEmailAddressGoodAddressHyperUser() throws AddressException {
        cuAchAdviceNotificationService.validateEmailAddress("test-user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressGoodAddressUnderscoreUser() throws AddressException {
        cuAchAdviceNotificationService.validateEmailAddress("test_user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressBadAddressUnderscoreDomain() {
        try {
            cuAchAdviceNotificationService.validateEmailAddress("test_user@cornell_univesity.edu");
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
        
    }
    
    @Test
    public void validateEmailAddressBadAddresEmpty() {
        try {
            cuAchAdviceNotificationService.validateEmailAddress("");
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
        
    }

}
