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
 * CU Customization: Fixed a bug where core pool size was being increased prior to increasing max pool size.
 * 
 * A Thread Pool implementation which implements a thread pool backed by a configuration store.
 */
public class KSBThreadPoolImpl extends ThreadPoolExecutor implements KSBThreadPool {

    private static final Logger LOG = LogManager.getLogger();

    private static final int DEFAULT_POOL_SIZE = 5;

    private boolean started;
    private boolean poolSizeSet;

    public KSBThreadPoolImpl() {
        super(
                DEFAULT_POOL_SIZE,
                DEFAULT_POOL_SIZE,
                60L,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(1, new PriorityBlockingQueuePersistedMessageComparator()),
                new KSBThreadFactory(ClassLoaderUtils.getDefaultClassLoader()),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Override
    public long getKeepAliveTime() {
        return getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        LOG.info("Starting the KSB thread pool...");
        loadSettings();
        started = true;
        LOG.info("...KSB thread pool successfully started.");
    }

    @Override
    public void stop() throws InterruptedException {
        if (started) {
            LOG.info("Shutting down KSB thread pool...");
            final int pendingTasks = shutdownNow().size();
            LOG.info("{} pending tasks...", pendingTasks);
            final boolean terminated = awaitTermination(20L, TimeUnit.SECONDS);
            LOG.info("awaiting termination: {}", terminated);
            LOG.info(
                    "...KSB thread pool successfully stopped, isShutdown={}, isTerminated={}",
                    this::isShutdown,
                    this::isTerminated
            );
            started = false;
            LOG.info("...KSB thread pool successfully shut down.");
        }
    }

    private void loadSettings() {
        final String threadPoolSizeStr = ConfigContext.getCurrentContextConfig().getProperty(Config.THREAD_POOL_SIZE);
        final String threadPoolMaxSizeStr =
                ConfigContext.getCurrentContextConfig().getProperty(Config.THREAD_POOL_MAX_SIZE);
        if (!poolSizeSet) {
            final int poolSize = parsePoolSizeProperty(threadPoolSizeStr, Config.THREAD_POOL_SIZE);
            final int maxPoolSize = parsePoolSizeProperty(threadPoolMaxSizeStr, Config.THREAD_POOL_MAX_SIZE);
            if (poolSize <= maxPoolSize && poolSize > 0) {
                // CU Customization: Reversed setter order; we need to set max size BEFORE core size to avoid errors.
                LOG.info("Setting max pool size to {} threads.", maxPoolSize);
                setMaximumPoolSize(maxPoolSize);
                LOG.info("Setting core pool size to {} threads.", poolSize);
                setCorePoolSize(poolSize);
                poolSizeSet = true;
            } else {
                LOG.error("loadSettings(): Invalid pool size value(s). Pool size ({}) must be <=  max pool size ({}) "
                          + "and > 0.", threadPoolSizeStr, threadPoolMaxSizeStr);
            }
        }
    }

    private static int parsePoolSizeProperty(final String threadPoolSizeStr, final String propertyName) {
        int poolSize = DEFAULT_POOL_SIZE;
        try {
            poolSize = Integer.parseInt(threadPoolSizeStr);
        } catch (final NumberFormatException nfe) {
            LOG.error("loadSettings(): Unable to parse the {}: '{}'", propertyName, threadPoolSizeStr);
        }
        return poolSize;
    }

    @Override
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
        private static int factorySequence;

        private static int threadSequence;

        private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        private final ClassLoader contextClassLoader;

        KSBThreadFactory(final ClassLoader contextClassLoader) {
            this.contextClassLoader = contextClassLoader;
            factorySequence++;
        }

        @Override
        public Thread newThread(final Runnable r) {
            threadSequence++;
            final Thread thread = defaultThreadFactory.newThread(r);
            // if the thread ends up getting spawned by an action inside a workflow plugin or something along
            // those lines, it will inherit the plugin's classloader as it's ContextClassLoader.  Let's make sure
            // it's set to the same ClassLoader that loaded the KFSConfigurer (formerly KSBConfigurer)
            thread.setContextClassLoader(contextClassLoader);
            thread.setName("KSB-pool-" + factorySequence + "-thread-" + threadSequence);
            return thread;
        }
    }
}
