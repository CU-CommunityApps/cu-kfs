package  edu.cornell.kfs.cemi.patterntemplate.batch.service;

// The routines in this interface define the processing pertaining to a SPECIFIC data conversion extraction file.
//
// 

import java.time.LocalDateTime;

public interface CemiEXTRACTNAMEExtractService {
    
    void resetState(String dataExtractName);
    
    void captureInScopeBusinessObjectKeysToProcessingTable(String dataExtractName);
    
    void generateIntermediateAwardScheduleExtractData(final LocalDateTime jobRunDate);
    
    void generateAwardScheduleExtractFile(final LocalDateTime jobRunDate);
    
}
