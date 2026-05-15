package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.util.function.Consumer;

public interface CemiSheetDao {

    void getAndHandleSheetRowDataForPrinting(final CemiSheetOrmMetadata sheetMetadata, final String jobRunDate,
            final Consumer<String[]> sheetRowHandler);

}
