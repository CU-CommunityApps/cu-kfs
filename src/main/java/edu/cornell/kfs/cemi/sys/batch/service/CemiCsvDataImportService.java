package edu.cornell.kfs.cemi.sys.batch.service;

import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;

public interface CemiCsvDataImportService {

    CemiCsvBatchInputFileType getBatchInputFileTypeForProcessing();

    void truncateDestinationTableFor(final CemiCsvBatchInputFileType batchInputFileType);

    void importCsvDataFor(final CemiCsvBatchInputFileType batchInputFileType);

}
