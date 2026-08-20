package edu.cornell.kfs.cemi.vnd.batch.service;

import java.time.LocalDateTime;

public interface CemiSupplierExtractService {

    void resetState();

    void captureInScopeBusinessObjectKeysToProcessingTable();

    void generateIntermediateExtractData(final LocalDateTime jobRunDate);

    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);

}
