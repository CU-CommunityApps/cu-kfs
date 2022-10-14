package edu.cornell.kfs.concur.batch.service;

import java.io.File;
import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;

public interface ConcurEventNotificationV2ReportService {
    File generateReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults);

}
