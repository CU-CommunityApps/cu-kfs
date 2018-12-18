package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.*;

import javax.mail.internet.AddressException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuAchAdviceNotificationWrrorReportServiceImplTest extends CuAchAdviceNotificationServiceImpl {
    
    private CuAchAdviceNotificationWrrorReportServiceImpl cuAchAdviceNotificationWrrorReportService;

    @Before
    public void setUp() throws Exception {
        cuAchAdviceNotificationWrrorReportService = new CuAchAdviceNotificationWrrorReportServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        cuAchAdviceNotificationWrrorReportService = null;
    }

    @Test
    public void validateEmailAddressGoodAddressHyperUser() throws AddressException {
        cuAchAdviceNotificationWrrorReportService.validateEmailAddress("test-user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressGoodAddressUnderscoreUser() throws AddressException {
        cuAchAdviceNotificationWrrorReportService.validateEmailAddress("test_user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressBadAddressUnderscoreDomain() {
        try {
            cuAchAdviceNotificationWrrorReportService.validateEmailAddress("test_user@cornell_univesity.edu");
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
        
    }
    
    @Test
    public void validateEmailAddressBadAddresEmpty() {
        try {
            cuAchAdviceNotificationWrrorReportService.validateEmailAddress("");
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
        
    }

}
