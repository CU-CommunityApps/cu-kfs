package edu.cornell.kfs.cemi.sys.dataaccess;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;

public interface CemiFileAppenderOrmDao {

    void getSheetDataAndWriteToFile(final CemiExcelWriter writer, final CemiSheetDefinition sheetDefinition,
            final String jobRunDate);

}
