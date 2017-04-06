package edu.cornell.kfs.concur.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurEventNotificationServiceImplTest {
    
    ConcurEventNotificationServiceImpl concurEventNotificationServiceImpl;

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
        assertEquals("Expeted NotificationId and ActualNotificationId should be the same", expectedNotifcationId, actualNotificationId);
    }

}
