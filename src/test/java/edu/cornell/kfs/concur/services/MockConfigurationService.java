package edu.cornell.kfs.concur.services;

import java.util.Map;

import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

public class MockConfigurationService implements ConfigurationService {
    public static final String ERROR_REQUIRED_MESSAGE ="{0} is a required field.";
    public static final String ERROR_EXISTENCE_MESSAGE ="The specified {0} does not exist.";
    public static final String ERROR_INACTIVE_MESSAGE = "The specified {0} is inactive.";

    @Override
    public Map<String, String> getAllProperties() {
        return null;
    }

    @Override
    public boolean getPropertyValueAsBoolean(String key) {
        
        return false;
    }
    @Override
    public boolean getPropertyValueAsBoolean(String key, boolean defaultValue) {      
        return false;
    }

    @Override
    public String getPropertyValueAsString(String key) {
        if(KFSKeyConstants.ERROR_REQUIRED.equalsIgnoreCase(key)){
            return ERROR_REQUIRED_MESSAGE;
        }
        if(KFSKeyConstants.ERROR_EXISTENCE.equalsIgnoreCase(key)){
            return ERROR_EXISTENCE_MESSAGE;
        }
        if(KFSKeyConstants.ERROR_INACTIVE.equalsIgnoreCase(key)){
            return ERROR_INACTIVE_MESSAGE;
        }
        return null;
    }

}
