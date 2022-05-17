package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import org.kuali.kfs.ksb.messaging.threadpool.KSBThreadPool;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.BatchSpringContext;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.sys.batch.JobDescriptor;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.service.BatchModuleService;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSConstants;

@Transactional
public class CuSchedulerServiceImpl implements SchedulerService, InitializingBean {

    private static final Logger LOG = LogManager.getLogger();

    private final List<String> schedulerGroups = List.of(UNSCHEDULED_GROUP);
    private final List<String> jobStatuses = List.of(SCHEDULED_JOB_STATUS_CODE, SUCCEEDED_JOB_STATUS_CODE,
            CANCELLED_JOB_STATUS_CODE, RUNNING_JOB_STATUS_CODE, FAILED_JOB_STATUS_CODE);

    private KualiModuleService kualiModuleService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private KSBThreadPool threadPool;
    private List<JobListener> jobListeners;
    private ConcurrentMap<JobKey, CuJobEntry> jobs;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.jobs = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        LOG.info("initialize, Initializing jobs");
        for (ModuleService moduleService : kualiModuleService.getInstalledModuleServices()) {
            loadJobsForModule(moduleService);
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
                addJob(jobDescriptor);
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

    private void addJob(JobDescriptor jobDescriptor) {
        String jobLabel = jobDescriptor.getNamespaceCode() + CUKFSConstants.PADDED_HYPHEN + jobDescriptor.getName();
        LOG.info("addJob, Adding job: " + jobLabel);
        if (!StringUtils.equals(jobDescriptor.getGroup(), UNSCHEDULED_GROUP)) {
            LOG.info("addJob, Overriding group name to 'unscheduled' for job: " + jobLabel);
            jobDescriptor.setGroup(UNSCHEDULED_GROUP);
        }
        JobDetail jobDetail = jobDescriptor.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        CuJobEntry jobEntry = new CuJobEntry(jobDescriptor, jobDetail);
        if (jobs.putIfAbsent(jobKey, jobEntry) != null) {
            LOG.warn("addJob, Skipping detected duplicate insert of job (which may have been defined "
                    + "with separate scheduled and unscheduled bean variants): " + jobLabel);
        }
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
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
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
    public Date getNextStartTime(BatchJobStatus job) {
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
        CuJobEntry jobEntry = getExistingUnscheduledJob(jobDetail.getKey().getName());
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
        CuJobEntry jobEntry = getExistingUnscheduledJob(jobName);
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
        CuJobEntry jobEntry = getUnscheduledJobIfPresent(jobName);
        return ObjectUtils.isNotNull(jobEntry) && jobEntry.isRunning();
    }

    @Override
    public boolean isPastScheduleCutoffTime() {
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
        if (!StringUtils.equals(groupName, UNSCHEDULED_GROUP)) {
            LOG.warn("runJob, Group name '" + groupName + "' will be overridden with '" + UNSCHEDULED_GROUP + "'");
        }
        CuJobEntry jobEntry = getExistingUnscheduledJob(jobName);
        if (jobEntry.isRunning()) {
            LOG.warn("runJob, Skipping run because job is already in progress: " + jobName);
            return;
        }
        
        Date currentDate = dateTimeService.getCurrentDate();
        Map<String, String> extraSettings = Map.ofEntries(
                Map.entry(org.kuali.kfs.sys.batch.JobListener.REQUESTOR_EMAIL_ADDRESS_KEY,
                        StringUtils.defaultIfBlank(requestorEmailAddress, KFSConstants.EMPTY_STRING)),
                Map.entry(Job.JOB_RUN_START_STEP, String.valueOf(startStep)),
                Map.entry(Job.JOB_RUN_END_STEP, String.valueOf(stopStep)));
        CuJobExecutionContext executionContext = CuJobExecutionContext.forJobAndSettings(
                jobEntry.getJobDetail(), extraSettings, currentDate);
        
        threadPool.execute(() -> runJobWithoutQuartz(jobEntry, executionContext));
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
            if (canAttemptRun) {
                notifyListenersOfJobExecutionQuietly(executionContext, jobException);
                jobEntry.clearCurrentlyExecutingJob();
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
        throw new UnsupportedOperationException("This class does not use a Quartz scheduler");
    }

    @Override
    public boolean shouldNotRun(JobDetail jobDetail) {
        return isJobRunning(jobDetail.getKey().getName());
    }

    @Override
    public void updateStatus(JobDetail jobDetail, String jobStatus) {
        LOG.info("updateStatus, Updating status of job '" + jobDetail.getKey().getName() + "' to '" + jobStatus + "'");
        CuJobEntry jobEntry = getExistingUnscheduledJob(jobDetail.getKey().getName());
        jobEntry.setJobStatus(jobStatus);
    }

    private CuJobEntry getExistingUnscheduledJob(String jobName) {
        CuJobEntry jobEntry = getUnscheduledJobIfPresent(jobName);
        if (ObjectUtils.isNull(jobEntry)) {
            throw new IllegalStateException("Job does not exist: " + jobName);
        }
        return jobEntry;
    }

    private CuJobEntry getUnscheduledJobIfPresent(String jobName) {
        JobKey jobKey = new JobKey(jobName, UNSCHEDULED_GROUP);
        return jobs.get(jobKey);
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

    public void setThreadPool (KSBThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setJobListeners(List<JobListener> jobListeners) {
        this.jobListeners = jobListeners;
    }

}
