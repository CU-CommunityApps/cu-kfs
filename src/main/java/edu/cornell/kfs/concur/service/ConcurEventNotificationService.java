package edu.cornell.kfs.concur.service;

import java.util.Collection;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;

public interface ConcurEventNotificationService {
    
    void saveConcurEventNotification(ConcurEventNotification concurEventNotification);
    
    Collection<ConcurEventNotification> retrieveConcurEventNotificationsForProcessing();
    
    void updateConcurEventNotificationFlagsAndValidationMessage(ConcurEventNotification concurEventNotification, boolean inProcess, boolean processed, boolean validationResult, String validationResultMessages);
    
}
