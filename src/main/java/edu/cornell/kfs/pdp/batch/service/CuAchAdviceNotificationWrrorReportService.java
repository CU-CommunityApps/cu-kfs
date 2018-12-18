package edu.cornell.kfs.pdp.batch.service;

import java.io.File;
import java.util.List;

import edu.cornell.kfs.pdp.batch.PDPBadEmailRecord;

public interface CuAchAdviceNotificationWrrorReportService {
    
    File createBadEmailReport(List<PDPBadEmailRecord> badEmailRecords);
    
    void emailBadEmailReport(File errorReport);

}
