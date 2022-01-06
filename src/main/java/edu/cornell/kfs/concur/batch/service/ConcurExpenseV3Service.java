package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;

public interface ConcurExpenseV3Service {

    public void processExpenseReports(String accessToken,
            List<ConcurEventNotificationProcessingResultsDTO> processingResults);

}
