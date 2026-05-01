package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface CemiSheetDao {

    void insertSheetTableRows(final CemiTableMetadata metadata, final List<Object> rowObjects);

    int processSheetTableRows(final CemiTableMetadata metadata,
            final Map<String, Object> criteria, final List<String> orderByFields,
            final Consumer<String[]> rowHandler);

}
