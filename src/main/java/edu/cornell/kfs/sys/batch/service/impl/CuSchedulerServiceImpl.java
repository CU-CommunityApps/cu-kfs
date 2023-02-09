package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.ModuleConfiguration;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJob;
import org.kuali.kfs.ksb.messaging.threadpool.KSBScheduledPool;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.BatchSpringContext;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.sys.batch.JobDescriptor;
import org.kuali.kfs.sys.batch.SchedulerDummy;
import org.kuali.kfs.sys.service.BatchModuleService;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.service.CuSchedulerService;

@Transactional
public class CuSchedulerServiceImpl implements CuSchedulerService {

    private static final Logger LOG = LogManager.getLogger();

    private static final List<String> schedulerGroups = List.of(UNSCHEDULED_GROUP);
    private static final List<String> jobStatuses = List.of(SCHEDULED_JOB_STATUS_CODE, SUCCEEDED_JOB_STATUS_CODE,
            CANCELLED_JOB_STATUS_CODE, RUNNING_JOB_STATUS_CODE, FAILED_JOB_STATUS_CODE);

    private SchedulerDummy schedulerDummy;
    private KSBScheduledPool scheduledThreadPool;
    private KualiModuleService kualiModuleService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private JobDescriptor exceptionMessageJob;
    private JobDescriptor delayedAsyncCallJob;
    private List<JobListener> jobListeners;

    private ConcurrentMap<JobKey, CuJobEntry> jobs;
    private AtomicInteger nextExceptionMessageJobIndex;

    public CuSchedulerServiceImpl() {
        this.jobs = new ConcurrentHashMap<>();
        this.nextExceptionMessageJobIndex = new AtomicInteger();
    }

    @Override
    public void initialize() {
        LOG.info("initialize, Initializing jobs");
        initializeKfsJobListener();
        for (ModuleService moduleService : kualiModuleService.getInstalledModuleServices()) {
            loadJobsForModule(moduleService);
        }
    }

    private void initializeKfsJobListener() {
        for (JobListener jobListener : jobListeners) {
            if (jobListener instanceof org.kuali.kfs.sys.batch.JobListener) {
                ((org.kuali.kfs.sys.batch.JobListener) jobListener).setSchedulerService(this);
            }
        }
    }

    private void loadJobsForModule(ModuleService moduleService) {
        ModuleConfiguration moduleConfiguration = moduleService.getModuleConfiguration();
        LOG.info("loadJobsForModule, Loading jobs for module: " + moduleConfiguration.getNamespaceCode());
        if (CollectionUtils.isEmpty(moduleConfiguration.getJobNames())) {
            LOG.info("loadJobsForModule, No jobs found for module: " + moduleConfiguration.getNamespaceCode());
            return;
        }
        
        BatchModuleService batchModuleService = (moduleService instanceof BatchModuleService)
                ? (BatchModuleService) moduleService : null;
        Predicate<String> externalJobChecker = ObjectUtils.isNotNull(batchModuleService)
                ? batchModuleService::isExternalJob
                : jobName -> false;
        
        for (String jobName : moduleConfiguration.getJobNames()) {
            try {
                if (externalJobChecker.test(jobName)) {
                    LOG.warn("loadJobsForModule, Skipping setup of external job "
                            + jobName + " because this class does not support external jobs");
                    continue;
                }
                JobDescriptor jobDescriptor = getJobDescriptorBean(jobName);
                jobDescriptor.setNamespaceCode(moduleConfiguration.getNamespaceCode());
                addJobToGroup(jobDescriptor, UNSCHEDULED_GROUP);
            } catch (NoSuchBeanDefinitionException e) {
                LOG.error("loadJobsForModule, Could not find job descriptor bean: " + jobName, e);
            } catch (Exception e) {
                LOG.error("loadJobsForModule, Could not prepare job: " + jobName, e);
            }
        }
    }

    private JobDescriptor getJobDescriptorBean(String jobName) {
        return BatchSpringContext.getJobDescriptor(jobName);
    }

