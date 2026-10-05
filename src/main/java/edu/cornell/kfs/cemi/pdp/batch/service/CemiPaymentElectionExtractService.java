package  edu.cornell.kfs.cemi.pdp.batch.service;

import java.time.LocalDateTime;

public interface CemiPaymentElectionExtractService {

    void resetState();

    void captureInScopeBusinessObjectKeysToProcessingTable();

    void generateIntermediateExtractData(final LocalDateTime jobRunDate);

    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);

}
