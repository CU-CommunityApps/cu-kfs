package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.businessobjects.ConcurReport;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;
import edu.cornell.kfs.concur.service.ConcurReportsService;

public class ConcurEventNotificationProcessingServiceImpl implements ConcurEventNotificationProcessingService {

	private static final Logger LOG = LogManager.getLogger(ConcurEventNotificationProcessingServiceImpl.class);
    protected ConcurEventNotificationService concurEventNotificationService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConcurReportsService concurReportsService;
    protected ConfigurationService configurationService;

    @Override
    public void processConcurEventNotifications() {
        concurEventNotificationService.retrieveAndPersistFailedEventQueueReports();
        Collection<ConcurEventNotification> concurEventNotifications = concurEventNotificationService.retrieveConcurEventNotificationsForProcessing();
        if (ObjectUtils.isNotNull(concurEventNotifications)) {
            for (ConcurEventNotification concurEventNotification : concurEventNotifications) {
               processConcurEventNotification(concurEventNotification);
            }
        }
    }
    
    protected void processConcurEventNotification(ConcurEventNotification concurEventNotification) {
        LOG.info("processConcurEventNotification() start");
        try {
            ValidationResult validationResult = new ValidationResult();
            ConcurReport concurReport = concurReportsService.extractConcurReport(concurEventNotification.getObjectURI());

            if (concurReport != null) {
                LOG.info("Concur report status code from Concur: report ID: " + concurReport.getReportID() + ", status code: " + concurReport.getConcurStatusCode() + ", workflow URI: " + concurReport.getWorkflowURI());
                
                if (ConcurUtils.isConcurReportStatusAwaitingExternalValidation(concurReport.getConcurStatusCode())) {
                    validationResult = validateReportAccountInfo(concurReport);
                    concurReportsService.updateExpenseReportStatusInConcur(concurReport.getWorkflowURI(), validationResult);
                    }
                else{
                    LOG.info("Concur Report not in Awaiting External Validation status");
                    validationResult.addErrorMessage(configurationService.getPropertyValueAsString(ConcurKeyConstants.INCORRECT_CONCUR_STATUS_CODE));
                }               
                concurEventNotificationService.updateConcurEventNotificationFlagsAndValidationMessage(concurEventNotification, ConcurConstants.EVENT_NOTIFICATION_NOT_IN_PROCESS, ConcurConstants.EVENT_NOTIFICATION_PROCESSED, validationResult.isValid(), validationResult.getErrorMessagesAsOneFormattedString());           
            }
        } catch (Exception e) {
            LOG.error("An exception occured while processing this request: id " + concurEventNotification.getConcurEventNotificationId() + ", object URI" + concurEventNotification.getObjectURI() + ", error: " + e.getMessage(), e);
        }
    }
    
    private ValidationResult validateReportAccountInfo(ConcurReport concurReport){
        ValidationResult reportValidationResult = new ValidationResult();
        LOG.info("validateReportAccountInfo()");
        if (concurReport.getAccountInfos() != null && concurReport.getAccountInfos().size() > 0) {            
            for (ConcurAccountInfo concurAccountInfo : concurReport.getAccountInfos()) {
                LOG.info(concurAccountInfo.toString());
                if(concurAccountInfo.isForPersonalCorporateCardExpense()){
                    reportValidationResult.add(concurAccountValidationService.validateConcurAccountInfoObjectCodeNotRequired(concurAccountInfo));
                }
                else {
                    reportValidationResult.add(concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo));    
                }
            }
        }
        LOG.info("Validation Result: " + reportValidationResult.isValid() + ", validation messages: " + reportValidationResult.getErrorMessagesAsOneFormattedString());
        return reportValidationResult;
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

    public ConcurReportsService getConcurReportsService() {
        return concurReportsService;
    }

    public void setConcurReportsService(ConcurReportsService concurReportsService) {
        this.concurReportsService = concurReportsService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