    private boolean addJobToGroup(JobDescriptor jobDescriptor, String groupName) {
        String jobLabel = jobDescriptor.getNamespaceCode() + CUKFSConstants.PADDED_HYPHEN + jobDescriptor.getName();
        LOG.info("addJobToGroup, Adding job '" + jobLabel + "' to group: " + groupName);
        if (!StringUtils.equals(jobDescriptor.getGroup(), groupName)) {
            LOG.info("addJobToGroup, Overriding group name to '" + groupName + "' for job: " + jobLabel);
            jobDescriptor.setGroup(groupName);
        }
        JobDetail jobDetail = jobDescriptor.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        CuJobEntry jobEntry = new CuJobEntry(jobDescriptor, jobDetail);
        if (jobs.putIfAbsent(jobKey, jobEntry) != null) {
            LOG.warn("addJobToGroup, Skipping detected duplicate insert of job (which may have been defined "
                    + "with separate scheduled and unscheduled bean variants): " + jobLabel);
            return false;
        }
        return true;
    }

    @Override
    public void addScheduled(JobDetail jobDetail) {
        LOG.warn("addScheduled, This class does not support adding scheduled jobs");
    }

    @Override
    public void addUnscheduled(JobDetail jobDetail) {
        LOG.warn("addScheduled, This class does not support adding unscheduled jobs through this method");
    }

    @Override
    public boolean cronConditionMet(String cronExpressionString) {
        LOG.warn("cronConditionMet, This class does not support checking cron conditions");
        return false;
    }

    @Override
    public BatchJobStatus getJob(String groupName, String jobName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        CuJobEntry jobEntry = jobs.get(jobKey);
        return ObjectUtils.isNotNull(jobEntry) ? jobEntry.toBatchJobStatusInstance() : null;
    }

    @Override
    public List<String> getJobStatuses() {
        return jobStatuses;
    }

    @Override
    public List<BatchJobStatus> getJobs() {
        return getJobs(UNSCHEDULED_GROUP);
    }

    @Override
    public List<BatchJobStatus> getJobs(String groupName) {
        if (!StringUtils.equals(groupName, UNSCHEDULED_GROUP)) {
            return List.of();
        }
        return jobs.values().stream()
                .map(CuJobEntry::toBatchJobStatusInstance)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Date getNextStartTime(final BatchJobStatus job) {
        LOG.warn("getNextStartTime, This class does not support scheduled jobs");
        return null;
    }

    @Override
    public Date getNextStartTime(String groupName, String jobName) {
        LOG.warn("getNextStartTime, This class does not support scheduled jobs");
        return null;
    }

    @Override
    public List<JobExecutionContext> getRunningJobs() {
        return jobs.values().stream()
                .map(CuJobEntry::getCurrentlyExecutingJob)
                .filter(ObjectUtils::isNotNull)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<String> getSchedulerGroups() {
        return schedulerGroups;
    }

    @Override
    public String getStatus(JobDetail jobDetail) {
        if (ObjectUtils.isNull(jobDetail)) {
            LOG.warn("getStatus, jobDetail is null; returning Failed status instead of throwing an exception, "
                    + "for consistency with base code");
            return FAILED_JOB_STATUS_CODE;
        }
        CuJobEntry jobEntry = getExistingJob(jobDetail.getKey().getName());
        return jobEntry.getJobStatus();
    }

    @Override
    public boolean hasIncompleteJob() {
        return false;
    }

    // Copied and adapted from SchedulerServiceImpl implementation.
    @Override
    public void initializeJob(String jobName, Job job) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeJob, Initializing job: " + jobName);
        }
        CuJobEntry jobEntry = getExistingJob(jobName);
        job.setSchedulerService(this);
        job.setParameterService(parameterService);
        job.setSteps(jobEntry.getJobDescriptor().getSteps());
        job.setDateTimeService(dateTimeService);
    }

    @Override
    public void interruptJob(String jobName) {
        LOG.warn("interruptJob, This class does not support interrupting jobs");
    }

    @Override
    public boolean isJobRunning(String jobName) {
        CuJobEntry jobEntry = getJobIfPresent(jobName);
        return ObjectUtils.isNotNull(jobEntry) && jobEntry.isRunning();
    }

