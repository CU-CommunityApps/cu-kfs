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
package org.kuali.kfs.sys.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.impl.config.property.Config;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * CU Customization: Backported the FINP-8690 integration test fixes from the 2022-07-20 financials patch.
 * This overlay can be removed when we upgrade to the 2022-07-20 patch.
 * 
 * Equivalent to {@link KFSInitializeListener} but for integration tests.
 */
public final class KFSTestStartup {
    private static final Logger LOG = LogManager.getLogger();

    private static ConfigurableWebApplicationContext context;

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private KFSTestStartup() {
    }

    public static void initializeKfsTestContext() {
        long startInit = System.currentTimeMillis();

        LOG.info("Updating relational database.");
        SpringContext.updateDatabase();

        LOG.info("Initializing Kuali Financials Application...");

        String bootstrapSpringBeans = "classpath:kfs-startup-test.xml";

        Properties baseProps = new Properties();
        baseProps.putAll(System.getProperties());
        Config config = new Config(baseProps);
        ConfigContext.init(config);

        context = new XmlWebApplicationContext();
        context.setConfigLocation(bootstrapSpringBeans);
        context.setServletContext(new KfsMockServletContext());
        try {
            context.refresh();
        } catch (RuntimeException e) {
            LOG.error("problem during context.refresh()", e);

            throw e;
        }

        context.start();
        long endInit = System.currentTimeMillis();
        LOG.info("...Kuali Financials Application successfully initialized, startup took {} ms.", endInit - startInit);

        SpringContext.finishInitializationAfterStartup();
    }

    /**
     * Integration tests need to be able to set a {@link javax.servlet.ServletContext} on the
     * {@link org.springframework.web.context.WebApplicationContext} and that {@code ServletContext} must not blow up
     * when {@code addServlet(...)} is called. That's the whole point of this inner-class...and it's inner-class.
     *
     * This is a stop-gap measure; this will not allow the Spring MVC Controllers to have integration tests. This entire
     * class needs to cease to exist -- that'll be part of the Spring modernization work.
     */
    private static final class KfsMockServletContext extends MockServletContext {

        @Override
        public ServletRegistration.Dynamic addServlet(final String servletName, final Servlet servlet) {
            return new KfsServletRegistrationDynamic();
        }

        private static final class KfsServletRegistrationDynamic implements ServletRegistration.Dynamic {

            @Override
            public void setLoadOnStartup(final int loadOnStartup) {
            }

            @Override
            public Set<String> setServletSecurity(final ServletSecurityElement constraint) {
                return Set.of();
            }

            @Override
            public void setMultipartConfig(final MultipartConfigElement multipartConfig) {
            }

            @Override
            public void setRunAsRole(final String roleName) {
            }

            @Override
            public void setAsyncSupported(final boolean isAsyncSupported) {
            }

            @Override
            public Set<String> addMapping(final String... urlPatterns) {
                return Set.of();
            }

            @Override
            public Collection<String> getMappings() {
                return Set.of();
            }

            @Override
            public String getRunAsRole() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getClassName() {
                return null;
            }

            @Override
            public boolean setInitParameter(final String name, final String value) {
                return false;
            }

            @Override
            public String getInitParameter(final String name) {
                return null;
            }

            @Override
            public Set<String> setInitParameters(final Map<String, String> initParameters) {
                return Set.of();
            }

            @Override
            public Map<String, String> getInitParameters() {
                return Map.of();
            }
        }
    }

}
