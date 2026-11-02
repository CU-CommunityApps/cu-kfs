package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.time.LocalDateTime;

public interface CemiImportBudgetExtractService {

    void logSystemParameterValues();

    void resetState();

    void captureInScopeBusinessObjectKeysToProcessingTable();

    void generateIntermediateExtractData(final LocalDateTime jobRunDate);

    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);

}
