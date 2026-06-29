package  edu.cornell.kfs.cemi.patterntemplate.batch.service;

import java.time.LocalDateTime;

public interface CemiEXTRACTNAMEExtractService {
    
    void resetState(String dataExtractName);
    
    void captureInScopeBusinessObjectKeysToProcessingTable(String dataExtractName);
    
    void generateIntermediateAwardScheduleExtractData(final LocalDateTime jobRunDate);
    
    void generateAwardScheduleExtractFile(final LocalDateTime jobRunDate);
    
}
