package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Collection;

import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ExpenseDetailedReportDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;

public class ConcurEventNotificationProcessingServiceImpl implements ConcurEventNotificationProcessingService {
    
    protected ConcurEventNotificationService concurEventNotificationService;
    protected ConcurAccountValidationService concurAccountValidationService;

    @Override
    public void processConcurEventNotifications() {
        Collection<ConcurEventNotification> concurEventNotifications = concurEventNotificationService.retrieveConcurEventNotificationsForProcessing();

        if (ObjectUtils.isNotNull(concurEventNotifications)) {
            for (ConcurEventNotification concurEventNotification : concurEventNotifications) {
                ExpenseDetailedReportDTO expenseDetailedReport = retrieveExpenseDetailedReportFromConcur(concurEventNotification);
                ConcurAccountInfo concurAccountInfo = extractAccountInfo(expenseDetailedReport);
                concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
                updateExpenseReportStatusInConcur();
            }
        }
    }

    @Override
    public ExpenseDetailedReportDTO retrieveExpenseDetailedReportFromConcur(ConcurEventNotification concurEventNotification) {
        return null;
    }

    @Override
    public ConcurAccountInfo extractAccountInfo(ExpenseDetailedReportDTO expenseDetailedReport) {
        return null;
    }
    
    @Override
    public void updateExpenseReportStatusInConcur() {
    }

    public ConcurAccountValidationService getConcurAccountValidationService() {
        return concurAccountValidationService;
    }

    public void setConcurAccountValidationService(ConcurAccountValidationService concurAccountValidationService) {
        this.concurAccountValidationService = concurAccountValidationService;
    }

    public ConcurEventNotificationService getConcurEventNotificationService() {
        return concurEventNotificationService;
    }

    public void setConcurEventNotificationService(ConcurEventNotificationService concurEventNotificationService) {
        this.concurEventNotificationService = concurEventNotificationService;
    }

}
