package  edu.cornell.kfs.cemi.pdp.batch.service;

import java.time.LocalDateTime;

public interface CemiPaymentElectionExtractService {
    
    void resetState();
    
    void populateListOfInScopeEmployeePaymentElections();
    
    void generateIntermediatePaymentElectionExtractData(final LocalDateTime jobRunDate);
    
    void generatePaymentElectionExtractFile(final LocalDateTime jobRunDate);
    
}
