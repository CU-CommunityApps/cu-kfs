package edu.cornell.kfs.sys.batch.service.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.JobDescriptor;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public final class CuJobEntry {

    private final JobDescriptor jobDescriptor;
    private final JobDetail jobDetail;
    private final AtomicReference<JobExecutionContext> currentlyExecutingJob;
    private final AtomicReference<String> jobStatus;

    public CuJobEntry(JobDescriptor jobDescriptor, JobDetail jobDetail) {
        this.jobDescriptor = jobDescriptor;
        this.jobDetail = jobDetail;
        this.currentlyExecutingJob = new AtomicReference<>();
        this.jobStatus = new AtomicReference<>(KFSConstants.EMPTY_STRING);
    }

    public JobDescriptor getJobDescriptor() {
        return jobDescriptor;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public String getJobStatus() {
        return jobStatus.get();
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus.set(jobStatus);
    }

    public boolean isRunning() {
        return ObjectUtils.isNotNull(getCurrentlyExecutingJob());
    }

    public JobExecutionContext getCurrentlyExecutingJob() {
        return currentlyExecutingJob.get();
    }

    public boolean setCurrentlyExecutingJobIfPossible(JobExecutionContext currentlyExecutingJob) {
        return this.currentlyExecutingJob.compareAndSet(null, currentlyExecutingJob);
    }

    public void clearCurrentlyExecutingJob() {
        currentlyExecutingJob.set(null);
    }

    public BatchJobStatus toBatchJobStatusInstance() {
        return new BatchJobStatus(jobDescriptor, jobDetail);
    }

}
