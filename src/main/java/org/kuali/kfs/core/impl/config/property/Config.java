/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.core.impl.config.property;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.util.ImmutableProperties;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Config class that maintains an internal copy of all properties in their "raw" form (without any nested properties
 * resolved). This allows properties to be added in stages and still alter values of properties previously read in.
 * It can also return system properties as defaults when a property has not been defined.
 */
// CU customization: add back methods that were removed in base code
public class Config {

    private static final Logger LOG = LogManager.getLogger();

    private static final String PLACEHOLDER_REGEX = "\\$\\{([^{}]+)\\}";

    private final Properties rawProperties = new Properties();
    private final Properties resolvedProperties = new Properties();

    // compile pattern for regex once
    private final Pattern pattern = Pattern.compile(PLACEHOLDER_REGEX);

    public Config() {
    }

    public Config(final Properties properties) {
        putProperties(properties);
    }

    public Properties getProperties() {
        return new ImmutableProperties(resolvedProperties);
    }

    public String getProperty(final String key) {
        return resolvedProperties.getProperty(key);
    }

    /**
     * This overrides the property.
     */
    public void putProperty(
            final String key,
            final String value
    ) {
        setProperty(key, replaceVariable(key, value));
        resolveRawToCache();
    }

    public void putProperties(final Properties properties) {
        // Nothing to do
        if (properties == null) {
            return;
        }

        // Cycle through the keys, using Rice's convention for expanding variables as we go
        replaceVariables(properties);

        // Still need to resolve placeholders in addition to expanding variables
        resolveRawToCache();
    }

    /**
     * Expand variables and invoke this.setProperty() for each property in the properties object
     * passed in
     */
    private void replaceVariables(final Properties properties) {
        final SortedSet<String> keys = new TreeSet<>(properties.stringPropertyNames());
        for (final String key : keys) {
            final String originalValue = properties.getProperty(key);
            final String replacedValue = replaceVariable(key, originalValue);
            logPropertyChange("", key, null, originalValue, replacedValue);
            setProperty("", key, replacedValue);
        }
    }

    protected static String flatten(final String s) {
        if (s == null) {
            return null;
        } else {
            return s.replace("\n", " ").replace("\r", " ");
        }
    }

    /**
     * This will set the property. No logic checking so what you pass in gets set. We use this as a
     * focal point for debugging the raw config changes.
     */
    protected void setProperty(
            final String name,
            final String value
    ) {
        setProperty("", name, value);
    }

    protected void setProperty(
            final String prefix,
            final String name,
            final String value
    ) {
        final String oldValue = rawProperties.getProperty(name);
        final String msg = prefix == null ? "Raw Config Override: " : prefix + "Raw Config Override: ";
        logPropertyChange(msg, name, null, oldValue, value);
        rawProperties.setProperty(name, value);
    }

    protected String resolve(final String key) {
        return resolve(key, null);
    }

    /**
     * This method will determine the value for a property by looking it up in the raw properties. If the property
     * value contains a nested property (foo=${nested}) it will start the recursion by calling parseValue(). It will
     * also check for a system property of the same name and 'default to' the system property if not found in the
     * raw properties. This method only determines the resolved value, it does not modify the properties in the
     * resolved or raw properties objects.
     *
     * @param key    they key of the property for which to determine the value
     * @param keySet contains all keys used so far in this recursion. used to check for circular references.
     * @return the value of the property corresponding to the key parameter
     */
    protected String resolve(
            final String key,
            Set<String> keySet
    ) {
        // check if we have already resolved this key and have circular reference
        if (keySet != null && keySet.contains(key)) {
            throw new ConfigurationException("Circular reference in config: " + key);
        }

        String value = rawProperties.getProperty(key);

        if (value == null && System.getProperties().containsKey(key)) {
            value = System.getProperty(key);
        }

        if (value != null && value.contains("${")) {
            if (keySet == null) {
                keySet = new HashSet<>();
            }
            keySet.add(key);

            value = parseValue(value, keySet);

            keySet.remove(key);
        }

        if (value == null) {
            value = "";
            LOG.debug("Property key: '{}' is not available and hence set to empty", key);
        }

        return value;
    }

