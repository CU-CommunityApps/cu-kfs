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
package org.kuali.kfs.ksb.messaging.threadpool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.kuali.kfs.core.impl.config.property.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A Thread Pool implementation for the KSB which implements a thread pool backed by a configuration store.
 */
public class KSBThreadPoolImpl extends ThreadPoolExecutor implements KSBThreadPool {

    private static final Logger LOG = LogManager.getLogger();

    public static final int DEFAULT_POOL_SIZE = 5;

    private boolean started;
    private boolean poolSizeSet;

    public KSBThreadPoolImpl() {
        super(DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE, 60, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(1, new PriorityBlockingQueuePersistedMessageComparator()),
                new KSBThreadFactory(
                        ClassLoaderUtils.getDefaultClassLoader()), new ThreadPoolExecutor.AbortPolicy());
    }

    public void setCorePoolSize(int corePoolSize) {
        LOG.info("Setting core pool size to " + corePoolSize + " threads.");
        super.setCorePoolSize(corePoolSize);
        this.poolSizeSet = true;
    }

    public long getKeepAliveTime() {
        return super.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    public boolean isStarted() {
        return this.started;
    }

    public void start() {
        LOG.info("Starting the KSB thread pool...");
        loadSettings();
        this.started = true;
        LOG.info("...KSB thread pool successfully started.");
    }

    public void stop() throws Exception {
        if (isStarted()) {
            LOG.info("Shutting down KSB thread pool...");
            int pendingTasks = this.shutdownNow().size();
            LOG.info(pendingTasks + " pending tasks...");
            LOG.info("awaiting termination: " + this.awaitTermination(20, TimeUnit.SECONDS));
            LOG.info("...KSB thread pool successfully stopped, isShutdown=" + this.isShutdown() + ", isTerminated=" +
                    this.isTerminated());
            this.started = false;
            LOG.info("...KSB thread pool successfully shut down.");
        }
    }

    /**
     * Loads the thread pool settings from the DAO.
     */
    protected void loadSettings() {
        String threadPoolSizeStr = ConfigContext.getCurrentContextConfig().getProperty(Config.THREAD_POOL_SIZE);
        if (!this.poolSizeSet) {
            int poolSize = DEFAULT_POOL_SIZE;
            try {
                poolSize = Integer.parseInt(threadPoolSizeStr);
            } catch (NumberFormatException nfe) {
                LOG.error("loadSettings(): Unable to parse the pool size: '" + threadPoolSizeStr + "'");
            }
            // CU Customization: If necessary, update the maximum pool size before updating the core pool size,
            // to prevent an exception from the parent ThreadPoolExecutor in Java 11.
            if (poolSize > getMaximumPoolSize()) {
                setMaximumPoolSize(poolSize);
            }
            setCorePoolSize(poolSize);
        }
    }

    public Object getInstance() {
        return this;
    }

    /**
     * A simple ThreadFactory which names the thread as follows:<br>
     * <br>
     *
     * KSB-pool-<i>m</i>-thread-<i>n</i><br>
     * <br>
     * <p>
     * Where <i>m</i> is the sequence number of the factory and <i>n</i> is the sequence number of the thread within
     * the factory.
     */
    private static class KSBThreadFactory implements ThreadFactory {
        private static int factorySequence = 0;

        private static int threadSequence = 0;

        private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        private final ClassLoader contextClassLoader;

        KSBThreadFactory(ClassLoader contextClassLoader) {
            this.contextClassLoader = contextClassLoader;
            factorySequence++;
        }

        public Thread newThread(Runnable runnable) {
            threadSequence++;
            Thread thread = this.defaultThreadFactory.newThread(runnable);
            // if the thread ends up getting spawned by an action inside of a workflow plugin or something along
            // those lines, it will inherit the plugin's classloader as it's ContextClassLoader.  Let's make sure
            // it's set to the same ClassLoader that loaded the KFSConfigurer (formerly KSBConfigurer)
            thread.setContextClassLoader(contextClassLoader);
            thread.setName("KSB-pool-" + factorySequence + "-thread-" + threadSequence);
            return thread;
        }
    }
}
