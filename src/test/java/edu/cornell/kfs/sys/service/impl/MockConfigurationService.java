package edu.cornell.kfs.sys.service.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

public class MockConfigurationService implements ConfigurationService {

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
        if(ErrorMessageUtilsServiceTestConstants.ERROR_TEST_KEY.equalsIgnoreCase(key)){
            return ErrorMessageUtilsServiceTestConstants.ERROR_TEST_MESSAGE;
        }
        if(StringUtils.isBlank(key)){
            return "";
        }
       
        return null;
    }
}
