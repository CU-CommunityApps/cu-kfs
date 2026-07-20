package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public abstract class CemiOrmDaoOjbImplBase extends PlatformAwareDaoBaseOjb {
    
    private ConfigurationService configurationService;
    
    // These two methods were added to reduce processing time for local development during CEMI project work.
    protected boolean shouldUseLessDataDuringCemiDevelopment() {
        return getBooleanProperty(CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }
    
    protected boolean getBooleanProperty(String propertyName) {
        return configurationService.getPropertyValueAsBoolean(propertyName);
    }
    
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
