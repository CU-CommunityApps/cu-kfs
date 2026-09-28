package  edu.cornell.kfs.cemi.module.cam.batch.service;

import java.time.LocalDateTime;

public interface CemiRegisterAssetExtractService {
    
    void resetState();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
