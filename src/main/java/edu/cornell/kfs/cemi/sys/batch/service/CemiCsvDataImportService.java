package edu.cornell.kfs.cemi.sys.batch.service;

import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;

public interface CemiCsvDataImportService {

    void truncateDestinationTable(final CemiCsvBatchInputFileType batchInputFileType);

    void importCsvData(final CemiCsvBatchInputFileType batchInputFileType);

}
