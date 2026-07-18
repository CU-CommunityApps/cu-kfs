package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.krad.service.KRADServiceLocator;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public abstract class CemiOrmDaoOjbImplBase extends PlatformAwareDaoBaseOjb {
    
    // These two methods were added to reduce processing time for local development during CEMI project work.
    protected boolean shouldUseLessDataDuringCemiDevelopment() {
        return getBooleanProperty(CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }
    
    protected boolean getBooleanProperty(String propertyName) {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(propertyName);
    }

}