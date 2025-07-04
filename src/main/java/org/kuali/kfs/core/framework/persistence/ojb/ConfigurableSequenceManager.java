/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.core.framework.persistence.ojb;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.JdbcAccess;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.SequenceDescriptor;
import org.apache.ojb.broker.util.sequence.AbstractSequenceManager;
import org.apache.ojb.broker.util.sequence.SequenceManager;
import org.apache.ojb.broker.util.sequence.SequenceManagerException;
import org.apache.ojb.broker.util.sequence.SequenceManagerNextValImpl;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;

import java.util.Iterator;
import java.util.Properties;

/**
 * A sequence manager implementation which can be configured at runtime via the KEW
 * Configuration API.
 */
@Deprecated

// Cornell customization: KualiCo removed this class with FINP-10106 and is now using a MySql specific class. 
// We copied this in our cu base code to continue using it.
public class ConfigurableSequenceManager implements SequenceManager {

    private static final String PROPERTY_PREFIX_ATTRIBUTE = "property.prefix";
    private static final String DEFAULT_PROPERTY_PREFIX = "datasource.ojb.sequenceManager";
    private static final String DEFAULT_SEQUENCE_MANAGER_CLASSNAME = SequenceManagerNextValImpl.class.getName();

    private final PersistenceBroker broker;
    private final SequenceManager sequenceManager;

    public ConfigurableSequenceManager(final PersistenceBroker broker) {
        this.broker = broker;
        sequenceManager = createSequenceManager(broker);
    }

    protected SequenceManager createSequenceManager(final PersistenceBroker broker) {
        final String propertyPrefix = getPropertyPrefix();
        String sequenceManagerClassName = ConfigContext.getCurrentContextConfig()
                .getProperty(getSequenceManagerClassNameProperty(propertyPrefix));
        if (StringUtils.isBlank(sequenceManagerClassName)) {
            sequenceManagerClassName = DEFAULT_SEQUENCE_MANAGER_CLASSNAME;
        }
        try {
            final Class sequenceManagerClass = ClassLoaderUtils.getDefaultClassLoader().loadClass(sequenceManagerClassName);
            final Object sequenceManagerObject = ConstructorUtils.invokeConstructor(sequenceManagerClass, broker);
            if (!(sequenceManagerObject instanceof SequenceManager)) {
                throw new ConfigurationException("The configured sequence manager ('" + sequenceManagerClassName +
                        "') is not an instance of '" + SequenceManager.class.getName() + "'");
            }
            final SequenceManager sequenceManager = (SequenceManager) sequenceManagerObject;
            if (sequenceManager instanceof AbstractSequenceManager) {
                ((AbstractSequenceManager) sequenceManager)
                        .setConfigurationProperties(getSequenceManagerConfigProperties(propertyPrefix));
            }
            return sequenceManager;
        } catch (final ClassNotFoundException e) {
            throw new ConfigurationException(
                    "Could not locate sequence manager with the given class '" + sequenceManagerClassName + "'");
        } catch (final Exception e) {
            throw new ConfigurationException(
                    "Property loading sequence manager class '" + sequenceManagerClassName + "'", e);
        }
    }

    protected String getSequenceManagerClassNameProperty(final String propertyPrefix) {
        return propertyPrefix + ".className";
    }

    protected SequenceManager getConfiguredSequenceManager() {
        return sequenceManager;
    }

    protected Properties getSequenceManagerConfigProperties(final String propertyPrefix) {
        final Properties sequenceManagerProperties = new Properties();
        final Properties properties = ConfigContext.getCurrentContextConfig().getProperties();
        final String attributePrefix = propertyPrefix + ".attribute.";
        for (final Iterator iterator = properties.keySet().iterator(); iterator.hasNext(); ) {
            final String key = (String) iterator.next();
            if (key.startsWith(attributePrefix)) {
                final String value = properties.getProperty(key);
                final String attributeName = key.substring(attributePrefix.length());
                sequenceManagerProperties.setProperty(attributeName, value);
            }
        }
        return sequenceManagerProperties;
    }

    @Override
    public void afterStore(final JdbcAccess jdbcAccess, final ClassDescriptor classDescriptor, final Object object)
            throws SequenceManagerException {
        getConfiguredSequenceManager().afterStore(jdbcAccess, classDescriptor, object);
    }

    @Override
    public Object getUniqueValue(final FieldDescriptor fieldDescriptor) throws SequenceManagerException {
        return getConfiguredSequenceManager().getUniqueValue(fieldDescriptor);
    }

    public PersistenceBroker getBroker() {
        return broker;
    }

    public String getPropertyPrefix() {
        final SequenceDescriptor sd =
                getBroker().serviceConnectionManager().getConnectionDescriptor().getSequenceDescriptor();
        String propertyPrefix = null;
        if (sd != null) {
            propertyPrefix = sd.getConfigurationProperties().getProperty(PROPERTY_PREFIX_ATTRIBUTE);
        }
        if (StringUtils.isBlank(propertyPrefix)) {
            propertyPrefix = DEFAULT_PROPERTY_PREFIX;
        }
        return propertyPrefix;
    }
}