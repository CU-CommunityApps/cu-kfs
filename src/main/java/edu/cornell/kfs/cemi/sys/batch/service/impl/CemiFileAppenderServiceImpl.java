package edu.cornell.kfs.cemi.sys.batch.service.impl;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiSheetOrmDataHandlerDao;

public class CemiFileAppenderServiceImpl implements CemiFileAppenderService {

    private CemiSheetOrmDataHandlerDao cemiSheetOrmDataHandlerDao;

    @Override
    public void populateFileFromOrmDataStorage(final CemiExcelWriter writer, final CemiOutputDefinition outputDefinition,
            final String jobRunDate) {
        Validate.notNull(writer, "writer cannot be null");
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notBlank(jobRunDate, "jobRunDate cannot be blank");

        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            cemiSheetOrmDataHandlerDao.getSheetDataAndWriteToFile(writer, sheetDefinition, jobRunDate);
        }
    }

    public void setCemiSheetOrmDataHandlerDao(final CemiSheetOrmDataHandlerDao cemiSheetOrmDataHandlerDao) {
        this.cemiSheetOrmDataHandlerDao = cemiSheetOrmDataHandlerDao;
    }

}
