package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2ReportService;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurEventNotificationV2ReportServiceImpl implements ConcurEventNotificationV2ReportService {
    private static final Logger LOG = LogManager.getLogger();
    
    private ReportWriterService reportWriterService;
    private ConfigurationService configurationService;
    private ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public File generateReport(List<ConcurEventNotificationResponse> processingResults) {
        initializeReportTitleAndFileName(processingResults);
        buildSummarySeciton(processingResults);
        buildDetailSections(processingResults);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }
    
    protected void initializeReportTitleAndFileName(List<ConcurEventNotificationResponse> processingResults) {
        reportWriterService.setFileNamePrefix(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_FILE_NAME));
        reportWriterService.setTitle(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_TITLE));
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
    }
    
    private void buildSummarySeciton(List<ConcurEventNotificationResponse> processingResults) {
        String sectionOpening = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_OPENING);
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(sectionOpening, "Summary"));

        String summaryLineFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SUMMARY_LINE);
        for (ConcurEventNotificationType eventType : ConcurEventNotificationType.values()) {
            for (ConcurEventNotificationStatus resultsType : ConcurEventNotificationStatus.values()) {
                List<ConcurEventNotificationResponse> filteredDtos = filterResultsDto(processingResults,
                        eventType, resultsType);
                reportWriterService.writeFormattedMessageLine(MessageFormat.format(summaryLineFormat,
                        eventType.eventType, resultsType.statusForReport, filteredDtos.size()));
            }
        }

        reportWriterService.writeFormattedMessageLine(configurationService
                .getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_CLOSING));
        
        reportWriterService.writeNewLines(2);
    }
    
    private List<ConcurEventNotificationResponse> filterResultsDto(
            List<ConcurEventNotificationResponse> processingResults,
            ConcurEventNotificationType eventType,
            ConcurEventNotificationStatus resultsType) {
        return processingResults.stream()
                .filter(result -> result.getEventType() == eventType && result.getEventNotificationStatus() == resultsType)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private void buildDetailSections(List<ConcurEventNotificationResponse> processingResults) {
        String detailSummaryFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_DETAIL_SUMMARY);
        for (ConcurEventNotificationType eventType : ConcurEventNotificationType.values()) {
            for (ConcurEventNotificationStatus resultsType : ConcurEventNotificationStatus.values()) {
                List<ConcurEventNotificationResponse> filteredDtos = filterResultsDto(processingResults,
                        eventType, resultsType);
                if (CollectionUtils.isNotEmpty(filteredDtos)) {
                    String sectionTitle = MessageFormat.format(detailSummaryFormat, eventType.eventType, resultsType.statusForReport);
                    buildDetailSection(filteredDtos, sectionTitle, eventType);
                }
            }
        }
    }

    private void buildDetailSection(List<ConcurEventNotificationResponse> filteredDtos, String sectionTitle,
                                    ConcurEventNotificationType eventType) {
        String detailItemFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_DETAIL_ITEM);
        
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(configurationService.getPropertyValueAsString(
                ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_OPENING), sectionTitle));
        int reportIndex = 0;
        for (ConcurEventNotificationResponse dto : filteredDtos) {
            if (reportIndex > 0 ) {
                reportWriterService.writeNewLines(2);
            }

            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, eventType.reportNumberDescription, dto.getReportNumber()));
            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, eventType.reportNameDescription, dto.getReportName()));
            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, eventType.reportStatusDescription, dto.getReportStatus()));
            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, "Traveler Name", dto.getTravelerName()));
            if (eventType.displayTravelerEmail) {
                reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, "Traveler Email", dto.getTravelerEmail()));
            }
            String detailMessages = buildFormattedMessageForReport(detailItemFormat, "Detail Messages", dto.getDetailMessages());
            reportWriterService.writeFormattedMessageLine(detailMessages);
            String errorMessages = buildFormattedMessageForReport(detailItemFormat, "Error Messages", dto.getErrorMessages());
            reportWriterService.writeFormattedMessageLine(errorMessages);
            
            reportIndex++;
        }
        reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_CLOSING));
        reportWriterService.writeNewLines(2);
    }
    
    private String buildFormattedMessageForReport(String detailItemFormat, String header, List<String> messageList) {
        String messageListHeaderBlank = KFSConstants.NEWLINE + "          ";
        String messageOutput = StringUtils.join(messageList, messageListHeaderBlank);
        return MessageFormat.format(detailItemFormat, header, messageOutput);
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
