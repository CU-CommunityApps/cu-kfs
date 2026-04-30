package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface CemiSheetDao {

    void insertSheetTableRows(final CemiTableMetadata metadata, final List<Object> rowObjects);

    Stream<String[]> getSheetTableRowsFormattedForFileOutput(final CemiTableMetadata metadata,
            final Map<String, Object> criteria, final List<String> orderByFields);

}
