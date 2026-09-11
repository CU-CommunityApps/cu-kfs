package  edu.cornell.kfs.cemi.module.purap.batch.service;

import java.time.LocalDateTime;

public interface CemiPurchaseOrderExtractService {
    
    void resetState();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
