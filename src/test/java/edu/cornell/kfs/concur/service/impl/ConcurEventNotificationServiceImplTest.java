package edu.cornell.kfs.concur.service.impl;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.rass.RassConstants;

public class ConcurEventNotificationServiceImplTest {
    private static final Logger LOG = LogManager.getLogger(ConcurEventNotificationServiceImplTest.class);
    private ConcurEventNotificationServiceImpl concurEventNotificationServiceImpl;

    @Before
    public void setUp() throws Exception {
        concurEventNotificationServiceImpl = new ConcurEventNotificationServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        concurEventNotificationServiceImpl = null;
    }

    @Test
    public void validateFindingNotificationId() {
        String notificationURI = "https://www.concursolutions.com/api/platform/notifications/v1.0/notification/gWu2zdPNXG6oQHp0VDaEmNn77NGJ5aQ9rww";
        String expectedNotifcationId = "gWu2zdPNXG6oQHp0VDaEmNn77NGJ5aQ9rww";
        String actualNotificationId = concurEventNotificationServiceImpl.findNotificationId(notificationURI);
        assertEquals("Expected NotificationId and ActualNotificationId should be the same", expectedNotifcationId, actualNotificationId);
    }
    
    @Test
    public void validateShortRespone() {
        String shortMessage = "some short message";
        String actualResults = concurEventNotificationServiceImpl.truncateValidationResultMessageToMaximumDatabaseFieldSize(shortMessage);
        assertEquals("short message should be the same", shortMessage, actualResults);
    }
    
    @Test
    public void validateNullRespone() {
        String nullMessage = null;
        String actualResults = concurEventNotificationServiceImpl.truncateValidationResultMessageToMaximumDatabaseFieldSize(nullMessage);
        assertEquals("null message should be the same", nullMessage, actualResults);
    }
    
    @Test
    public void validateLongRespone() {
        String longMessage = buildLongMessage();
        assertEquals("long message should 2000 characters long", ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH, longMessage.length());
        String actualResults = concurEventNotificationServiceImpl.truncateValidationResultMessageToMaximumDatabaseFieldSize(longMessage);
        assertEquals("long message should be the same", longMessage, actualResults);
    }
    
    @Test
    public void validateOverageRespone() {
        String longMessage = buildLongMessage();
        String overageMessage = longMessage + "some extra stuff to exceed max length";
        String actualResults = concurEventNotificationServiceImpl.truncateValidationResultMessageToMaximumDatabaseFieldSize(overageMessage);
        assertEquals("message should be the long message without the extra stuff", longMessage, actualResults);
    }
    
    private String buildLongMessage() {
        return StringUtils.repeat("A", ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH);
    }
    
    @Test
    public void findConcurFailedEventQueueProcessingControllerReadOnly() {
        validateConcurFailedEventQueueProcessingController(ConcurFailedEventQueueProcessingController.READONLY, 
                findConcurFailedEventQueueProcessingControllerByString(RassConstants.RASS_FAILED_EVENT_QUEUE_READONLY));
    }
    
    @Test
    public void findConcurFailedEventQueueProcessingControllerReadWrite() {
        validateConcurFailedEventQueueProcessingController(ConcurFailedEventQueueProcessingController.READWRITE, 
                findConcurFailedEventQueueProcessingControllerByString(KFSConstants.ParameterValues.YES));
    }
    
    @Test
    public void findConcurFailedEventQueueProcessingControllerOff() {
        validateConcurFailedEventQueueProcessingController(ConcurFailedEventQueueProcessingController.OFF, 
                findConcurFailedEventQueueProcessingControllerByString(KFSConstants.ParameterValues.NO));
    }
    
    @Test
    public void findConcurFailedEventQueueProcessingControllerDefault() {
        validateConcurFailedEventQueueProcessingController(ConcurFailedEventQueueProcessingController.OFF, 
                findConcurFailedEventQueueProcessingControllerByString("foo"));
    }
    
    private ConcurFailedEventQueueProcessingController findConcurFailedEventQueueProcessingControllerByString(String parm) {
        return ConcurFailedEventQueueProcessingController.getConcurFailedEventQueueProcessingControllerFromString(parm);
    }
    
    private void validateConcurFailedEventQueueProcessingController(ConcurFailedEventQueueProcessingController expected, 
            ConcurFailedEventQueueProcessingController actual) {
        LOG.info("validateConcurFailedEventQueueProcessingController, actual: " + actual.toString());
        assertEquals(expected, actual);
    }

}
