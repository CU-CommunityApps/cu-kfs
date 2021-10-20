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
package org.kuali.kfs.ksb.messaging.exceptionhandling;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.messaging.exceptionhandling.DocumentMessageExceptionHandler;
import org.kuali.kfs.ksb.api.messaging.AsynchronousCall;
import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJob;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJobListener;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 * ====
 * CU Customization:
 * Overlayed this class to include an intermediate portion of the FINP-7647 changes from the 2021-09-30 release.
 * This overlay should be removed once we upgrade to the 2021-09-30 release or later.
 * ====
 * 
 * Default implementation of {@link ExceptionRoutingService}.  Just saves the message in the queue as is, which
 * should be marked Exception by the {@link MessageExceptionHandler}.
 */
public class DefaultExceptionServiceImpl implements ExceptionRoutingService {

    private static final Logger LOG = LogManager.getLogger();
    private Scheduler scheduler;

    public void placeInExceptionRouting(Throwable throwable, PersistedMessage message, Object service) throws
            Exception {
        LOG.error("Exception caught processing message " + message.getRouteQueueId() + " " +
                message.getServiceName() + ": " + throwable);

        AsynchronousCall methodCall = null;
        if (message.getMethodCall() != null) {
            methodCall = message.getMethodCall();
        } else {
            methodCall = message.getPayload().getMethodCall();
        }
        message.setMethodCall(methodCall);
        MessageExceptionHandler exceptionHandler = new DocumentMessageExceptionHandler();
        exceptionHandler.handleException(throwable, message, service);
    }

    public void placeInExceptionRoutingLastDitchEffort(Throwable throwable, PersistedMessage message,
            Object service) throws Exception {
        LOG.error("Exception caught processing message " + message.getRouteQueueId() + " " +
                message.getServiceName() + ": " + throwable);

        AsynchronousCall methodCall = null;
        if (message.getMethodCall() != null) {
            methodCall = message.getMethodCall();
        } else {
            methodCall = message.getPayload().getMethodCall();
        }
        message.setMethodCall(methodCall);
        MessageExceptionHandler exceptionHandler = new DocumentMessageExceptionHandler();
        exceptionHandler.handleExceptionLastDitchEffort(throwable, message, service);
    }

    public void scheduleExecution(Throwable throwable, PersistedMessage message, String description) throws
            Exception {
        KSBServiceLocator.getMessageQueueService().delete(message);
        PersistedMessage messageCopy = message.copy();
        JobDataMap jobData = new JobDataMap();
        jobData.put(MessageServiceExecutorJob.MESSAGE_KEY, messageCopy);
        JobDetailImpl jobDetail = new JobDetailImpl("Exception_Message_Job " + Math.random(), "Exception Messaging",
                MessageServiceExecutorJob.class);
        jobDetail.setJobDataMap(jobData);

        if (StringUtils.isNotBlank(description)) {
            jobDetail.setDescription(description);
        }

        scheduler.getListenerManager().addJobListener(new MessageServiceExecutorJobListener());

        SimpleTriggerImpl trigger = new SimpleTriggerImpl("Exception_Message_Trigger " + Math.random(),
                "Exception Messaging", messageCopy.getQueueDate());
        // 1.6 bug required or derby will choke
        trigger.setJobDataMap(jobData);

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
