package edu.cornell.kfs.concur.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.service.ConcurEventNotificationConversionService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;

public class ConcurEventNotificationServiceImpl implements ConcurEventNotificationService {
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

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
