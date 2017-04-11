package edu.cornell.kfs.concur.service.impl;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationListDTO;
import edu.cornell.kfs.concur.service.ConcurEventNotificationConversionService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;
import edu.cornell.kfs.concur.service.ConcurReportsService;

public class ConcurEventNotificationServiceImpl implements ConcurEventNotificationService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurEventNotificationServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;
    protected ConcurReportsService concurReportsService;
    protected ConcurEventNotificationConversionService concurEventNotificationConversionService;

    @Override
    public void saveConcurEventNotification(ConcurEventNotification concurEventNotification) {
        businessObjectService.save(concurEventNotification);
    }
    
    @Override
    public Collection<ConcurEventNotification> retrieveConcurEventNotificationsForProcessing() {
        Collection<ConcurEventNotification> concurEventNotifications = null;
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(ConcurPropertyConstants.ConcurEventNotification.IN_PROCESS, KRADConstants.NO_INDICATOR_VALUE);
        fieldValues.put(ConcurPropertyConstants.ConcurEventNotification.PROCESSED, KRADConstants.NO_INDICATOR_VALUE);

        concurEventNotifications = businessObjectService.findMatching(ConcurEventNotification.class, fieldValues);

        return concurEventNotifications;
    }
    
    @Override
    public void updateConcurEventNotificationFlagsAndValidationMessage(ConcurEventNotification concurEventNotification, boolean inProcess, boolean processed, boolean validationResult, String validationResultMessages) {
        LOG.info("Update ConcurEventNotification flags and validationMessage in KFS database: " + validationResult + "," + validationResultMessages);
        concurEventNotification.setInProcess(inProcess);
        concurEventNotification.setProcessed(processed);
        concurEventNotification.setValidationResult(validationResult);
        concurEventNotification.setValidationResultMessage(truncateValidationResultMessageToMaximumDatabaseFieldSize(validationResultMessages));
        saveConcurEventNotification(concurEventNotification);
    }
    
    protected String truncateValidationResultMessageToMaximumDatabaseFieldSize(String validationResultMessages) {
        if (validationResultMessages != null && validationResultMessages.length() > ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH) {
            validationResultMessages = validationResultMessages.substring(0, ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH);
        }
        return validationResultMessages;
    }
    
    @Override
    public void retrieveAndPersistFailedEventQueueReports() {
        ConcurEventNotificationListDTO notificationList = getConcurReportsService().retrieveFailedEventQueueNotificationsFromConcur();
        if (areThereNotificationsToProcess(notificationList)) {
            LOG.info("retrieveAndPersistFailedEventQueueReports, found " + notificationList.getConcurEventNotificationDTOs().size() + " failed events to process.");
            for (ConcurEventNotificationDTO concurEventNotificationDTO : notificationList.getConcurEventNotificationDTOs()) {
                try {
                    ConcurEventNotification concurEventNotification = getConcurEventNotificationConversionService().convertConcurEventNotification(concurEventNotificationDTO);
                    saveConcurEventNotification(concurEventNotification);
                    getConcurReportsService().deleteFailedEventQueueItemInConcur(findNotificationId(concurEventNotification.getNotificationURI()));
                } catch (ParseException e) {
                    LOG.error("validate():" + e.getMessage(), e);
                }
            }
        } else {
            LOG.info("retrieveAndPersistFailedEventQueueReports, There were no failed events to process.");
        }
    }

    private boolean areThereNotificationsToProcess(ConcurEventNotificationListDTO notificationList) {
        return notificationList != null && 
                notificationList.getConcurEventNotificationDTOs() != null && 
                notificationList.getConcurEventNotificationDTOs().size() > 0;
    }
    
    protected String findNotificationId(String notificationUri) {
        String[] uriElements = StringUtils.split(notificationUri, "/");
        int lastIndex = uriElements.length - 1;
        return uriElements[lastIndex];
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ConcurReportsService getConcurReportsService() {
        return concurReportsService;
    }

    public void setConcurReportsService(ConcurReportsService concurReportsService) {
        this.concurReportsService = concurReportsService;
    }

    public ConcurEventNotificationConversionService getConcurEventNotificationConversionService() {
        return concurEventNotificationConversionService;
    }

    public void setConcurEventNotificationConversionService(
            ConcurEventNotificationConversionService concurEventNotificationConversionService) {
        this.concurEventNotificationConversionService = concurEventNotificationConversionService;
    }

}
