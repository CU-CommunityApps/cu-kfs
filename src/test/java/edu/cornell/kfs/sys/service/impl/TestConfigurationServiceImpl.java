package edu.cornell.kfs.sys.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.Truth;

/**
 * Unit-test-specific implementation of ConfigurationService that reads its properties
 * from the specified Properties instance.
 */
public class TestConfigurationServiceImpl implements ConfigurationService {

    private Properties properties;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Map<String, String> getAllProperties() {
        return (Map) Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean getPropertyValueAsBoolean(final String key, final boolean defaultValue) {
        final String stringValue = getPropertyValueAsString(key);
        return Truth.strToBooleanIgnoreCase(stringValue, defaultValue);
    }

    @Override
    public boolean getPropertyValueAsBoolean(final String key) {
        return getPropertyValueAsBoolean(key, false);
    }

    @Override
    public String getPropertyValueAsString(final String key) {
        return properties.getProperty(key);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

}