    /**
     * This method parses the value string to find all nested properties (foo=${nested}) and replaces them with the
     * value returned from calling resolve(). It does this in a new string and does not modify the raw or resolved
     * properties objects.
     *
     * @param value  the string to search for nest properties
     * @param keySet contains all keys used so far in this recursion. used to check for circular references.
     * @return the value with nested properties replaced
     */
    private String parseValue(
            final String value,
            final Set<String> keySet
    ) {
        String result = value;

        Matcher matcher = pattern.matcher(value);

        while (matcher.find()) {
            // get the first, outermost ${} in the string. removes the ${} as well.
            final String key = matcher.group(1);

            final String resolved = resolve(key, keySet);

            result = matcher.replaceFirst(Matcher.quoteReplacement(resolved));
            matcher = matcher.reset(result);
        }

        return result;
    }

    /**
     * This method is used when reading in new properties to check if there is a direct reference to the key in the
     * value. This emulates operating system environment variable setting behavior and replaces the reference in the
     * value with the current value of the property from the rawProperties.
     *
     * <pre>
     * ex:
     * path=/usr/bin;${someVar}
     * path=${path};/some/other/path
     *
     * resolves to:
     * path=/usr/bin;${someVar};/some/other/path
     * </pre>
     * <p>
     * It does not resolve the value from rawProperties as it could contain nested properties that might change later.
     * If the property does not exist in the rawProperties it will check for a default system property now to prevent a
     * circular reference error.
     *
     * @param name  the property name
     * @param value the value to check for nested property of the same name
     * @return the resolved value
     */
    String replaceVariable(
            final String name,
            final String value
    ) {
        final String regex = "(?:\\$\\{" + name + "\\})";
        String temporary;

        // Look for a property in the map first and use that. If system override is true then it will get overridden
        // during the resolve phase. If the value is null we need to check the system now so we don't throw an error.
        if (value.contains("${" + name + "}")) {
            temporary = rawProperties.getProperty(name);
            if (temporary == null) {
                temporary = System.getProperty(name);
            }

            if (temporary != null) {
                return value.replaceAll(regex, Matcher.quoteReplacement(temporary));
            }
        }

        return value;
    }

    /**
     * This method iterates through the raw properties and stores their resolved values in the resolved properties
     * map, which acts as a cache so we don't have to run the recursion every time getProperty() is called.
     */
    void resolveRawToCache() {
        // Make sure we have something to do
        if (rawProperties.isEmpty()) {
            return;
        }

        // Store the existing resolved properties in another object
        final Properties oldProps = new Properties(new ImmutableProperties(resolvedProperties));

        // Clear the resolved properties object
        resolvedProperties.clear();

        // Setup sorted property keys
        final SortedSet<String> keys = new TreeSet<>(rawProperties.stringPropertyNames());

        // Cycle through the properties resolving values as we go
        for (final String key : keys) {

            // Fully resolve the value for this key
            final String newValue = resolve(key);

            // Extract the old value for this key
            final String oldValue = oldProps.getProperty(key);

            // Extract the raw value for this key
            final String rawValue = rawProperties.getProperty(key);

            // Log what happened (if anything) in terms of an existing property being overridden
            logPropertyChange("Resolved Config Override: ", key, rawValue, oldValue, newValue);

            // Store the fully resolved property value
            resolvedProperties.setProperty(key, newValue);
        }
    }

    private static void logPropertyChange(
            final String msg,
            final String key,
            final String rawValue,
            final String oldValue,
            final String newValue
    ) {
        // If INFO level logging is not enabled, we are done
        if (!LOG.isInfoEnabled()) {
            return;
        }

        // There was no previous value, we are done
        if (oldValue == null) {
            return;
        }

        // There was a previous value, but it's the same as the new value, we are done
        if (StringUtils.equals(oldValue, newValue)) {
            return;
        }

        // Create some log friendly strings
        final String displayOld = flatten(ConfigLogger.getDisplaySafeValue(key, oldValue));
        final String displayNew = flatten(ConfigLogger.getDisplaySafeValue(key, newValue));
        final String displayRaw = flatten(rawValue);

        // Log what happened to this property value
        if (StringUtils.contains(rawValue, "$")) {
            LOG.info("{}{}({})=[{}]->[{}]", msg, key, displayRaw, displayOld, displayNew);
        } else {
            LOG.info("{}{}=[{}]->[{}]", msg, key, displayOld, displayNew);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(resolvedProperties);
    }

    /* CU customization */
    public boolean getBooleanProperty(
            final String key,
            final boolean defaultValue
    ) {
        return Truth.strToBooleanIgnoreCase(getProperty(key), defaultValue);
    }

    /* CU customization */
    public Boolean getBooleanProperty(final String key) {
        return Truth.strToBooleanIgnoreCase(getProperty(key));
    }

}
