package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;
import edu.cornell.kfs.cemi.sys.batch.ImportCemiLegacyDataStep;
import edu.cornell.kfs.cemi.sys.batch.service.CemiCsvDataImportService;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiCsvDataImportDao;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;

public class CemiCsvDataImportServiceImpl implements CemiCsvDataImportService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiCsvDataImportDao cemiCsvDataImportDao;
    private BatchInputFileService batchInputFileService;
    private ParameterService parameterService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public CemiCsvBatchInputFileType getBatchInputFileTypeForProcessing() {
        final String inputFileTypeBeanName = parameterService.getParameterValueAsString(
                ImportCemiLegacyDataStep.class, CemiBaseParameterConstants.CEMI_LEGACY_DATA_IMPORT_FILE_TYPE);
        Validate.validState(StringUtils.isNotBlank(inputFileTypeBeanName),
                "Value of parameter CEMI_LEGACY_DATA_IMPORT_FILE_TYPE cannot be blank");
        final CemiCsvBatchInputFileType inputFileType = SpringContext.getBean(
                CemiCsvBatchInputFileType.class, inputFileTypeBeanName);
        return inputFileType;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void truncateDestinationTableFor(final CemiCsvBatchInputFileType batchInputFileType) {
        validateBatchInputFileTypeSetup(batchInputFileType);
        final String fileTypeName = batchInputFileType.getFileTypeName();
        LOG.info("truncateDestinationTableFor, Truncating table for {}", fileTypeName);
        cemiCsvDataImportDao.truncateDestinationTable(batchInputFileType.getLegacyDataDestinationTableName());
        LOG.info("truncateDestinationTableFor, Finished truncating table for {}", fileTypeName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void importCsvDataFor(final CemiCsvBatchInputFileType batchInputFileType) {
        validateBatchInputFileTypeSetup(batchInputFileType);

        final String fileTypeName = batchInputFileType.getFileTypeName();
        final String fileName = findInputFileToProcess(batchInputFileType);
        final String simpleFileName = CuBatchFileUtils.getFileNameWithoutPath(fileName);

        LOG.info("importCsvDataFor, Importing data for {} from file: {}", fileTypeName, simpleFileName);
        try (
            final CemiCsvReader csvReader = new CemiCsvReader(fileName);
        ) {
            processCsvContent(csvReader, batchInputFileType);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            removeDoneFileFor(fileName);
        }
        LOG.info("importCsvDataFor, Finished importing data for {} from file: {}", fileTypeName, simpleFileName);
    }

    private void validateBatchInputFileTypeSetup(final CemiCsvBatchInputFileType batchInputFileType) {
        Validate.notNull(batchInputFileType, "batchInputFileType cannot be null");
        Validate.notBlank(batchInputFileType.getFileTypeName(),
                "batchInputFileType cannot have a blank file type name; this should have been auto-configured on the bean");
        Validate.notBlank(batchInputFileType.getLegacyDataDestinationTableName(),
                "batchInputFileType cannot have a blank destination table name");
        Validate.isTrue(CollectionUtils.isNotEmpty(batchInputFileType.getLegacyDataDestinationTableColumns()),
                "batchInputFileType cannot have a null or empty list of destination table columns");
        Validate.notBlank(batchInputFileType.getDirectoryPath(), "batchInputFileType cannot have a blank directory path");
    }

    private String findInputFileToProcess(final CemiCsvBatchInputFileType batchInputFileType) {
        final String fileTypeName = batchInputFileType.getFileTypeName();
        final List<String> inputFiles = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
        Validate.validState(!inputFiles.isEmpty(), "No input files found for %s", fileTypeName);
        Validate.validState(inputFiles.size() == 1, "Found multiple input files for %s", fileTypeName);
        return inputFiles.get(0);
    }

    private void processCsvContent(final CemiCsvReader csvReader, final CemiCsvBatchInputFileType batchInputFileType) {
        final Iterator<String[]> csvIterator = csvReader.iterator();
        if (csvIterator.hasNext() && batchInputFileType.isHasHeaderRow()) {
            final String simpleFileName = csvReader.getInputFile().getName();
            LOG.info("processCsvContent, Skipping header row for file: {}", simpleFileName);
            final String[] headerRow = csvIterator.next();
            final int expectedColumnCount = batchInputFileType.getLegacyDataDestinationTableColumns().size();
            Validate.validState(headerRow.length == expectedColumnCount,
                    "File %s has the wrong number of headers; expected: %s, actual: %s",
                    simpleFileName, expectedColumnCount, headerRow.length);
        }
        cemiCsvDataImportDao.storeCsvData(batchInputFileType.getLegacyDataDestinationTableName(),
                batchInputFileType.getLegacyDataDestinationTableColumns(), csvIterator);
    }

    private void removeDoneFileFor(final String csvFileName) {
        final File doneFile = new File(
                StringUtils.substringBeforeLast(csvFileName, KFSConstants.DELIMITER) + FileExtensions.DONE);
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    public void setCemiCsvDataImportDao(final CemiCsvDataImportDao cemiCsvDataImportDao) {
        this.cemiCsvDataImportDao = cemiCsvDataImportDao;
    }

    public void setBatchInputFileService(final BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
