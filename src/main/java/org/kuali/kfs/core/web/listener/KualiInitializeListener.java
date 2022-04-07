/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.core.web.listener;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.impl.config.property.Config;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;
import java.util.Properties;

/**
 * A ServletContextListener responsible for initializing the a Kuali Financials application.
 */
/*
 * CU Customization: Backport the FINP-7916 changes into the 2021-01-28 version of this file.
 * This overlay should be removed when we upgrade to the 2021-11-17 financials patch or later.
 */
public class KualiInitializeListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DEFAULT_SPRING_BEANS_REPLACEMENT_VALUE = "${bootstrap.spring.file}";
    private static final String WEB_BOOTSTRAP_SPRING_FILE = "web.bootstrap.spring.file";

    private XmlWebApplicationContext context;

    /**
     * ServletContextListener interface implementation that schedules the start of the lifecycle
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long startInit = System.currentTimeMillis();
        LOG.info("Initializing Kuali Financials Application...");

        // Stop Quartz from "phoning home" on every startup
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");

        String bootstrapSpringBeans = "";
        if (StringUtils.isNotBlank(System.getProperty(WEB_BOOTSTRAP_SPRING_FILE))) {
            bootstrapSpringBeans = System.getProperty(WEB_BOOTSTRAP_SPRING_FILE);
        } else if (StringUtils.isNotBlank(sce.getServletContext().getInitParameter(WEB_BOOTSTRAP_SPRING_FILE))) {
            String bootstrapSpringInitParam = sce.getServletContext().getInitParameter(WEB_BOOTSTRAP_SPRING_FILE);
            // if the value comes through as ${bootstrap.spring.beans}, we ignore it
            if (!DEFAULT_SPRING_BEANS_REPLACEMENT_VALUE.equals(bootstrapSpringInitParam)) {
                bootstrapSpringBeans = bootstrapSpringInitParam;
                LOG.info("Found bootstrap Spring Beans file defined in servlet context: " + bootstrapSpringBeans);
            }
        }

        Properties baseProps = new Properties();
        baseProps.putAll(getContextParameters(sce.getServletContext()));
        baseProps.putAll(System.getProperties());
        Config config = new Config(baseProps);
        ConfigContext.init(config);

        context = new XmlWebApplicationContext();
        if (StringUtils.isNotEmpty(bootstrapSpringBeans)) {
            context.setConfigLocation(bootstrapSpringBeans);
        }
        context.setServletContext(sce.getServletContext());

        try {
            context.refresh();
        } catch (RuntimeException e) {
            LOG.error("problem during context.refresh()", e);

            throw e;
        }

        context.start();
        long endInit = System.currentTimeMillis();
        LOG.info(
                "...Kuali Financials Application successfully initialized, startup took " + (endInit - startInit) +
                        " ms.");
    }

    /**
     * Translates context parameters from the web.xml into entries in a Properties file.
     */
    protected Properties getContextParameters(ServletContext context) {
        Properties properties = new Properties();
        Enumeration<String> paramNames = context.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            properties.put(paramName, context.getInitParameter(paramName));
        }
        return properties;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down Kuali Financials...");
        if (context != null) {
            context.close();
        }
        LOG.info("...completed shutdown of Kuali Financials.");
        LogManager.shutdown();
    }

    public XmlWebApplicationContext getContext() {
        return context;
    }

}
