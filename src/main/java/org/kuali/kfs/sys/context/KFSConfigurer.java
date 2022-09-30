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
package org.kuali.kfs.sys.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.api.config.module.Configurer;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.lifecycle.BaseCompositeLifecycle;
import org.kuali.kfs.core.api.lifecycle.BaseLifecycle;
import org.kuali.kfs.core.api.lifecycle.Lifecycle;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.core.api.resourceloader.ResourceLoader;
import org.kuali.kfs.core.api.resourceloader.ResourceLoaderContainer;
import org.kuali.kfs.core.framework.lifecycle.ServiceDelegatingLifecycle;
import org.kuali.kfs.core.framework.resourceloader.BaseResourceLoader;
import org.kuali.kfs.core.framework.resourceloader.ResourceLoaderFactory;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.ksb.messaging.MessageFetcher;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/* CU customization: this is still needed for our local customization of CuSchedulerServiceImpl to allow certain processes to run without Quartz*/
public class KFSConfigurer extends BaseCompositeLifecycle implements Configurer, DisposableBean, InitializingBean,
        ServletContextAware, SmartApplicationListener {

    private static final Logger LOG = LogManager.getLogger();
    private static final String DEFAULT_ROOT_RESOURCE_LOADER_NAME = "RootResourceLoader";
    private static final String ROOT_RESOURCE_LOADER_CONTAINER_NAME = "RootResourceLoaderContainer";
    private static final String SCHEDULED_THREAD_POOL_SERVICE = "rice.ksb.scheduledThreadPool";

    private DataSource dataSource;
    private DataSource nonTransactionalDataSource;
    private UserTransaction userTransaction;
    private TransactionManager transactionManager;
    private final List<Lifecycle> internalLifecycles;
    private ServletContext servletContext;

    private boolean testMode = false;

    public KFSConfigurer() {
        LOG.info("KFSConfigurer instantiated");
        this.internalLifecycles = new ArrayList<>();
    }

    @Override
    public final void addToConfig() {
        configureJta();
        configureDataSource();
    }

    /**
     * If the user injected JTA classes into this configurer, verify that both the UserTransaction and
     * TransactionManager are set and then attach them to the configuration.
     */
    private void configureJta() {
        if (this.userTransaction != null) {
            ConfigContext.getCurrentContextConfig()
                    .putObject(KFSConstants.USER_TRANSACTION_OBJ, this.userTransaction);
        }
        if (this.transactionManager != null) {
            ConfigContext.getCurrentContextConfig()
                    .putObject(KFSConstants.TRANSACTION_MANAGER_OBJ, this.transactionManager);
        }
        boolean userTransactionConfigured = this.userTransaction != null;
        boolean transactionManagerConfigured = this.transactionManager != null;
        if (userTransactionConfigured && !transactionManagerConfigured) {
            throw new ConfigurationException("When configuring JTA, both a UserTransaction and a TransactionManager " +
                    "are required. Only the UserTransaction was configured.");
        }
        if (transactionManagerConfigured && !userTransactionConfigured) {
            throw new ConfigurationException("When configuring JTA, both a UserTransaction and a TransactionManager " +
                    "are required. Only the TransactionManager was configured.");
        }
    }

    private void configureDataSource() {
        if (dataSource != null) {
            ConfigContext.getCurrentContextConfig().putObject(KFSConstants.DATASOURCE_OBJ, this.dataSource);
        }
        if (nonTransactionalDataSource != null) {
            ConfigContext.getCurrentContextConfig()
                    .putObject(KFSConstants.NON_TRANSACTIONAL_DATASOURCE_OBJ, this.nonTransactionalDataSource);
        }
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        validateConfigurerState();
        addToConfig();
        initializeResourceLoaders();
        start();
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

    private void doAdditionalModuleStartLogic() {
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
            } catch (Exception e) {
                LOG.error("Failed to properly execute shutdown logic.", e);
            }
        }
    }

    @Override
    public List<String> getPrimarySpringFiles() {
        String files = ConfigContext.getCurrentContextConfig().getProperty("spring.source.files");
        if (testMode) {
            files = files + "," + ConfigContext.getCurrentContextConfig().getProperty("spring.test.files");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("KFS Spring Files Requested.  Returning: " + files);
        }
        return files == null ? Collections.emptyList() : parseFileList(files);
    }

    private List<String> parseFileList(String files) {
        final List<String> parsedFiles = new ArrayList<>();
        for (String file : files.split(",")) {
            final String trimmedFile = file.trim();
            if (!trimmedFile.isEmpty()) {
                parsedFiles.add(trimmedFile);
            }
        }

        return parsedFiles;
    }

    @Override
    public final void initializeResourceLoaders() throws Exception {
        List<String> files = new ArrayList<>(getPrimarySpringFiles());

        ResourceLoader rootResourceLoader = GlobalResourceLoader.getResourceLoader();
        if (rootResourceLoader == null) {
            createRootResourceLoader();
        }

        if (!files.isEmpty()) {
            ResourceLoader rl = createResourceLoader(servletContext, files);
            rl.start();
            GlobalResourceLoader.addResourceLoader(rl);
        }
    }

    private void createRootResourceLoader() throws Exception {
        final ResourceLoaderContainer container = new ResourceLoaderContainer(
                new QName(KFSConstants.APPLICATION_NAMESPACE_CODE, ROOT_RESOURCE_LOADER_CONTAINER_NAME));
        ResourceLoader rootResourceLoader = new BaseResourceLoader(
                new QName(KFSConstants.APPLICATION_NAMESPACE_CODE, DEFAULT_ROOT_RESOURCE_LOADER_NAME));

        container.addResourceLoader(rootResourceLoader);
        GlobalResourceLoader.addResourceLoader(container);
        GlobalResourceLoader.start();
    }

    private ResourceLoader createResourceLoader(ServletContext servletContext, List<String> files) {
        return ResourceLoaderFactory.createRootRiceResourceLoader(servletContext, files, "KFS");
    }

    @Override
    public List<Lifecycle> loadLifecycles() {
        List<Lifecycle> lifecycles = new LinkedList<>();

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
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            loadDataDictionary();
            doAdditionalContextStartedLogic();
        }
    }

    /**
     * Used to "poke" the Data Dictionary again after the Spring Context is initialized.
     */
    private void loadDataDictionary() {
        LOG.info("KRAD Configurer - Loading DD");
        DataDictionaryService dds = KNSServiceLocator.getDataDictionaryService();
        if (dds == null) {
            dds = GlobalResourceLoader.getService(KRADServiceLocatorWeb.DATA_DICTIONARY_SERVICE);
        }
        dds.parseDataDictionaryConfigurationFiles(false);

        LOG.info("KRAD Configurer - Validating DD");
        //TODO: Fix via config when we can.
        dds.validateDD();

        dds.performBeanOverrides();
    }

    private void doAdditionalContextStartedLogic() {
        Lifecycle threadPool = new ServiceDelegatingLifecycle(KSBServiceLocator.THREAD_POOL_SERVICE);
        Lifecycle scheduledThreadPool =
                new ServiceDelegatingLifecycle(SCHEDULED_THREAD_POOL_SERVICE);

        try {
            threadPool.start();
            internalLifecycles.add(threadPool);
            scheduledThreadPool.start();
            internalLifecycles.add(scheduledThreadPool);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to initialize KSB on context startup");
        }

        requeueMessages();
    }

    private void requeueMessages() {
        // automatically requeue documents sitting with status of 'R'
        MessageFetcher messageFetcher = new MessageFetcher((Integer) null);
        KSBServiceLocator.getThreadPool().execute(messageFetcher);
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return true;
    }

    @Override
    public boolean supportsSourceType(Class<?> aClass) {
        return true;
    }

    @Override
    public int getOrder() {
        // return a lower value which will give the data dictionary indexing higher precedence since DD indexing should
        // be started as soon as it can be
        return -1000;
    }

    @Override
    public final void validateConfigurerState() {
        if (!ConfigContext.isInitialized()) {
            throw new ConfigurationException(
                    "ConfigContext has not yet been initialized, please initialize prior to using.");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setNonTransactionalDataSource(DataSource nonTransactionalDataSource) {
        this.nonTransactionalDataSource = nonTransactionalDataSource;
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public void setUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

}
