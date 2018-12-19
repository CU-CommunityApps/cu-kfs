package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.*;

import javax.mail.internet.AddressException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

public class CuAchAdviceNotificationWrrorReportServiceImplTest {
    
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
    public void validateEmailAddressBadAddressEmpty() {
        try {
            cuAchAdviceNotificationWrrorReportService.validateEmailAddress(StringUtils.EMPTY);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }
    
    @Test
    public void validateEmailAddressBadAddressSpace() {
        try {
            cuAchAdviceNotificationWrrorReportService.validateEmailAddress(KFSConstants.BLANK_SPACE);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }
    
    @Test
    public void validateEmailAddressBadAddressNull() {
        try {
            cuAchAdviceNotificationWrrorReportService.validateEmailAddress(null);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }

}
