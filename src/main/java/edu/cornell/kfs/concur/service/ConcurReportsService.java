package edu.cornell.kfs.concur.service;

import edu.cornell.kfs.concur.eventnotification.rest.xmlObjects.ConcurEventNotificationListDTO;

public interface ConcurReportsService {
    
    boolean deleteFailedEventQueueItemInConcur(String noticationId);
    
    ConcurEventNotificationListDTO retrieveFailedEventQueueNotificationsFromConcur();
    
    String retrieveFailedEventQueueNotificationsFromConcurAsString();

}