    @Override
    public boolean isPastScheduleCutoffTime(Date startTime) {
        LOG.warn("isPastScheduleCutoffTime, This class does not support scheduled jobs");
        return false;
    }

    @Override
    public void logScheduleResults() {
        LOG.warn("logScheduleResults, This class does not support scheduled jobs");
    }

    @Override
    public void pastScheduleCutoffTimeNotify() {
        LOG.warn("pastScheduleCutoffTimeNotify, This class does not support scheduled jobs");
    }

    @Override
    public void processWaitingJobs() {
        LOG.warn("processWaitingJobs, This class does not support scheduled jobs");
    }

    @Override
    public void reinitializeScheduledJobs() {
        LOG.warn("reinitializeScheduledJobs, This class does not support scheduled jobs");
    }

    @Override
    public void removeScheduled(String jobName) {
        LOG.warn("removeScheduled, This class does not support scheduled jobs");
    }

    @Override
    public void runJob(String jobName, String requestorEmailAddress) {
        runJob(jobName, 0, 0, dateTimeService.getCurrentDate(), requestorEmailAddress);
    }

    @Override
    public void runJob(String jobName, int startStep, int stopStep, Date startTime, String requestorEmailAddress) {
        runJob(UNSCHEDULED_GROUP, jobName, startStep, stopStep, startTime, requestorEmailAddress);
    }

    @Override
    public void runJob(String groupName, String jobName, int startStep, int stopStep, Date jobStartTime,
            String requestorEmailAddress) {
        if (StringUtils.equals(groupName, CUKFSConstants.EXCEPTION_MESSAGING_GROUP)) {
            throw new IllegalArgumentException("This method cannot be used to execute Exception Messaging jobs");
        } else if (StringUtils.equals(groupName, CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_GROUP)) {
            throw new IllegalArgumentException("This method cannot be used to execute Delayed Asynchronous Call jobs");
        } else if (!StringUtils.equals(groupName, UNSCHEDULED_GROUP)) {
            LOG.warn("runJob, Group name '" + groupName + "' will be overridden with '" + UNSCHEDULED_GROUP + "'");
        }
        
        CuJobEntry jobEntry = getExistingJob(jobName);
        if (jobEntry.isRunning()) {
            LOG.warn("runJob, Skipping run because job is already in progress: " + jobName);
            return;
        }
        
        Map<String, String> extraSettings = Map.ofEntries(
                Map.entry(org.kuali.kfs.sys.batch.JobListener.REQUESTOR_EMAIL_ADDRESS_KEY,
                        StringUtils.defaultIfBlank(requestorEmailAddress, KFSConstants.EMPTY_STRING)),
                Map.entry(Job.JOB_RUN_START_STEP, String.valueOf(startStep)),
                Map.entry(Job.JOB_RUN_END_STEP, String.valueOf(stopStep)));
        
        runJob(jobEntry, extraSettings, jobStartTime);
    }

    @Override
    public void scheduleExceptionMessageJob(PersistedMessage message, String description) {
        JobDetail jobDetail = createExceptionMessageJobDetail(message, description);
        CuJobEntry jobEntry = new CuJobEntry(exceptionMessageJob, jobDetail);
        JobKey jobKey = jobDetail.getKey();
        boolean jobWasScheduled = false;
        
        if (jobs.putIfAbsent(jobKey, jobEntry) != null) {
            throw new IllegalStateException("Exception messaging job with name '" + jobKey.getName()
                    + "' was already running; this should NEVER happen");
        }
        
        try {
            Date queueDate = new Date(message.getQueueDate().getTime());
            runJob(jobEntry, Map.of(), queueDate);
            jobWasScheduled = true;
        } catch (RuntimeException e) {
            jobWasScheduled = false;
            LOG.error("scheduleExceptionMessageJob, Could not successfully schedule exception messaging", e);
            throw e;
        } finally {
            if (!jobWasScheduled) {
                jobs.remove(jobKey);
            }
        }
    }
    
