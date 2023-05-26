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
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.kfs.coreservice.api.component.ComponentService;
import org.kuali.kfs.coreservice.impl.component.DerivedComponent;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.bo.Step;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.ksb.messaging.MessageFetcher;
import org.kuali.kfs.ksb.messaging.threadpool.KSBScheduledThreadPoolExecutor;
import org.kuali.kfs.ksb.messaging.threadpool.KSBThreadPool;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.DispatcherServlet;

import edu.cornell.kfs.sys.CUKFSConstants;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//CU customization: this is still needed for our local customization of CuSchedulerServiceImpl to allow certain processes to run without Quartz
public class KFSConfigurer implements DisposableBean, InitializingBean, ServletContextAware, SmartApplicationListener {

    private static final Logger LOG = LogManager.getLogger();
    /* This is under the webapp context (i.e. .../fin/<SPRING_MVC_ROOT_PATH>) */
    private static final String SPRING_MVC_ROOT_PATH = "/api";
    // CU customization
    private static final String SCHEDULED_THREAD_POOL_SERVICE = "rice.ksb.scheduledThreadPool";

    private KSBThreadPool ksbThreadPool;
    // CU customization
    private KSBScheduledThreadPoolExecutor scheduledThreadPool;
    private ServletContext servletContext;

    private boolean testMode;

