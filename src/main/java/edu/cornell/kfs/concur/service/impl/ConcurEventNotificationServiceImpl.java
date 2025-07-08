package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;

public class ConcurEventNotificationServiceImpl implements ConcurEventNotificationService {
	private static final Logger LOG = LogManager.getLogger(ConcurEventNotificationServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;

    @Override
    public void saveConcurEventNotification(ConcurEventNotification concurEventNotification) {
        businessObjectService.save(concurEventNotification);
    }

    protected String truncateValidationResultMessageToMaximumDatabaseFieldSize(String validationResultMessages) {
        return StringUtils.left(validationResultMessages, ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH);
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

}