    @Override
    public void scheduleDelayedAsyncCallJob(PersistedMessage message, String description) {
        JobDetail jobDetail = createDelayedAsyncCallJobDetail(message, description);
        CuJobEntry jobEntry = new CuJobEntry(delayedAsyncCallJob, jobDetail);
        JobKey jobKey = jobDetail.getKey();
        boolean jobWasScheduled = false;

        if (jobs.putIfAbsent(jobKey, jobEntry) != null) {
            throw new IllegalStateException("Job with name '" + jobKey.getName()
                    + "' was already running; this should NEVER happen");
        }

        try {
            Date queueDate = new Date(message.getQueueDate().getTime());
            runJob(jobEntry, Map.of(), queueDate);
            jobWasScheduled = true;
        } catch (RuntimeException e) {
            jobWasScheduled = false;
            LOG.error("scheduleDelayedAsyncCallJob, Could not successfully schedule job", e);
            throw e;
        } finally {
            if (!jobWasScheduled) {
                jobs.remove(jobKey);
            }
        }
    }
    
    private JobDetail createDelayedAsyncCallJobDetail(PersistedMessage message, String description) {
        String jobName = CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_JOB_NAME_PREFIX + Math.random();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(MessageServiceExecutorJob.MESSAGE_KEY, message);

        JobBuilder jobBuilder = JobBuilder.newJob();
        if (StringUtils.isNotBlank(description)) {
            jobBuilder = jobBuilder.withDescription(description);
        }
        return jobBuilder.withIdentity(jobName, CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_GROUP)
                .ofType(MessageServiceExecutorJob.class)
                .setJobData(jobDataMap)
                .build();
    }

    private JobDetail createExceptionMessageJobDetail(PersistedMessage message, String description) {
        int nextIndex = nextExceptionMessageJobIndex.updateAndGet(
                index -> (index < Integer.MAX_VALUE) ? index + 1 : 0);
        String jobName = CUKFSConstants.EXCEPTION_MESSAGE_JOB_NAME_PREFIX + nextIndex;
        
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(MessageServiceExecutorJob.MESSAGE_KEY, message);
        
        JobBuilder jobBuilder = JobBuilder.newJob();
        if (StringUtils.isNotBlank(description)) {
            jobBuilder = jobBuilder.withDescription(description);
        }
        return jobBuilder.withIdentity(jobName, CUKFSConstants.EXCEPTION_MESSAGING_GROUP)
                .ofType(MessageServiceExecutorJob.class)
                .setJobData(jobDataMap)
                .build();
    }

    private void runJob(CuJobEntry jobEntry, Map<String, String> extraSettings, Date jobStartTime) {
        Date currentDate = dateTimeService.getCurrentDate();
        long startDelay = calculateDelayPriorToRunningJob(currentDate, jobStartTime);
        Date scheduledStartTime = new Date(currentDate.getTime() + startDelay);
        CuJobExecutionContext executionContext = CuJobExecutionContext.forJobAndSettings(
                schedulerDummy, jobEntry.getJobDetail(), extraSettings, scheduledStartTime);
        scheduledThreadPool.schedule(() -> runJobWithoutQuartz(jobEntry, executionContext),
                startDelay, TimeUnit.MILLISECONDS);
    }

    private long calculateDelayPriorToRunningJob(Date currentDate, Date startTime) {
        if (ObjectUtils.isNull(startTime)) {
            return 0L;
        } else if (currentDate.compareTo(startTime) < 0) {
            return startTime.getTime() - currentDate.getTime();
        } else {
            return 0L;
        }
    }

