package edu.cornell.kfs.concur.batch.service;

import java.io.File;
import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;

public interface ConcurEventNotificationV2ReportService {
    File generateReport(List<ConcurEventNotificationResponse> processingResults);

}
