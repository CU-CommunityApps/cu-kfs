package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.batch.PDPBadEmailRecord;
import edu.cornell.kfs.pdp.batch.service.CuAchAdviceNotificationErrorReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CuAchAdviceNotificationErrorReportServiceImpl implements CuAchAdviceNotificationErrorReportService {
    private static final Logger LOG = LogManager.getLogger(CuAchAdviceNotificationErrorReportServiceImpl.class);
    
    protected ReportWriterService reportWriterService;
    protected ConfigurationService configurationService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ParameterService parameterService;

    @Override
    public File createBadEmailReport(List<PDPBadEmailRecord> badEmailRecords) {
        LOG.info("createBadEmailReport, there are " + badEmailRecords.size() + " bad email records to report");
        initializeErrorReport();
        printErrorReportDetails(badEmailRecords);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }
    
    private void initializeErrorReport() {
        reportWriterService.setFileNamePrefix(configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_PREFIX));
        reportWriterService.setTitle(configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_TITLE));
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
        String rowFormat = configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_FORMAT);
        reportWriterService.writeFormattedMessageLine(rowFormat, "PAYEE ID", "PAYMENT GROUP", "DISBURSEMENT NUMBER", "EMAIL ADDRESS");
        String hyphens25 = buildHyphenString(20);
        String hyphens200 = buildHyphenString(50);
        reportWriterService.writeFormattedMessageLine(rowFormat, hyphens25, hyphens25, hyphens25, hyphens200);
    }
    
    private String buildHyphenString(int numberOfHyphens) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numberOfHyphens; i++) {
            sb.append("-");
        }
        return sb.toString();
    }
    
    private void printErrorReportDetails(List<PDPBadEmailRecord> badEmailRecords) {
        String rowFormat = configurationService.getPropertyValueAsString(CUPdpKeyConstants.PDP_SEND_ACH_NOTIFICATION_ERROR_REPORT_FORMAT);
        for (PDPBadEmailRecord record : badEmailRecords) {
            reportWriterService.writeFormattedMessageLine(rowFormat, record.getPayeeId(), record.getPaymentGroupId(), record.getDisbursementNumber(), record.getEmailAddress());
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
        return parameterService.getParameterValueAsString(KFSConstants.ParameterNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARM_NM);
    }
    
    private List<String> findPDPToEmailAddress() {
        Collection addresses = parameterService.getParameterValuesAsString(KFSConstants.ParameterNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, 
                CUPdpParameterConstants.PDP_ACH_INVALID_EMAIL_ERROR_REPORT_TO_ADDRESSES);
        return new ArrayList<String>(addresses);
    }
    
    @Override
    public void validateEmailAddress(String email) throws AddressException {
        if (email == null) {
            throw new AddressException("The email must not be null");
        }
        InternetAddress emailAddr = new InternetAddress(email);
        emailAddr.validate();
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
