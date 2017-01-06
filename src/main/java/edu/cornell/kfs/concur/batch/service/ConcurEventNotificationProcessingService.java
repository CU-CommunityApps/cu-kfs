package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ExpenseDetailedReportDTO;

public interface ConcurEventNotificationProcessingService {
    
    void processConcurEventNotifications();
    
    ExpenseDetailedReportDTO retrieveExpenseDetailedReportFromConcur(ConcurEventNotification concurEventNotification);
    
    ConcurAccountInfo extractAccountInfo(ExpenseDetailedReportDTO expenseDetailedReport);
    
    void updateExpenseReportStatusInConcur();

}
