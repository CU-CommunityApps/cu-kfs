package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.report.ConcurEmailableReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurReportEmailService;

public class ConcurReportEmailServiceImpl implements ConcurReportEmailService {
	private static final Logger LOG = LogManager.getLogger(ConcurReportEmailServiceImpl.class);
    
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConfigurationService configurationService;

    @Override
    public void sendResultsEmail(ConcurEmailableReportData reportData, File reportFile) {
        String body = readReportFileToString(reportData, reportFile);
        String subject = buildEmailSubject(reportData);
        sendEmail(subject, body);
    }
    
    protected String readReportFileToString(ConcurEmailableReportData reportData, File reportFile) {
        String contents = getConcurBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString, could not read report file into a String");
            contents = "Could not read the " + reportData.getReportTypeName() + " file.";
        }
        return contents;
    }
    
    protected String buildEmailSubject(ConcurEmailableReportData reportData) {
        StringBuilder sb = new StringBuilder("The ");
        sb.append(reportData.getReportTypeName()).append(" file ");
        sb.append(reportData.getConcurFileName()).append(" has been processed.");
        if(!reportData.getHeaderValidationErrors().isEmpty()) {
            sb.append("  There are header validation errors.");
        }
        if(shouldAppendLineLevelValidationErrors(reportData.getValidationErrorFileLines())) {
            sb.append("  There are line level validation errors.");
        }
        return sb.toString();
    }

    protected boolean shouldAppendLineLevelValidationErrors(List<ConcurBatchReportLineValidationErrorItem> lineValidationErrorItems) {
        return lineValidationErrorItems.stream().anyMatch(r -> r.getReportableAsLineLevelValidationError());
    }
//
//    todo: remove
//    protected boolean isValidLineLevelValidationError(ConcurBatchReportLineValidationErrorItem concurBatchReportLineValidationErrorItem) {
//
//        for (String errorMessage : concurBatchReportLineValidationErrorItem.getItemErrorResults()) {
//            if (errorMessage.contains("The line has the Pseudo (XXXX) payment code")) {
//                continue;
//            } else if (errorMessage.contains("The line detected as cash advance request. Should have been processed by first batch job step")){
//                continue;
//            } else if (errorMessage.contains("Cash Advance with key") && errorMessage.contains("did not have a corresponding entry from the Request Extract")) {
//                continue;
//            } else if (errorMessage.contains("Line was not processed because a Cash Advance under the same Report ID was missing a related Request Extract entry")) {
//                continue;
//            } else {
//                return true;
//            }
//        }
//
//        return false;
//    }

    @Override
    public void sendEmail(String subject, String body) {
        String toAddress = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_TO_ADDRESS);
        String fromAddress = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_FROM_ADDRESS);
        List<String> toAddressList = new ArrayList<>();
        toAddressList.add(toAddress);
        
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        message.setSubject(subject);
        message.getToAddresses().addAll(toAddressList);
        message.setMessage(body);
        
        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddress);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        try {
            getEmailService().sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }

    }
    
    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    
    

}
