package  edu.cornell.kfs.cemi.module.cg.batch.service;

import java.time.LocalDateTime;

public interface CemiAwardExtractService {
    
    void logSystemParameterValues();

    void resetState();
    
    void initializeExtractDateSettings();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
