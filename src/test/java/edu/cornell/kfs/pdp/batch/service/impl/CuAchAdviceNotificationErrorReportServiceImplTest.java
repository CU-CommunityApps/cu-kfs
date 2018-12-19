package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.*;

import javax.mail.internet.AddressException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

public class CuAchAdviceNotificationErrorReportServiceImplTest {
    
    private CuAchAdviceNotificationErrorReportServiceImpl cuAchAdviceNotificationErrorReportService;

    @Before
    public void setUp() throws Exception {
        cuAchAdviceNotificationErrorReportService = new CuAchAdviceNotificationErrorReportServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        cuAchAdviceNotificationErrorReportService = null;
    }

    @Test
    public void validateEmailAddressGoodAddressHyperUser() throws AddressException {
        cuAchAdviceNotificationErrorReportService.validateEmailAddress("test-user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressGoodAddressUnderscoreUser() throws AddressException {
        cuAchAdviceNotificationErrorReportService.validateEmailAddress("test_user@cornell-university.edu");
        assertTrue(true);
    }
    
    @Test
    public void validateEmailAddressBadAddressUnderscoreDomain() {
        try {
            cuAchAdviceNotificationErrorReportService.validateEmailAddress("test_user@cornell_univesity.edu");
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
        
    }
    
    @Test
    public void validateEmailAddressBadAddressEmpty() {
        try {
            cuAchAdviceNotificationErrorReportService.validateEmailAddress(StringUtils.EMPTY);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }
    
    @Test
    public void validateEmailAddressBadAddressSpace() {
        try {
            cuAchAdviceNotificationErrorReportService.validateEmailAddress(KFSConstants.BLANK_SPACE);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }
    
    @Test
    public void validateEmailAddressBadAddressNull() {
        try {
            cuAchAdviceNotificationErrorReportService.validateEmailAddress(null);
            assertTrue(false);
        } catch (AddressException ae) {
            assertTrue(true);
        }
    }

}
