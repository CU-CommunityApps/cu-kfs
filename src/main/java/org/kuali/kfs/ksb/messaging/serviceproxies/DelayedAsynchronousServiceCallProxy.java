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
package org.kuali.kfs.ksb.messaging.serviceproxies;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.kuali.kfs.core.api.util.reflect.BaseInvocationHandler;
import org.kuali.kfs.core.api.util.reflect.TargetedInvocationHandler;
import org.kuali.kfs.ksb.api.messaging.AsynchronousCall;
import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJob;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJobListener;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.SpringContext;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import edu.cornell.kfs.sys.batch.service.CuSchedulerService;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A proxy which schedules a service to be executed asynchronously after some delay period.
 */
// This class has been customized for Cornell to fix an issue with scheduling doc routing when quartz is not available
public final class DelayedAsynchronousServiceCallProxy extends BaseInvocationHandler
        implements TargetedInvocationHandler<Object> {

    private static final Logger LOG = LogManager.getLogger();

    private final String serviceName;
    private final String value1;
    private final long delayMilliseconds;
    private DateTimeService dateTimeService;

    private DelayedAsynchronousServiceCallProxy(final String serviceName, final String value1, final long delayMilliseconds) {
        this.serviceName = serviceName;
        this.value1 = value1;
        this.delayMilliseconds = delayMilliseconds;
    }

    public static Object createInstance(final String serviceName, final String value1, final long delayMilliseconds) {
        if (StringUtils.isBlank(serviceName)) {
            throw new RuntimeException("Cannot create service proxy, no service name passed in.");
        }
        try {
            return Proxy.newProxyInstance(ClassLoaderUtils.getDefaultClassLoader(),
                    ClassLoaderUtils.getInterfacesToProxy(GlobalResourceLoader.getService(serviceName), null, null),
                    new DelayedAsynchronousServiceCallProxy(serviceName, value1, delayMilliseconds));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object invokeInternal(final Object proxy, final Method method, final Object[] arguments) throws Throwable {
        synchronized (this) {
            final AsynchronousCall methodCall = new AsynchronousCall(method.getParameterTypes(), arguments, serviceName,
                    method.getName());
            final PersistedMessage message = PersistedMessage.buildMessage(serviceName, methodCall);
            message.setValue1(value1);

            final ZonedDateTime zonedDateTime = getDateTimeService().getLocalDateTimeNow()
                    .atZone(ZoneId.systemDefault())
                    .plus(delayMilliseconds, ChronoUnit.MILLIS);

            message.setQueueDate(new Timestamp(zonedDateTime.toInstant().toEpochMilli()));
            scheduleMessage(message);
        }
        return null;
    }

    // Cornell Customization to fix issue where quartz is being called to schedule doc routing when quartz is not available
    private void scheduleMessage(final PersistedMessage message) throws SchedulerException {
        LOG.debug("Scheduling execution of a delayed asynchronous message.");
        
        final String description = "Delayed_Asynchronous_Call";
        final boolean useQuartzScheduling = Boolean.valueOf(ConfigContext.getCurrentContextConfig().getProperty(KFSPropertyConstants.USE_QUARTZ_SCHEDULING_KEY));
        
        if (!useQuartzScheduling) {
            getCuSchedulerService().scheduleDelayedAsyncCallJob(message, description);
            return;
        }
        
        final Scheduler scheduler = KSBServiceLocator.getScheduler();
        final JobDataMap jobData = new JobDataMap();
        jobData.put(MessageServiceExecutorJob.MESSAGE_KEY, message);

        final JobDetail jobDetail = JobBuilder.newJob()
                .ofType(MessageServiceExecutorJob.class)
                .usingJobData(jobData)
                .withIdentity("Delayed_Asynchronous_Call-" + Math.random(), "Delayed_Asynchronous_Call")
                .build();

        scheduler.getListenerManager().addJobListener(new MessageServiceExecutorJobListener());

        final Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(message.getQueueDate())
                .usingJobData(jobData)
                .withIdentity(
                        TriggerKey.triggerKey("Delayed_Asynchronous_Call_Trigger-" + Math.random(),
                                "Delayed_Asynchronous_Call")
                )
                    .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Override
    public Object getTarget() {
        return GlobalResourceLoader.getService(serviceName);
    }

    public DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    private CuSchedulerService getCuSchedulerService() {
        return (CuSchedulerService) SpringContext.getBean(SchedulerService.class);
    }
}