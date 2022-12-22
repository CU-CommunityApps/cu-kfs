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

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2ReportService;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurEventNotificationV2ReportServiceImpl implements ConcurEventNotificationV2ReportService {
    private static final Logger LOG = LogManager.getLogger();
    
    private ReportWriterService reportWriterService;
    private ConfigurationService configurationService;
    private ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public File generateReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        initializeReportTitleAndFileName(processingResults);
        buildSummarySeciton(processingResults);
        buildDetailSections(processingResults);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }
    
    protected void initializeReportTitleAndFileName(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        reportWriterService.setFileNamePrefix(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_FILE_NAME));
        reportWriterService.setTitle(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_TITLE));
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
    }
    
    private void buildSummarySeciton(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
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
        
        reportWriterService.writeNewLines(2);
    }
    
    private List<ConcurEventNotificationProcessingResultsDTO> filterResultsDto(
            List<ConcurEventNotificationProcessingResultsDTO> processingResults,
            ConcurEventNoticationVersion2EventType eventType,
            ConcurEventNotificationVersion2ProcessingResults resultsType) {
        return processingResults.stream()
                .filter(result -> result.getEventType() == eventType && result.getProcessingResults() == resultsType)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private void buildDetailSections(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        String detailSummaryFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_DETAIL_SUMMARY);
        for (ConcurEventNoticationVersion2EventType eventType : ConcurEventNoticationVersion2EventType.values()) {
            for (ConcurEventNotificationVersion2ProcessingResults resultsType : ConcurEventNotificationVersion2ProcessingResults.values()) {
                List<ConcurEventNotificationProcessingResultsDTO> filteredDtos = filterResultsDto(processingResults,
                        eventType, resultsType);
                if (CollectionUtils.isNotEmpty(filteredDtos)) {
                    String sectionTitle = MessageFormat.format(detailSummaryFormat, eventType.eventType, resultsType.statusForReport);
                    buildDetailSection(filteredDtos, sectionTitle, reportNumberDescription, eventType.displayTravelerEmail);
                    
                }
            }
        }
    }

    private void buildDetailSection(List<ConcurEventNotificationProcessingResultsDTO> filteredDtos, String sectionTitle,
                                    ConcurEventNoticationVersion2EventType eventType) {
        String detailItemFormat = configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_DETAIL_ITEM);
        
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(configurationService.getPropertyValueAsString(
                ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_OPENING), sectionTitle));
        int reportIndex = 0;
        for (ConcurEventNotificationProcessingResultsDTO dto : filteredDtos) {
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
            String messageListHeader = "Messages";
            String messageListHeaderBlank = KFSConstants.NEWLINE + "          ";
            String messageOutput = StringUtils.join(dto.getMessages(), messageListHeaderBlank);
            reportWriterService.writeFormattedMessageLine(MessageFormat.format(detailItemFormat, messageListHeader, messageOutput));
            
            reportIndex++;
        }
        reportWriterService.writeFormattedMessageLine(configurationService.getPropertyValueAsString(ConcurParameterConstants.CONCUR_EVENT_V2_PROCESSING_REPORT_SECTION_CLOSING));
        reportWriterService.writeNewLines(2);
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
