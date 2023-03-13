/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.core.framework.resourceloader;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.lifecycle.BaseLifecycle;
import org.kuali.kfs.core.api.reflect.ObjectDefinition;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * CU Customization: Backported the FINP-8690 integration test fixes from the 2022-07-20 financials patch.
 * This overlay can be removed when we upgrade to the 2022-07-20 patch.
 * 
 * A simple resource loader which wraps a Spring {@link ConfigurableApplicationContext}.
 * <p>
 * Starts and stops the {@link ConfigurableApplicationContext}.
 */
public class SpringResourceLoader extends BaseLifecycle {

    private static final Logger LOG = LogManager.getLogger();

    private ConfigurableWebApplicationContext context;
    private final ServletContext servletContext;
    private final List<String> fileLocs;

    public SpringResourceLoader(
            final List<String> fileLocs,
            final ServletContext servletContext
    ) {
        this.fileLocs = fileLocs;
        this.servletContext = servletContext;
    }

    /** Constructs an instance of the Object using the given ObjectDefinition classname. */
    public static <T> T getObject(final ObjectDefinition objectDefinition) {
        return ObjectDefinitionResolver.createObject(
                objectDefinition,
                ClassLoaderUtils.getDefaultClassLoader()
        );
    }

    /** Fetches the service with the given name. */
    public <T> T getService(final QName serviceName) {
        if (!isStarted()) {
            return null;
        }

        if (context.containsBean(serviceName.toString())) {
            return (T) context.getBean(serviceName.toString());
        }
        return null;
    }

    @Override
    public void start() throws Exception {
        if (!isStarted()) {
            LOG.info("Creating Spring context {}", () -> StringUtils.join(fileLocs, ","));

            context = new XmlWebApplicationContext();
            context.setServletContext(servletContext);
            context.setConfigLocations(fileLocs.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            context.refresh();

            super.start();
        }
    }

    @Override
    public void stop() throws Exception {
        if (context != null) {
            LOG.info("Stopping Spring context {}", () -> StringUtils.join(fileLocs, ","));
            context.close();
        }
        super.stop();
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }

    public String getContents() {
        final StringBuilder contents = new StringBuilder();
        for (final String beanName : context.getBeanDefinitionNames()) {
            contents.append("  +++")
                    .append(beanName)
                    .append(System.lineSeparator());
        }
        return contents.toString();
    }

}
