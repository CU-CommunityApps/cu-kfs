package  edu.cornell.kfs.cemi.patterntemplate.batch.service;

// The method signatures in this interface define minimum processing necessary for a SPECIFIC data conversion 
// extraction file. For clarity, the actual {EXTRACTNAME} is purposely part of the interface and implementation 
// class names for each data extraction. When coding substitute the camel cased value that is being used 
// everywhere else in this pattern for that same {EXTRACTNAME} item.

import java.time.LocalDateTime;

public interface CemiEXTRACTNAMEExtractService {
    
    void resetState();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
