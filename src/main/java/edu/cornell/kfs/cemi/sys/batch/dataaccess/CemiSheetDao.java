package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.util.stream.Stream;

public interface CemiSheetDao {

    Stream<String[]> getSheetRowDataForPrinting(final CemiSheetOrmMetadata sheetMetadata, final String jobRunDate);

}