    private void runJobWithoutQuartz(CuJobEntry jobEntry, CuJobExecutionContext executionContext) {
        boolean canAttemptRun = false;
        JobExecutionException jobException = null;
        try {
            canAttemptRun = jobEntry.setCurrentlyExecutingJobIfPossible(executionContext);
            if (!canAttemptRun) {
                LOG.warn("runJobWithoutQuartz, Skipping run because job is already in progress: "
                        + jobEntry.getJobDetail().getKey());
                return;
            }
            
            for (JobListener jobListener : jobListeners) {
                jobListener.jobToBeExecuted(executionContext);
            }
            executionContext.getJobInstance().execute(executionContext);
        } catch (Exception e) {
            LOG.error("runJobWithoutQuartz, Failed to execute job " + jobEntry.getJobDetail().getKey(), e);
            jobException = (e instanceof JobExecutionException)
                    ? (JobExecutionException) e : new JobExecutionException(e);
        } finally {
            try {
                if (canAttemptRun) {
                    notifyListenersOfJobExecutionQuietly(executionContext, jobException);
                    jobEntry.clearCurrentlyExecutingJob();
                }
                JobKey jobKey = jobEntry.getJobDetail().getKey();
                if (StringUtils.equals(jobKey.getGroup(), CUKFSConstants.EXCEPTION_MESSAGING_GROUP)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("runJobWithoutQuartz, Removing temporary exception message job: "
                                + jobKey.getName());
                    }
                    jobs.remove(jobKey);
                }
                if (StringUtils.equals(jobKey.getGroup(), CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_GROUP)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("runJobWithoutQuartz, Removing temporary delayed asynchronous call job: "
                                + jobKey.getName());
                    }
                    jobs.remove(jobKey);
                }
            } catch (Exception e) {
                LOG.error("runJobWithoutQuartz, Unexpected error during listener and entry cleanup", e);
            }
        }
    }

    private void notifyListenersOfJobExecutionQuietly(
            CuJobExecutionContext executionContext, JobExecutionException jobException) {
        try {
            for (JobListener jobListener : jobListeners) {
                try {
                    jobListener.jobWasExecuted(executionContext, jobException);
                } catch (Exception e) {
                    LOG.error("notifyListenersOfJobCompletionQuietly, Unexpected error while calling listener", e);
                }
            }
        } catch (Exception e) {
            LOG.error("notifyListenersOfJobCompletionQuietly, Could not iterate over listeners", e);
        }
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        Objects.requireNonNull(scheduler, "scheduler cannot be null");
        if (!(scheduler instanceof SchedulerDummy)) {
            throw new IllegalArgumentException("The scheduler object was not a no-op/dummy instance; "
                    + "please double-check the KFS configuration to make sure Quartz is disabled");
        }
        this.schedulerDummy = (SchedulerDummy) scheduler;
    }

    @Override
    public boolean shouldNotRun(JobDetail jobDetail) {
        return isJobRunning(jobDetail.getKey().getName());
    }

    @Override
    public void updateStatus(JobDetail jobDetail, String jobStatus) {
        LOG.info("Updating status of job: {}={}", () -> jobDetail.getKey().getName(), () -> jobStatus);
        CuJobEntry jobEntry = getExistingJob(jobDetail.getKey().getName());
        jobEntry.setJobStatus(jobStatus);
    }

    private CuJobEntry getExistingJob(String jobName) {
        CuJobEntry jobEntry = getJobIfPresent(jobName);
        if (ObjectUtils.isNull(jobEntry)) {
            throw new IllegalStateException("Job does not exist: " + jobName);
        }
        return jobEntry;
    }

    private CuJobEntry getJobIfPresent(String jobName) {
        String groupName = UNSCHEDULED_GROUP;
        if (StringUtils.startsWithIgnoreCase(jobName, CUKFSConstants.EXCEPTION_MESSAGE_JOB_NAME_PREFIX)) {
            groupName = CUKFSConstants.EXCEPTION_MESSAGING_GROUP;
        } else if (StringUtils.startsWithIgnoreCase(jobName, CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_JOB_NAME_PREFIX)) {
            groupName = CUKFSConstants.DELAYED_ASYNCHRONOUS_CALL_GROUP;
        }
        JobKey jobKey = new JobKey(jobName, groupName);
        return jobs.get(jobKey);
    }

    public void setScheduledThreadPool(KSBScheduledPool scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }

    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setExceptionMessageJob(JobDescriptor exceptionMessageJob) {
        this.exceptionMessageJob = exceptionMessageJob;
    }

    public void setJobListeners(List<JobListener> jobListeners) {
        this.jobListeners = List.copyOf(jobListeners);
    }
    
    public JobDescriptor getDelayedAsyncCallJob() {
        return delayedAsyncCallJob;
    }

    public void setDelayedAsyncCallJob(JobDescriptor delayedAsyncCallJob) {
        this.delayedAsyncCallJob = delayedAsyncCallJob;
    }

}
