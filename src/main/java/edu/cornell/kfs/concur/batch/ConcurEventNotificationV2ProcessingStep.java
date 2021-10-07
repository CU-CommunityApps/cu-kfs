package edu.cornell.kfs.concur.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;

public class ConcurEventNotificationV2ProcessingStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected ConcurAccessTokenV2Service concurAccessTokenV2Service;

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
        LOG.info("validateExpenseReports, has not been implemented yet.");
    }
    
    private void validateTravelRequests(String accessToken, List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        LOG.info("validateTravelRequests, has not been implemented yet.");
    }
    
    private void generateReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        LOG.info("generateReport, full report has not bee implemented yet");
        if (processingResults.isEmpty()) {
            LOG.info("generateReport, no reports nor travel requests were validated");
        } else {
            processingResults.stream().forEach(result -> logIndividualResult(result));
        }
    }
    
    private void logIndividualResult(ConcurEventNotificationProcessingResultsDTO result) {
        LOG.info("logIndividualResult, " + result.toString());
    }

    public void setConcurAccessTokenV2Service(ConcurAccessTokenV2Service concurAccessTokenV2Service) {
        this.concurAccessTokenV2Service = concurAccessTokenV2Service;
    }

}
