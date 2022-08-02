package edu.cornell.kfs.sys.batch;

import java.util.List;

import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.ksb.messaging.quartz.MessageServiceExecutorJobListener;
import org.kuali.kfs.ksb.messaging.threadpool.KSBScheduledPool;
import org.kuali.kfs.sys.batch.JobDescriptor;
import org.kuali.kfs.sys.batch.JobListener;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.batch.service.impl.SchedulerServiceImpl;
import org.kuali.kfs.sys.service.EmailService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.FactoryBean;

import edu.cornell.kfs.sys.batch.service.impl.CuSchedulerServiceImpl;

public class SchedulerServiceFactoryBean implements FactoryBean<SchedulerService> {

    private Scheduler scheduler;
    private JobListener jobListener;
    private MessageServiceExecutorJobListener messageServiceExecutorJobListener;
    private KualiModuleService kualiModuleService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private EmailService emailService;
    private KSBScheduledPool scheduledThreadPool;
    private JobDescriptor exceptionMessageJob;
    private JobDescriptor messageJob;
	private boolean useQuartzScheduling;

    private volatile SchedulerService schedulerServiceInstance;

    @Override
    public SchedulerService getObject() throws Exception {
        SchedulerService schedulerService = schedulerServiceInstance;
        if (schedulerService == null) {
            synchronized (this) {
                schedulerService = schedulerServiceInstance;
                if (schedulerService == null) {
                    schedulerService = useQuartzScheduling
                            ? createBaseFinancialsSchedulerService() : createCornellSpecificSchedulerService();
                    schedulerServiceInstance = schedulerService;
                }
            }
        }
        return schedulerService;
    }

    private SchedulerServiceImpl createBaseFinancialsSchedulerService() {
        SchedulerServiceImpl schedulerService = new SchedulerServiceImpl();
        schedulerService.setScheduler(scheduler);
        schedulerService.setJobListener(jobListener);
        schedulerService.setKualiModuleService(kualiModuleService);
        schedulerService.setParameterService(parameterService);
        schedulerService.setDateTimeService(dateTimeService);
        schedulerService.setEmailService(emailService);
        return schedulerService;
    }

    private CuSchedulerServiceImpl createCornellSpecificSchedulerService() {
        CuSchedulerServiceImpl schedulerService = new CuSchedulerServiceImpl();
        schedulerService.setScheduler(scheduler);
        schedulerService.setScheduledThreadPool(scheduledThreadPool);
        schedulerService.setKualiModuleService(kualiModuleService);
        schedulerService.setParameterService(parameterService);
        schedulerService.setDateTimeService(dateTimeService);
        schedulerService.setExceptionMessageJob(exceptionMessageJob);
        schedulerService.setMessageJob(messageJob);
        schedulerService.setJobListeners(List.of(jobListener, messageServiceExecutorJobListener));
        return schedulerService;
    }

    @Override
    public Class<SchedulerService> getObjectType() {
        return SchedulerService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public JobListener getJobListener() {
        return jobListener;
    }

    public void setJobListener(JobListener jobListener) {
        this.jobListener = jobListener;
    }

    public MessageServiceExecutorJobListener getMessageServiceExecutorJobListener() {
        return messageServiceExecutorJobListener;
    }

    public void setMessageServiceExecutorJobListener(
            MessageServiceExecutorJobListener messageServiceExecutorJobListener) {
        this.messageServiceExecutorJobListener = messageServiceExecutorJobListener;
    }

    public KualiModuleService getKualiModuleService() {
        return kualiModuleService;
    }

    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public KSBScheduledPool getScheduledThreadPool() {
        return scheduledThreadPool;
    }

    public void setScheduledThreadPool(KSBScheduledPool scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }

    public JobDescriptor getExceptionMessageJob() {
        return exceptionMessageJob;
    }

    public void setExceptionMessageJob(JobDescriptor exceptionMessageJob) {
        this.exceptionMessageJob = exceptionMessageJob;
    }

    public boolean isUseQuartzScheduling() {
        return useQuartzScheduling;
    }

    public void setUseQuartzScheduling(boolean useQuartzScheduling) {
        this.useQuartzScheduling = useQuartzScheduling;
    }
    
    public JobDescriptor getMessageJob() {
		return messageJob;
	}

	public void setMessageJob(JobDescriptor messageJob) {
		this.messageJob = messageJob;
	}

}
