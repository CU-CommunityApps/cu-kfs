package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.businessobjects.ConcurReport;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;
import edu.cornell.kfs.concur.service.ConcurReportsService;

public class ConcurEventNotificationProcessingServiceImpl implements ConcurEventNotificationProcessingService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurEventNotificationProcessingServiceImpl.class);
    protected ConcurEventNotificationService concurEventNotificationService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConcurReportsService concurReportsService;
    protected ConfigurationService configurationService;

    @Override
    public void processConcurEventNotifications() {
        Collection<ConcurEventNotification> concurEventNotifications = concurEventNotificationService.retrieveConcurEventNotificationsForProcessing();
        if (ObjectUtils.isNotNull(concurEventNotifications)) {
            for (ConcurEventNotification concurEventNotification : concurEventNotifications) {
               processConcurEventNotification(concurEventNotification);
            }
        }
    }
    
    protected void processConcurEventNotification(ConcurEventNotification concurEventNotification){
        try {
            ValidationResult validationResult = new ValidationResult(false, new ArrayList<String>());

            LOG.info("Extract concur report with objectURI: " + concurEventNotification.getObjectURI());

            ConcurReport concurReport = concurReportsService.extractConcurReport(concurEventNotification.getObjectURI());
            if (concurReport != null) {
                if (concurReport.getAccountInfos() != null) {
                    LOG.info("Validate account info: " + KFSConstants.NEWLINE);
                    for (ConcurAccountInfo concurAccountInfo : concurReport.getAccountInfos()) {
                        LOG.info(concurAccountInfo.toString());
                        validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
                        LOG.info("Validation Result: " + validationResult.isValid() + ", validation messages: " + validationResult.getErrorMessagesAsOneFormattedString());
                    }
                } else {
                    LOG.info("No account info present.");
                    validationResult.addMessage(configurationService.getPropertyValueAsString(ConcurKeyConstants.CONCUR_ACCOUNT_INFO_IS_REQUIRED));
                }
                
                LOG.info("Update report status in Concur");
                concurReportsService.updateExpenseReportStatusInConcur(concurReport.getWorkflowURI(), validationResult);  
                LOG.info("Update ConcurEventNotification flags and validationMessage in KFS database");
                concurEventNotificationService.updateConcurEventNotificationFlagsAndValidationMessage(concurEventNotification, false, true, validationResult.isValid(), validationResult.getErrorMessagesAsOneFormattedString());
            }
        } catch (Exception e) {
            LOG.error("An exception occured while processing this request: id " + concurEventNotification.getConcurEventNotificationId() + ", object URI" + concurEventNotification.getObjectURI() + ", error: " + e.getMessage(), e);
        }
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
