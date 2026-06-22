package edu.cornell.kfs.cemi.sys.dataaccess;

import java.util.Iterator;

import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;

public interface CemiCsvDataImportDao {

    void truncateDestinationTable(final CemiCsvBatchInputFileType batchInputFileType);

    void storeCsvData(final CemiCsvBatchInputFileType batchInputFileType, final Iterator<String[]> csvIterator);

}
