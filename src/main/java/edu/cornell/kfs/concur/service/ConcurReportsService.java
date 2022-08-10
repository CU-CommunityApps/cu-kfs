package edu.cornell.kfs.concur.service;

import edu.cornell.kfs.concur.businessobjects.ConcurReport;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.eventnotification.rest.xmlObjects.ConcurEventNotificationListDTO;

public interface ConcurReportsService {
    
    void initializeTemporaryAccessToken();
    
    void clearTemporaryAccessToken();
    
    ConcurReport extractConcurReport(String reportURI);

    void updateExpenseReportStatusInConcur(String workflowURI, ValidationResult validationResult);
    
    boolean deleteFailedEventQueueItemInConcur(String noticationId);
    
    ConcurEventNotificationListDTO retrieveFailedEventQueueNotificationsFromConcur();
    
    String retrieveFailedEventQueueNotificationsFromConcurAsString();

}
