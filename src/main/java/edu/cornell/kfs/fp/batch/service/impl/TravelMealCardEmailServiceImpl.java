package edu.cornell.kfs.fp.batch.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.TravelMealCardEmailService;
import software.amazon.awssdk.utils.StringUtils;

public class TravelMealCardEmailServiceImpl implements TravelMealCardEmailService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected EmailService emailService;
    protected ParameterService parameterService;

    @Override
    public void sendErrorEmail(String toAddress, String emailSubject, String emailBody) { //(List<String> toAddresses, String emailSubject, String emailBody) {
        String fromAddress = emailService.getDefaultFromAddress();
        
        LOG.info("sendErrorEmail: sending error email");
        LOG.info("sendErrorEmail: DefaultFrom: {}", fromAddress);
        LOG.info("sendErrorEmail: To: {}", toAddress);
        LOG.info("sendErrorEmail: Subject: {}", emailSubject);
        LOG.info("sendErrorEmail: Message: {}", emailBody);
        
        if (StringUtils.isNotBlank(fromAddress) && StringUtils.isNotBlank(emailSubject)
                && StringUtils.isNotBlank(emailBody) && StringUtils.isNotBlank(toAddress)) {  // && toAddresses != null && !toAddresses.isEmpty()
//                && listDoesNotContainBlanks(toAddresses)) {
            BodyMailMessage message = new BodyMailMessage();
            message.setFromAddress(fromAddress);
            message.setSubject(emailSubject);
            message.addToAddress(toAddress);
            message.setMessage(emailBody);

            boolean htmlMessage = false;
            try {
                emailService.sendMessage(message, htmlMessage);
            } catch (Exception e) {
                LOG.error("sendErrorEmail: Exception caught, the email could not be sent", e);
            }
        } else {
            LOG.error("sendErrorEmail: Blank input parameter detected. The email could not be sent.");
        }
    }
    
    private boolean listDoesNotContainBlanks(List<String> listToVerify) {
        if (listToVerify == null) {
            return false;
        }
        for (String listItem : listToVerify) {
            if (listItem == null || listItem.isBlank()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void sendReportEmail(String fromAddress, List<String> toAddresses) {
        LOG.info("sendReportEmail: Needs to be written.");
//        BodyMailMessage message = new BodyMailMessage();
//        message.setFromAddress(fromAddress);
//        String subject = reportWriterService.getTitle();
//        message.setSubject(subject);
//        message.getToAddresses().addAll(toAddresses);
//        String body = concurBatchUtilityService.getFileContents(reportWriterService.getReportFile().getAbsolutePath());
//        message.setMessage(body);
//
//        boolean htmlMessage = false;
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("sendEmail, from address: " + fromAddress + "  to addresses: " + toAddresses);
//            LOG.debug("sendEmail, the email subject: " + subject);
//            LOG.debug("sendEmail, the email body: " + body);
//        }
//        try {
//            emailService.sendMessage(message, htmlMessage);
//        } catch (Exception e) {
//            LOG.error("sendEmail, the email could not be sent", e);
//        }
    }

    @Override
    public String generateNewFileNotReceivedSubject() {
        return parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPParameterConstants.TravelMealCardFileFeedService.TRAVEL_MEAL_CARD_LOAD_FILE_STEP_COMPONENT_NAME, 
                CuFPParameterConstants.TravelMealCardFileFeedService.TMCARD_NO_NEW_FILE_SUBJECT);
    }
    
    @Override
    public String generateNewFileNotReceivedMessage() {
        return parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPParameterConstants.TravelMealCardFileFeedService.TRAVEL_MEAL_CARD_LOAD_FILE_STEP_COMPONENT_NAME, 
                CuFPParameterConstants.TravelMealCardFileFeedService.TMCARD_NO_NEW_FILE_MESSAGE);
    }

    @Override
    public String getFileNotReceivedRecipentEmailAddress() {
        return parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
            CuFPParameterConstants.TravelMealCardFileFeedService.TRAVEL_MEAL_CARD_LOAD_FILE_STEP_COMPONENT_NAME, 
            CuFPParameterConstants.TravelMealCardFileFeedService.TMCARD_NO_NEW_FILE_EMAIL_ADDRESSES);
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
