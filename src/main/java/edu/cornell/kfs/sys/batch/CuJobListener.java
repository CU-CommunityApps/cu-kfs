package edu.cornell.kfs.sys.batch;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.JobListener;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.quartz.JobExecutionContext;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

public class CuJobListener extends JobListener  {
    private static final Logger LOG = LogManager.getLogger();
	
	protected void notify(final JobExecutionContext jobExecutionContext, final String jobStatus) {		
		try {
            final StringBuilder mailMessageSubject = new StringBuilder(jobExecutionContext.getJobDetail().getKey()
                    .getGroup()).append(": ").append(jobExecutionContext.getJobDetail().getKey().getName());
            final BodyMailMessage mailMessage = new BodyMailMessage();
            mailMessage.setFromAddress(emailService.getDefaultFromAddress());
            if (jobExecutionContext.getMergedJobDataMap().containsKey(REQUESTOR_EMAIL_ADDRESS_KEY)
                    && !StringUtils.isBlank(jobExecutionContext.getMergedJobDataMap()
                    .getString(REQUESTOR_EMAIL_ADDRESS_KEY))) {
                mailMessage.addToAddress(jobExecutionContext.getMergedJobDataMap()
                        .getString(REQUESTOR_EMAIL_ADDRESS_KEY));
            }
            if (SchedulerService.FAILED_JOB_STATUS_CODE.equals(jobStatus)
                    || SchedulerService.CANCELLED_JOB_STATUS_CODE.equals(jobStatus)) {
                mailMessage.addToAddress(emailService.getDefaultToAddress());
            }
            final String url = SpringContext.getBean(ParameterService.class).getParameterValueAsString("KFS-SYS", "Batch", "BATCH_REPORTS_URL");         
            mailMessageSubject.append(": ").append(jobStatus);
            final String messageText = MessageFormat.format(configurationService.getPropertyValueAsString(
                    KFSKeyConstants.MESSAGE_BATCH_FILE_LOG_EMAIL_BODY), url);
            mailMessage.setMessage(messageText);
            if (mailMessage.getToAddresses().size() > 0) {
                mailMessage.setSubject(mailMessageSubject.toString());
                emailService.sendMessage(mailMessage, false);
            }
        } catch (final Exception iae) {
            LOG.error(
                    "Caught exception while trying to send job completion notification e-mail for {}",
                    () -> jobExecutionContext.getJobDetail().getKey().getName(),
                    () -> iae
            );
        }
    }
	
}