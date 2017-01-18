package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigurationService;

public class ErrorMessageUtilsServiceImpl {
    protected ConfigurationService configurationService;
    
    public String createErrorString(String errorKey, String... params) {

        String errorString = configurationService.getPropertyValueAsString(errorKey);
        if (StringUtils.isEmpty(errorString)) {
            final StringBuilder s = new StringBuilder(errorKey).append(':');
            if (params != null) {
                for (String p : params) {
                    if (p != null) {
                        s.append(p);
                        s.append(';');
                    }
                }
            }
            errorString = s.toString();
        } else {
            errorString = MessageFormat.format(errorString, params);
        }
        return errorString;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
