package edu.cornell.kfs.cemi.sys.dataaccess;

import java.util.Iterator;
import java.util.List;

public interface CemiCsvDataImportDao {

    void truncateDestinationTable(final String legacyDataDestinationTableName);

    void storeCsvData(final String legacyDataDestinationTableName,
            final List<String> legacyDataDestinationTableColumns, final Iterator<String[]> csvIterator);

}
