package  edu.cornell.kfs.cemi.module.cg.batch.service;

import java.time.LocalDateTime;

public interface CemiAwardScheduleExtractService {
    
    void resetState();
    
    void populateListOfInScopeAwards();
    
    void generateIntermediateAwardScheduleExtractData(final LocalDateTime jobRunDate);
    
    void generateAwardScheduleExtractFile(final LocalDateTime jobRunDate);
    
}
