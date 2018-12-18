package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.batch.PDPBadEmailRecord;
import edu.cornell.kfs.pdp.batch.service.CuAchAdviceNotificationWrrorReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CuAchAdviceNotificationWrrorReportServiceImpl implements CuAchAdviceNotificationWrrorReportService {
    private static final Logger LOG = LogManager.getLogger(CuAchAdviceNotificationWrrorReportServiceImpl.class);
    
    protected ReportWriterService reportWriterService;
    protected ConfigurationService configurationService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ParameterService parameterService;

    @Override
    public File createBadEmailReport(List<PDPBadEmailRecord> badEmailRecords) {
        LOG.info("createBadEmailReport, there are " + badEmailRecords.size() + " bad email records to report");
        initiatlizeErrorReport();
        printErrorReportDetails(badEmailRecords);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }
    
    private void initiatlizeErrorReport() {
        reportWriterService.setFileNamePrefix(configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_PREFIX));
        reportWriterService.setTitle(configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_TITLE));
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
        reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_HEADER));
    }
    
    private void printErrorReportDetails(List<PDPBadEmailRecord> badEmailRecords) {
        String detailLineFormat = configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_DETAIL);
        for (PDPBadEmailRecord record : badEmailRecords) {
            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailLineFormat, record.getPayeeId(), 
                    String.valueOf(record.getPaymentGroupId()), record.getEmailAddress()));
        }
        reportWriterService.writeNewLines(1);
    }

    @Override
    public void emailBadEmailReport(File errorReport) {
        if (errorReport != null) {
            String errorReportContents = concurBatchUtilityService.getFileContents(errorReport.getAbsolutePath());
            String fromAddress = findPdpFromEmailAddress();
            List<String> toAddressList = findPDPToEmailAddress();
            String subject = configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_TITLE);
            
            BodyMailMessage message = new BodyMailMessage();
            message.setFromAddress(fromAddress);
            message.setSubject(subject);
            message.getToAddresses().addAll(toAddressList);
            message.setMessage(errorReportContents);
            
            try {
                emailService.sendMessage(message, false);
            } catch (Exception e) {
                LOG.error("emailBadEmailReport, the email could not be sent", e);
            }
            
        } else {
            LOG.error("emailBadEmailReport, no error report sent in");
        }

    }
    
    private String findPdpFromEmailAddress() {
        return parameterService.getParameterValueAsString("KFS-PDP", "Batch", "FROM_EMAIL_ADDRESS");
    }
    
    private List<String> findPDPToEmailAddress() {
        Collection addresses = parameterService.getParameterValuesAsString("KFS-PDP", "Batch", "PDP_ACH_INVALID_EMAIL_ERROR_REPORT_TO_ADDRESSES");
        return new ArrayList<String>(addresses);
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