    public KFSConfigurer() {
        LOG.info("KFSConfigurer instantiated");
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        validateConfigurerState();
        ensureServletContext();
        initializeResourceLoaders();
        doAdditionalModuleStartLogic();
        enableSpringMvcForRestApis();
    }

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
        doAdditionalModuleStopLogic();
        GlobalResourceLoader.stop();
    }

    private static void doAdditionalModuleStartLogic() {
        LOG.info("*********************************************************");
        LOG.info("KFS Starting Module");
        LOG.info("*********************************************************");
    }

    private void doAdditionalModuleStopLogic() {
        LOG.info("*********************************************************");
        LOG.info("KFS Stopping Module");
        LOG.info("*********************************************************");

        try {
            ksbThreadPool.stop();
            //CU customization
            scheduledThreadPool.stop();
        } catch (final Exception e) {
            LOG.error("Failed to properly execute shutdown logic.", e);
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

    // These ApplicationEventS are for the startup ApplicationContext (AC), not the main AC; however, most of/all the
    // methods herein deal with the main AC.
    @Override
    public void onApplicationEvent(final ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            loadDataDictionary();
            doAdditionalContextStartedLogic();

            final ApplicationContext mainApplicationContext = GlobalResourceLoader.getContext();

            // Publish Step classes as Components. These are not in the
            // DD so are not published by
            // DataDictionaryComponentPublisherService.publishAllComponents().
            publishBatchStepComponents(mainApplicationContext);
            initDirectories(mainApplicationContext);
            updateWorkflow(mainApplicationContext);
            initScheduler(mainApplicationContext);
        }

        if (applicationEvent instanceof ContextClosedEvent) {
            final ApplicationContext mainApplicationContext = GlobalResourceLoader.getContext();
            try {
                if (mainApplicationContext.getBean(Scheduler.class) != null) {
                    if (mainApplicationContext.getBean(Scheduler.class).isStarted()) {
                        LOG.info("Shutting Down scheduler");
                        mainApplicationContext.getBean(Scheduler.class).shutdown();
                    }
                }
            } catch (SchedulerException ex) {
                LOG.error("Exception while shutting down the scheduler", ex);
            }
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

        KRADServiceLocatorInternal.getDataDictionaryComponentPublisherService()
                .publishAllComponents();
    }

    private void doAdditionalContextStartedLogic() {
        ksbThreadPool = KSBServiceLocator.getThreadPool();
        // CU customization
        scheduledThreadPool = (KSBScheduledThreadPoolExecutor)KSBServiceLocator.getService(SCHEDULED_THREAD_POOL_SERVICE);

        try {
            ksbThreadPool.start();
            // CU customization
            scheduledThreadPool.start();
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

    private static void publishBatchStepComponents(final ApplicationContext applicationContext) {
        final String kfsBatchStepComponentSetId = "STEP:KFS";

        final KualiModuleService kualiModuleService = applicationContext.getBean(KualiModuleService.class);

        final Map<String, Step> steps = applicationContext.getBeansOfType(Step.class);
        final List<DerivedComponent> stepComponents = new ArrayList<>(steps.size());
        for (final Step step : steps.values()) {
            final Step unproxiedStep = (Step) ProxyUtils.getTargetIfProxied(step);
            LOG.info("Building component for step: {}({})", unproxiedStep::getName, unproxiedStep::getClass);

            final ModuleService responsibleModuleService =
                    kualiModuleService.getResponsibleModuleService(unproxiedStep.getClass());

            final String namespaceCode = responsibleModuleService == null
                    ? KFSConstants.CoreModuleNamespaces.KFS
                    : responsibleModuleService.getModuleConfiguration().getNamespaceCode();

            final String simpleName = unproxiedStep.getClass().getSimpleName();

            final DerivedComponent derivedComponent = new DerivedComponent();
            derivedComponent.setNamespaceCode(namespaceCode);
            derivedComponent.setCode(simpleName);
            derivedComponent.setName(simpleName);
            derivedComponent.setComponentSetId(kfsBatchStepComponentSetId);

            stepComponents.add(derivedComponent);
        }

        LOG.info("Requesting to publish {} derived components ", stepComponents::size);
        final ComponentService componentService =
                applicationContext.getBean(CoreServiceApiServiceLocator.COMPONENT_SERVICE, ComponentService.class);
        componentService.publishDerivedComponents(kfsBatchStepComponentSetId, stepComponents);
    }

    private static void initDirectories(final ApplicationContext applicationContext) {
        final ConfigurationService configurationService =
                applicationContext.getBean(KRADServiceLocator.KUALI_CONFIGURATION_SERVICE, ConfigurationService.class);

        final String directoriesToCreate =
                configurationService.getPropertyValueAsString(KFSPropertyConstants.DIRECTORIES_TO_CREATE_PATH);
        for (final String path : directoriesToCreate.split(",")) {
            final String trimmedPath = path.trim();
            if (trimmedPath.isEmpty()) {
                continue;
            }

            final File potentialDirectory = new File(trimmedPath);
            if (potentialDirectory.exists()) {
                if (!potentialDirectory.isDirectory()) {
                    throw new RuntimeException(trimmedPath + " exists but is not a directory");
                }
            } else {
                if (!potentialDirectory.mkdirs()) {
                    throw new RuntimeException(trimmedPath + " does not exist and unable to create it");
                } else {
                    LOG.info("Created directory: {}", potentialDirectory);
                }
            }
        }
    }

    private static void updateWorkflow(final ApplicationContext applicationContext) {
        final ConfigurationService configurationService =
                applicationContext.getBean(KRADServiceLocator.KUALI_CONFIGURATION_SERVICE, ConfigurationService.class);
        if (configurationService.getPropertyValueAsBoolean(KFSPropertyConstants.UPDATE_WORKFLOW_ON_STARTUP)) {
            new WorkflowImporter().importWorkflow(applicationContext);
        }
    }

    private static void initScheduler(final ApplicationContext applicationContext) {
        final ConfigurationService configurationService =
                applicationContext.getBean(KRADServiceLocator.KUALI_CONFIGURATION_SERVICE, ConfigurationService.class);
        if (configurationService.getPropertyValueAsBoolean(KFSPropertyConstants.USE_QUARTZ_SCHEDULING_KEY)) {
            try {
                LOG.info("Attempting to initialize the SchedulerService");
                applicationContext.getBean(SchedulerService.class).initialize();
                LOG.info("Starting the Quartz scheduler");
                applicationContext.getBean(Scheduler.class).start();
            } catch (NoSuchBeanDefinitionException e) {
                LOG.warn("Not initializing the scheduler because there is no scheduler bean");
            } catch (Exception ex) {
                LOG.error("Caught Exception while starting the scheduler", ex);
            }
        }
        
        // CU customization
        if (shouldAllowLocalBatchExecution()) {
            if (isQuartzSchedulingEnabled()) {
                throw new IllegalStateException("CU-specific local batch execution and Quartz scheduling "
                        + "cannot both be enabled");
            }
            // Base code skips this initialize() call if Quartz is disabled, so we add it again in this block.
            SpringContext.getBean(SchedulerService.class).initialize();
        }
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

    // CU customization
    private static boolean shouldAllowLocalBatchExecution() {
        return getBooleanProperty(CUKFSConstants.CU_ALLOW_LOCAL_BATCH_EXECUTION_KEY);
    }

    private static boolean isQuartzSchedulingEnabled() {
        return getBooleanProperty(KFSPropertyConstants.USE_QUARTZ_SCHEDULING_KEY);
    }

    private static boolean getBooleanProperty(String propertyName) {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(propertyName);
    }
}
