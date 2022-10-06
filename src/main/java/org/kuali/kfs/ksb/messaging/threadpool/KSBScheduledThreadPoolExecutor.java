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
import org.kuali.kfs.ksb.util.KSBConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/* CU customization: this is still needed for our local customization of CuSchedulerServiceImpl to allow certain processes to run without Quartz*/
public class KSBScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements KSBScheduledPool {

    private static final Logger LOG = LogManager.getLogger();

    private boolean started;
    private static final int DEFAULT_SIZE = 2;

    public KSBScheduledThreadPoolExecutor() {
        super(DEFAULT_SIZE, new KSBThreadFactory());
    }

    public boolean isStarted() {
        return started;
    }

    public void start() throws Exception {
        LOG.info("Starting the KSB scheduled thread pool...");
        try {
            Integer size = new Integer(
                    ConfigContext.getCurrentContextConfig().getProperty(CUKFSConstants.Config.FIXED_POOL_SIZE));
            this.setCorePoolSize(size);
        } catch (NumberFormatException nfe) {
            // ignore this, instead the pool will be set to DEFAULT_SIZE
        }
        LOG.info("...KSB scheduled thread pool successfully started.");
    }

    public void stop() throws Exception {
        LOG.info("Stopping the KSB scheduled thread pool...");
        try {
            int pendingTasks = this.shutdownNow().size();
            LOG.info(pendingTasks + " pending tasks...");
            LOG.info("awaiting termination: " + this.awaitTermination(20, TimeUnit.SECONDS));
            LOG.info("...KSB scheduled thread pool successfully stopped, isShutdown=" + this.isShutdown() +
                    ", isTerminated=" + this.isTerminated());
        } catch (Exception e) {
            LOG.warn("Exception thrown shutting down " + KSBScheduledThreadPoolExecutor.class.getSimpleName(), e);
        }

    }

    private static class KSBThreadFactory implements ThreadFactory {

        private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable runnable) {
            Thread thread = defaultThreadFactory.newThread(runnable);
            thread.setName("KSB-Scheduled-" + thread.getName());
            return thread;
        }
    }

}
