package  edu.cornell.kfs.cemi.module.cg.batch.service;

import java.time.LocalDateTime;

public interface CemiSponsorExtractService {

    // Default implementation is provided by abstract base class CemiDataExtractServiceBase.
    void logSystemParameterValues();

    void resetState();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
