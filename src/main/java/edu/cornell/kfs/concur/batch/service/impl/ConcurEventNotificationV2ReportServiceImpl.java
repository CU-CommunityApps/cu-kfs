package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.batch.report.ConcurEmailableReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2ReportService;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurEventNotificationV2ReportServiceImpl implements ConcurEventNotificationV2ReportService {
    private static final Logger LOG = LogManager.getLogger();
    
    private ReportWriterService reportWriterService;
    private ConfigurationService configurationService;
    private EmailService emailService;
    private ConcurBatchUtilityService concurBatchUtilityService;
    private DateTimeService dateTimeService;

    @Override
    public File generateReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        initializeReportTitleAndFileName(processingResults);
        buildSummarySeciton(processingResults);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }
    
    protected void initializeReportTitleAndFileName(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        reportWriterService.setFileNamePrefix(buildConcurReportFileName());
        reportWriterService.setTitle("Concur Event Notification V2. Processing Report");
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
    }
    
    protected void buildSummarySeciton(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        String sectionOpening = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_OPENING);
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(sectionOpening, "Summary"));

        String summaryLineFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SUMMARY_LINE);
        for (ConcurEventNoticationVersion2EventType eventType : ConcurEventNoticationVersion2EventType.values()) {
            for (ConcurEventNotificationVersion2ProcessingResults resultsType : ConcurEventNotificationVersion2ProcessingResults.values()) {
                List<ConcurEventNotificationProcessingResultsDTO> filteredDtos = filterResultsDto(processingResults,
                        eventType, resultsType);
                reportWriterService.writeFormattedMessageLine(MessageFormat.format(summaryLineFormat,
                        eventType.eventType, resultsType.statusForReport, filteredDtos.size()));
            }
        }

        reportWriterService.writeFormattedMessageLine(configurationService
                .getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_CLOSING));
    }
    
    protected List<ConcurEventNotificationProcessingResultsDTO> filterResultsDto(
            List<ConcurEventNotificationProcessingResultsDTO> processingResults,
            ConcurEventNoticationVersion2EventType eventType,
            ConcurEventNotificationVersion2ProcessingResults resultsType) {
        return processingResults.stream()
                .filter(result -> result.getEventType() == eventType && result.getProcessingResults() == resultsType)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    protected void buildDetailSections(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        /**
         ************ Expense Reports with Valid Accounts ****************
         *Report Number: XX-
         *Messages: Foo
         *          Bar
         *          Foo Bar
         *          
         *          
         *Report Nymber: YY
         *Messages:
         *
         *          
         *Report NUmber: ZZ
         *Messages: Tada
         ************************************* 
         */
    }
    
    private String buildConcurReportFileName() {
        StringBuilder sb = new StringBuilder("Concurn_Event_Notification_V2_Processing_Results_");
        Timestamp currentTimestamp = dateTimeService.getCurrentTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US);
        sb.append(formatter.format(currentTimestamp)).append(CUKFSConstants.TEXT_FILE_EXTENSION);
        return sb.toString();
    }

    @Override
    public void sendResultsEmail(List<ConcurEventNotificationProcessingResultsDTO> processingResults, File reportFile) {
        String body = readReportFileToString(reportFile);
        String subject = "Concur Event Notification Processing Results";
        sendEmail(subject, body);

    }
    
    protected String readReportFileToString(File reportFile) {
        String contents = concurBatchUtilityService.getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString, could not read report file into a String");
            contents = "Could not read the " + reportFile.getAbsolutePath() + " file.";
        }
        return contents;
    }
    
    public void sendEmail(String subject, String body) {
        String toAddress = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_TO_ADDRESS);
        String fromAddress = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_FROM_ADDRESS);
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
            emailService.sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }

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

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
