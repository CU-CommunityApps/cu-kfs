package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.SchedulerDummy;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public final class CuJobExecutionContext implements JobExecutionContext {

    private static final Logger LOG = LogManager.getLogger();

    private final SchedulerDummy schedulerDummy;
    private final JobDetail jobDetail;
    private final Job jobInstance;
    private final long scheduledStartTimeInMilliseconds;
    private final AtomicLong actualStartTimeInMilliseconds;
    private final JobDataMap jobDataMap;

    private CuJobExecutionContext(SchedulerDummy schedulerDummy, JobDetail jobDetail, Job jobInstance,
            long scheduledStartTimeInMilliseconds) {
        this.schedulerDummy = schedulerDummy;
        this.jobDetail = jobDetail;
        this.jobInstance = jobInstance;
        this.scheduledStartTimeInMilliseconds = scheduledStartTimeInMilliseconds;
        this.actualStartTimeInMilliseconds = new AtomicLong();
        this.jobDataMap = jobDetail.getJobDataMap();
    }

    public static CuJobExecutionContext forJobAndSettings(SchedulerDummy schedulerDummy,
            JobDetail jobDetail, Map<String, String> extraSettings, Date scheduledStartTime) {
        JobDetail jobDetailCopy = (JobDetail) jobDetail.clone();
        Job jobInstance = createNewJobInstance(jobDetailCopy);
        jobDetailCopy.getJobDataMap().putAll(extraSettings);
        return new CuJobExecutionContext(schedulerDummy, jobDetailCopy, jobInstance, scheduledStartTime.getTime());
    }

    private static Job createNewJobInstance(JobDetail jobDetail) {
        try {
            Class<? extends Job> jobClass = jobDetail.getJobClass();
            return jobClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOG.error("createNewJobInstance, Failed to instantiate Job implementation class", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object get(Object key) {
        return jobDataMap.get(key);
    }

    @Override
    public Calendar getCalendar() {
        LOG.warn("getCalendar, This implementation does not have a Calendar instance");
        return null;
    }

    @Override
    public String getFireInstanceId() {
        LOG.warn("getFireInstanceId, This implementation does not have a unique instance ID");
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public Date getFireTime() {
        long actualStartTime = actualStartTimeInMilliseconds.get();
        return (actualStartTime > 0) ? new Date(actualStartTime) : null;
    }

    @Override
    public JobDetail getJobDetail() {
        return jobDetail;
    }

    @Override
    public Job getJobInstance() {
        return jobInstance;
    }

    @Override
    public long getJobRunTime() {
        LOG.warn("getJobRunTime, This implementation does not track the job run time");
        return 0;
    }

    @Override
    public JobDataMap getMergedJobDataMap() {
        return jobDataMap;
    }

    @Override
    public Date getNextFireTime() {
        LOG.warn("getNextFireTime, This implementation does not have a next fire time");
        return null;
    }

    @Override
    public Date getPreviousFireTime() {
        LOG.warn("getPreviousFireTime, This implementation does not have a previous fire time");
        return null;
    }

    @Override
    public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
        LOG.warn("getRecoveringTriggerKey, This implementation does not have a recovering trigger key");
        return null;
    }

    @Override
    public int getRefireCount() {
        return 0;
    }

    @Override
    public Object getResult() {
        LOG.warn("getResult, This implementation does not store the result");
        return null;
    }

    @Override
    public Date getScheduledFireTime() {
        return new Date(scheduledStartTimeInMilliseconds);
    }

    @Override
    public Scheduler getScheduler() {
        return schedulerDummy;
    }

    @Override
    public Trigger getTrigger() {
        LOG.warn("getTrigger, This implementation does not have a trigger");
        return null;
    }

    @Override
    public boolean isRecovering() {
        return false;
    }

    @Override
    public void put(Object key, Object value) {
        jobDataMap.put((String) key, value);
    }

    @Override
    public void setResult(Object result) {
        LOG.warn("setResult, This implementation does not store the result");
    }

    public void setActualStartTime(Date actualStartTime) {
        if (!actualStartTimeInMilliseconds.compareAndSet(0L, actualStartTime.getTime())) {
            LOG.error("setActualStartTime, Actual start time was already initialized; "
                    + "will use the existing value instead");
        }
    }

}
