package edu.cornell.kfs.concur.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2ReportService;
import edu.cornell.kfs.concur.batch.service.ConcurExpenseV3Service;
import edu.cornell.kfs.concur.batch.service.ConcurRequestV4Service;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;

public class ConcurEventNotificationV2ProcessingStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected ConcurAccessTokenV2Service concurAccessTokenV2Service;
    protected ConcurExpenseV3Service concurExpenseV3Service;
    protected ConcurRequestV4Service concurRequestV4Service;
    protected ConcurEventNotificationV2ReportService concurEventNotificationV2ReportService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        String accessToken = concurAccessTokenV2Service.retrieveNewAccessBearerToken();
        List<ConcurEventNotificationProcessingResultsDTO> processingResults = new ArrayList<>();
        validateExpenseReports(accessToken, processingResults);
        validateTravelRequests(accessToken, processingResults);
        generateReport(processingResults);
        return true;
    }
    
    private void validateExpenseReports(String accessToken, List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        concurExpenseV3Service.processExpenseReports(accessToken, processingResults);
    }
    
    private void validateTravelRequests(String accessToken, List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        List<ConcurEventNotificationProcessingResultsDTO> travelRequestProcessingResults = concurRequestV4Service
                .processTravelRequests(accessToken);
        processingResults.addAll(travelRequestProcessingResults);
    }
    
    private void generateReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        LOG.info("generateReport, full report has not bee implemented yet");
        if (processingResults.isEmpty()) {
            LOG.info("generateReport, no reports nor travel requests were validated");
        } else {
            addTestData(processingResults);
            processingResults.stream().forEach(result -> logIndividualResult(result));
            File reportFile = concurEventNotificationV2ReportService.generateReport(processingResults);
            concurEventNotificationV2ReportService.sendResultsEmail(processingResults, reportFile);
        }
    }
    
    /*
     * @todo remove this
     */
    private void addTestData(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        processingResults.add(createDto(ConcurEventNoticationVersion2EventType.TravelRequest, 
                ConcurEventNotificationVersion2ProcessingResults.validAccounts, "travel 123"));
        processingResults.add(createDto(ConcurEventNoticationVersion2EventType.TravelRequest, 
                ConcurEventNotificationVersion2ProcessingResults.validAccounts, "travel 456"));
        processingResults.add(createDto(ConcurEventNoticationVersion2EventType.TravelRequest, 
                ConcurEventNotificationVersion2ProcessingResults.invalidAccounts, "travel 89", "G234715 can not cen not be used for travel", "Object code 6666 can not be used for travel"));
        processingResults.add(createDto(ConcurEventNoticationVersion2EventType.ExpenseReport, 
                ConcurEventNotificationVersion2ProcessingResults.invalidAccounts, "expesne 123", "G234715 can not cen not be used for expense", "Object code 6666 can not be used for expense"));
        processingResults.add(createDto(ConcurEventNoticationVersion2EventType.ExpenseReport, 
                ConcurEventNotificationVersion2ProcessingResults.processingError, "expesne 321", "Something went haywire, try again"));
    }
    
    private ConcurEventNotificationProcessingResultsDTO createDto(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults resultsType, String reportNumber, String... messages) {
        ConcurEventNotificationProcessingResultsDTO dto = createDto(eventType, resultsType, reportNumber);
        List<String> messageList = Arrays.asList(messages);
        dto.setMessages(messageList);
        return dto;
    }
    
    private ConcurEventNotificationProcessingResultsDTO createDto(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults resultsType, String reportNumber) {
        ConcurEventNotificationProcessingResultsDTO dto = new ConcurEventNotificationProcessingResultsDTO(eventType, resultsType, reportNumber);
        return dto;
    }
    
    private void logIndividualResult(ConcurEventNotificationProcessingResultsDTO result) {
        LOG.info("logIndividualResult, " + result.toString());
    }

    public void setConcurAccessTokenV2Service(ConcurAccessTokenV2Service concurAccessTokenV2Service) {
        this.concurAccessTokenV2Service = concurAccessTokenV2Service;
    }
    
    public void setConcurExpenseV3Service(ConcurExpenseV3Service concurExpenseV3Service) {
        this.concurExpenseV3Service = concurExpenseV3Service;
    }

    public void setConcurRequestV4Service(ConcurRequestV4Service concurRequestV4Service) {
        this.concurRequestV4Service = concurRequestV4Service;
    }

    public void setConcurEventNotificationV2ReportService(
            ConcurEventNotificationV2ReportService concurEventNotificationV2ReportService) {
        this.concurEventNotificationV2ReportService = concurEventNotificationV2ReportService;
    }

}
