package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksReportEmailService;

public class PaymentWorksReportEmailServiceImpl implements PaymentWorksReportEmailService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksReportEmailServiceImpl.class);

    protected EmailService emailService;

    @Override
    public void sendEmail(String toAddress, String fromAddress, String subject, String body) {
        if (allDataForEmailIsValid(toAddress, fromAddress, subject, body)) {
            List<String> toAddressList = new ArrayList<>();
            toAddressList.add(toAddress);
            
            BodyMailMessage message = new BodyMailMessage();
            message.setFromAddress(fromAddress);
            message.setSubject(subject);
            message.getToAddresses().addAll(toAddressList);
            message.setMessage(body);
            boolean htmlMessage = false;
            if (LOG.isDebugEnabled()) {
                LOG.debug("sendEmail: from address: " + fromAddress + "  to address: " + toAddress);
                LOG.debug("sendEmail: the email subject: " + subject);
                LOG.debug("sendEmail: the email body: " + body);
            }
    
            try {
                getEmailService().sendMessage(message, htmlMessage);
            } catch (Exception e) {
                LOG.error("sendEmail: The email could not be sent: ", e);
            }
        }
    }
    
    private boolean allDataForEmailIsValid (String toAddress, String fromAddress, String subject, String body) {
        if (StringUtils.isNotBlank(toAddress) && StringUtils.isNotBlank(fromAddress) && StringUtils.isNotBlank(body) && StringUtils.isNotBlank(subject)) {
            return true;
        }
        else {
            LOG.error("allDataForEmailIsValid: Could not send email as requested because " + 
               ((StringUtils.isBlank(toAddress)) ? "toAddress is blank. " : "") +
               ((StringUtils.isBlank(fromAddress)) ? "fromAddress is blank. " : "") +
               ((StringUtils.isBlank(subject)) ? "subject is blank. " : "") +
               ((StringUtils.isBlank(body)) ? "body is blank. " : ""));
            return false;
        }
    }
    
    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
}
