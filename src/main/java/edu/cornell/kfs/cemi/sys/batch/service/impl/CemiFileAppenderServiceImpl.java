package edu.cornell.kfs.cemi.sys.batch.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiFileAppenderOrmDao;

public class CemiFileAppenderServiceImpl implements CemiFileAppenderService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiFileAppenderOrmDao cemiFileAppenderOrmDao;

    @Override
    public void populateFileFromOrmDataStorage(final CemiExcelWriter writer, final CemiOutputDefinition outputDefinition,
            final String jobRunDate) {
        Validate.notNull(writer, "writer cannot be null");
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notBlank(jobRunDate, "jobRunDate cannot be blank");

        LOG.info("populateFileFromOrmDataStorage, Writing file data for {} sheets", outputDefinition.getSheets().size());
        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            LOG.info("populateFileFromOrmDataStorage, Writing file data for sheet: {}", sheetDefinition.getName());
            cemiFileAppenderOrmDao.getSheetDataAndWriteToFile(writer, sheetDefinition, jobRunDate);
        }
        LOG.info("populateFileFromOrmDataStorage, Finished writing file data for sheets");
    }

    public void setCemiFileAppenderOrmDao(final CemiFileAppenderOrmDao cemiFileAppenderDao) {
        this.cemiFileAppenderOrmDao = cemiFileAppenderDao;
    }

}
