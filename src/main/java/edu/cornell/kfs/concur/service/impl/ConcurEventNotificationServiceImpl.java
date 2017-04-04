package edu.cornell.kfs.concur.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;

public class ConcurEventNotificationServiceImpl implements ConcurEventNotificationService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurEventNotificationServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;

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
        concurEventNotification.setValidationResultMessage(validationResultMessages);
        saveConcurEventNotification(concurEventNotification);
    }
    
    @Override
    public void retrieveAndPersistFailedEventQueueReports() {
        
    }


    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
