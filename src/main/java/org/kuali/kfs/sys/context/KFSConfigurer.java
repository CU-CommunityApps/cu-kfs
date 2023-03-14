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
import org.apache.struts.mock.MockServletContext;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.lifecycle.BaseCompositeLifecycle;
import org.kuali.kfs.core.api.lifecycle.BaseLifecycle;
import org.kuali.kfs.core.api.lifecycle.Lifecycle;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.core.framework.lifecycle.ServiceDelegatingLifecycle;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.ksb.messaging.MessageFetcher;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * CU customization: this is still needed for our local customization of CuSchedulerServiceImpl
 * to allow certain processes to run without Quartz.
 * 
 * In addition, we have backported FINP-8867 to fix a potential batch startup problem.
 */
public class KFSConfigurer extends BaseCompositeLifecycle implements DisposableBean, InitializingBean,
        ServletContextAware, SmartApplicationListener {

    private static final Logger LOG = LogManager.getLogger();
    /* This is under the webapp context (i.e. .../fin/<SPRING_MVC_ROOT_PATH>) */
    private static final String SPRING_MVC_ROOT_PATH = "/api";
    private static final String SCHEDULED_THREAD_POOL_SERVICE = "rice.ksb.scheduledThreadPool";
  

    private final List<Lifecycle> internalLifecycles;
    private ServletContext servletContext;

    private boolean testMode;

    public KFSConfigurer() {
        LOG.info("KFSConfigurer instantiated");
        internalLifecycles = new ArrayList<>();
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        validateConfigurerState();
        // CU Customization: Backport ensureServletContext() call from FINP-8867 fix
        ensureServletContext();
        initializeResourceLoaders();
        start();
        enableSpringMvcForRestApis();
    }

    // CU Customization: Backport FINP-8867 fix
    private void ensureServletContext() {
        if (servletContext == null) {
            // For unit/integration tests or standalone utilities (e.g. BatchStepRunner, WorkFlowImporter, etc.), there
            // is no ServletContext, since it is not running as a webapp. However, we need one to have the
            // DispatcherServlet and for some beans that are auto-magically created to support Spring MVC annotated
            // classes.
            servletContext = new KfsMockServletContext();
        }
    }

    /*
     * Ideally, in a non-Boot Spring world, the DispatcherServlet would be configured via an entry in web.xml, pointing
     * the Servlet to the Spring XML config file(s) and allowing it to construct the ApplicationContext. However, due
     * to the fact KFS is a) using Struts 1.x in a non-idiomatic manner & b) constructing Spring's ApplicationContext
     * in a non-idiomatic manner, the web.xml approach is not possible at this time. That being the case, the
     * DispatcherServlet is being configured here programmatically, after KFS has already constructed Spring's
     * ApplicationContext.
     *
     * This REST API path will be completely separate from any other KFS paths currently in use; though still under
     * the webapp context (i.e. /fin).
     */
    private void enableSpringMvcForRestApis() {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setApplicationContext(GlobalResourceLoader.getContext());
        final ServletRegistration.Dynamic servletRegistration =
                servletContext.addServlet("SpringMvcDispatcherServlet", dispatcherServlet);
        servletRegistration.addMapping(SPRING_MVC_ROOT_PATH + "/*");
    }

    @Override
    public final void destroy() throws Exception {
        stop();
        GlobalResourceLoader.stop();
    }

    @Override
    public final void start() throws Exception {
        super.start();
        doAdditionalModuleStartLogic();
    }

    private static void doAdditionalModuleStartLogic() {
        LOG.info("*********************************************************");
        LOG.info("KFS Starting Module");
        LOG.info("*********************************************************");
    }

    @Override
    public final void stop() throws Exception {
        try {
            doAdditionalModuleStopLogic();
        } finally {
            super.stop();
        }
    }

    private void doAdditionalModuleStopLogic() {
        LOG.info("*********************************************************");
        LOG.info("KFS Stopping Module");
        LOG.info("*********************************************************");

        for (int index = internalLifecycles.size() - 1; index >= 0; index--) {
            try {
                internalLifecycles.get(index).stop();
            } catch (final Exception e) {
                LOG.error("Failed to properly execute shutdown logic.", e);
            }
        }
    }

    private List<String> getPrimarySpringFiles() {
        String files = ConfigContext.getCurrentContextConfig().getProperty("spring.source.files");
        if (testMode) {
            files = files + "," + ConfigContext.getCurrentContextConfig().getProperty("spring.test.files");
        }
        LOG.info("KFS Spring Files Requested.  Returning: {}", files);
        return files == null ? Collections.emptyList() : parseFileList(files);
    }

    private static List<String> parseFileList(final String files) {
        final List<String> parsedFiles = new ArrayList<>();
        for (final String file : files.split(",")) {
            final String trimmedFile = file.trim();
            if (!trimmedFile.isEmpty()) {
                parsedFiles.add(trimmedFile);
            }
        }

        return parsedFiles;
    }

    private void initializeResourceLoaders() throws Exception {
        GlobalResourceLoader.initialize(servletContext, getPrimarySpringFiles());
        GlobalResourceLoader.start();
    }

    @Override
    public List<Lifecycle> loadLifecycles() {
        final List<Lifecycle> lifecycles = new LinkedList<>();

        // this validation of our service list needs to happen after we've loaded our configs so it's a lifecycle
        lifecycles.add(new BaseLifecycle() {
            @Override
            public void start() throws Exception {
                super.start();
            }
        });

        return lifecycles;
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            loadDataDictionary();
            doAdditionalContextStartedLogic();
        }
    }

    /**
     * Used to "poke" the Data Dictionary again after the Spring Context is initialized.
     */
    private static void loadDataDictionary() {
        LOG.info("KRAD Configurer - Loading DD");
        final DataDictionaryService dds =
                GlobalResourceLoader.getService(KRADServiceLocatorWeb.DATA_DICTIONARY_SERVICE);
        dds.parseDataDictionaryConfigurationFiles();

        LOG.info("KRAD Configurer - Validating DD");
        //TODO: Fix via config when we can.
        dds.validateDD();

        dds.performBeanOverrides();
    }

    private void doAdditionalContextStartedLogic() {
        final Lifecycle threadPool = new ServiceDelegatingLifecycle(KSBServiceLocator.THREAD_POOL_SERVICE);
        Lifecycle scheduledThreadPool =
                new ServiceDelegatingLifecycle(SCHEDULED_THREAD_POOL_SERVICE);

        try {
            threadPool.start();
            internalLifecycles.add(threadPool);
            scheduledThreadPool.start();
            internalLifecycles.add(scheduledThreadPool);
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to initialize KSB on context startup");
        }

        requeueMessages();
    }

    private static void requeueMessages() {
        // automatically requeue documents sitting with status of 'R'
        final MessageFetcher messageFetcher = new MessageFetcher(null);
        KSBServiceLocator.getThreadPool().execute(messageFetcher);
    }

    @Override
    public boolean supportsEventType(final Class<? extends ApplicationEvent> eventType) {
        return true;
    }

    @Override
    public boolean supportsSourceType(final Class<?> sourceType) {
        return true;
    }

    @Override
    public int getOrder() {
        // return a lower value which will give the data dictionary indexing higher precedence since DD indexing should
        // be started as soon as it can be
        return -1000;
    }

    private static void validateConfigurerState() {
        if (!ConfigContext.isInitialized()) {
            throw new ConfigurationException(
                    "ConfigContext has not yet been initialized, please initialize prior to using.");
        }
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setTestMode(final boolean testMode) {
        this.testMode = testMode;
    }

    // CU Customization: Backport FINP-8867 fix
    /**
     * This allows tests and standalone utilities (e.g. BatchStepRunner, WorkflowImporter, etc.) to run.
     *
     * This is a stop-gap measure. This entire class needs to cease to exist -- that'll be part of the Spring
     * modernization work.
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
