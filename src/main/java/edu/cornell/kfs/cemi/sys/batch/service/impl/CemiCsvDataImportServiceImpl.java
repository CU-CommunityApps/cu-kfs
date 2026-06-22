package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiCsvDataImportService;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiCsvDataImportDao;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;

public class CemiCsvDataImportServiceImpl implements CemiCsvDataImportService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiCsvDataImportDao cemiCsvDataImportDao;
    private BatchInputFileService batchInputFileService;

    @Override
    public void truncateDestinationTable(final CemiCsvBatchInputFileType batchInputFileType) {
        validateBatchInputFileTypeSetup(batchInputFileType);
        final String enumClassName = batchInputFileType.getCsvEnumClass().getSimpleName();
        LOG.info("truncateDestinationTable, Truncating table for {}", enumClassName);
        cemiCsvDataImportDao.truncateDestinationTable(batchInputFileType);
        LOG.info("truncateDestinationTable, Finished truncating table for {}", enumClassName);
    }

    @Override
    public void importCsvData(final CemiCsvBatchInputFileType batchInputFileType) {
        validateBatchInputFileTypeSetup(batchInputFileType);

        final String enumClassName = batchInputFileType.getCsvEnumClass().getSimpleName();
        final List<String> inputFiles = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
        Validate.validState(!inputFiles.isEmpty(), "No input files found for %s", enumClassName);
        Validate.validState(inputFiles.size() == 1, "Found multiple input files for %s", enumClassName);
        final String fileName = inputFiles.get(0);
        final String simpleFileName = CuBatchFileUtils.getFileNameWithoutPath(fileName);

        LOG.info("importCsvData, Importing data for {} from file: {}", enumClassName, simpleFileName);
        try (
            final CemiCsvReader csvReader = new CemiCsvReader(fileName);
        ) {
            final Iterator<String[]> csvIterator = csvReader.iterator();
            cemiCsvDataImportDao.storeCsvData(batchInputFileType, csvIterator);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        LOG.info("importCsvData, Finished importing data for {} from file: {}", enumClassName, simpleFileName);
    }

    private void validateBatchInputFileTypeSetup(final CemiCsvBatchInputFileType batchInputFileType) {
        Validate.notNull(batchInputFileType, "batchInputFileType cannot be null");
        Validate.notNull(batchInputFileType.getCsvEnumClass(), "batchInputFileType cannot have a null CSV enum class");
        Validate.isTrue(batchInputFileType.getCsvEnumClass().isEnum(),
                "batchInputFileType has a CSV enum class that is not an enum");
        Validate.notBlank(batchInputFileType.getTableName(), "batchInputFileType cannot have a blank table name");
        Validate.notBlank(batchInputFileType.getDirectoryPath(), "batchInputFileType cannot have a blank directory path");
    }

    public void setCemiCsvDataImportDao(final CemiCsvDataImportDao cemiCsvDataImportDao) {
        this.cemiCsvDataImportDao = cemiCsvDataImportDao;
    }

    public void setBatchInputFileService(final BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

}
