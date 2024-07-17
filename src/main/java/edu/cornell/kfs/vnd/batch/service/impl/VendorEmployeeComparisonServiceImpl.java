package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.util.EnumConfiguredMappingStrategy;
import edu.cornell.kfs.sys.util.ForUnitTestConvenience;
import edu.cornell.kfs.sys.util.LoadFileUtils;
import edu.cornell.kfs.vnd.CUVendorConstants;
import edu.cornell.kfs.vnd.batch.VendorEmployeeComparisonCsv;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonReportService;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonService;
import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;
import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

public class VendorEmployeeComparisonServiceImpl implements VendorEmployeeComparisonService {

    private static final Logger LOG = LogManager.getLogger();

    private String csvEmployeeComparisonFileCreationDirectory;
    private String csvEmployeeComparisonFileExportDirectory;
    private CuVendorDao vendorDao;
    private VendorEmployeeComparisonReportService vendorEmployeeComparisonReportService;
    private BatchInputFileService batchInputFileService;
    private BatchInputFileType vendorEmployeeComparisonResultFileType;
    private DateTimeService dateTimeService;

    @ForUnitTestConvenience
    private BiConsumer<String, File> reportFileTracker = (csvResultFile, reportFile) -> {};

    @Transactional
    @Override
    public void generateFileContainingPotentialVendorEmployees() {
        final String csvFileName = generateEmployeeComparisonCsvOutboundFileName();
        final String qualifiedCreationDirectoryFileName = fullyQualifyFileNameWithCreationDirectory(csvFileName);
        final String qualifiedExportDirectoryFileName = fullyQualifyFileNameWithExportDirectory(csvFileName);

        LOG.info("generateFileContainingPotentialVendorEmployees, Creating employee comparison file: {}", csvFileName);
        final int ssnVendorCount = writeEmployeeComparisonOutboundCsvFile(qualifiedCreationDirectoryFileName);
        if (ssnVendorCount == 0) {
            LOG.warn("generateFileContainingPotentialVendorEmployees, There were no eligible vendors to include"
                    + " in the employee comparison file, so it will NOT be staged for export: {}", csvFileName);
            return;
        }
        LOG.info("generateFileContainingPotentialVendorEmployees, Successfully created employee comparison file "
                + "containing {} data rows: {}", ssnVendorCount, csvFileName);
        moveEmployeeComparisonCsvFileToExportDirectory(
                qualifiedCreationDirectoryFileName, qualifiedExportDirectoryFileName);
        LOG.info("generateFileContainingPotentialVendorEmployees, Successfully staged employee comparison file "
                + "for export: {}", csvFileName);
    }

    private String generateEmployeeComparisonCsvOutboundFileName() {
        final Date currentDate = dateTimeService.getCurrentDate();
        final DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
                .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));
        final String currentDateString = dateFormatter.format(currentDate.toInstant());
        return StringUtils.join(CUVendorConstants.EMPLOYEE_COMPARISON_OUTBOUND_FILE_PREFIX, currentDateString,
                FileExtensions.CSV);
    }

    private String fullyQualifyFileNameWithCreationDirectory(final String csvFileName) {
        return csvEmployeeComparisonFileCreationDirectory + CUKFSConstants.SLASH + csvFileName;
    }

    private String fullyQualifyFileNameWithExportDirectory(final String csvFileName) {
        return csvEmployeeComparisonFileExportDirectory + CUKFSConstants.SLASH + csvFileName;
    }

    private int writeEmployeeComparisonOutboundCsvFile(final String qualifiedCreationDirectoryFileName) {
        try (
                final Stream<VendorWithTaxId> ssnVendors = vendorDao.getPotentialEmployeeVendorsAsCloseableStream();
                final FileOutputStream fileStream = new FileOutputStream(qualifiedCreationDirectoryFileName);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
                final BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)
        ) {
            final int ssnVendorCount = writeSsnVendorsToCsvFile(ssnVendors, bufferedWriter);
            bufferedWriter.flush();
            return ssnVendorCount;
        } catch (final Exception e) {
            LOG.error("writeEmployeeComparisonOutboundCsvFile, Failed to create employee comparison CSV file", e);
            throw new RuntimeException(e);
        }
    }

    private int writeSsnVendorsToCsvFile(final Stream<VendorWithTaxId> ssnVendors, final BufferedWriter writer)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        final EnumConfiguredMappingStrategy<VendorWithTaxId, VendorEmployeeComparisonCsv> mappingStrategy =
                new EnumConfiguredMappingStrategy<>(
                        VendorEmployeeComparisonCsv.class,
                        VendorEmployeeComparisonCsv::getHeaderLabel,
                        VendorEmployeeComparisonCsv::getVendorDtoPropertyName);
        mappingStrategy.setType(VendorWithTaxId.class);

        final StatefulBeanToCsv<VendorWithTaxId> csvWriter = new StatefulBeanToCsvBuilder<VendorWithTaxId>(writer)
                .withMappingStrategy(mappingStrategy)
                .build();

        int ssnVendorCount = 0;
        for (final VendorWithTaxId ssnVendor : IteratorUtils.asIterable(ssnVendors.iterator())) {
            ssnVendorCount++;
            csvWriter.write(ssnVendor);
        }

        return ssnVendorCount;
    }

    private void moveEmployeeComparisonCsvFileToExportDirectory(
            final String qualifiedCreationDirectoryFileName, final String qualifiedExportDirectoryFileName) {
        try {
            final Path creationFilePath = convertToPath(qualifiedCreationDirectoryFileName);
            final Path exportFilePath = convertToPath(qualifiedExportDirectoryFileName);
            Files.move(creationFilePath, exportFilePath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.error("moveEmployeeComparisonCsvFileToExportDirectory, Failed to move file to export directory", e);
            throw new UncheckedIOException(e);
        }
    }

    private Path convertToPath(final String qualifiedFileName) {
        final File qualifiedFile = new File(qualifiedFileName);
        return qualifiedFile.toPath();
    }

    @Transactional
    @Override
    public boolean processResultsOfVendorEmployeeComparison() {
        final List<String> resultFiles = batchInputFileService.listInputFileNamesWithDoneFile(
                vendorEmployeeComparisonResultFileType);
        if (CollectionUtils.isEmpty(resultFiles)) {
            LOG.info("processResultsOfVendorEmployeeComparison, There were no employee comparison result files "
                    + "to process.");
            return true;
        }

        boolean allFilesSucceeded = true;

        for (final String resultFile : resultFiles) {
            try {
                processVendorEmployeeComparisonResultFile(resultFile);
            } catch (Exception e) {
                allFilesSucceeded = false;
                LOG.error("processResultsOfVendorEmployeeComparison, Failed to process comparison result file: {}",
                        resultFile, e);
            }
        }

        removeDoneFiles(resultFiles);
        return allFilesSucceeded;
    }

    private void processVendorEmployeeComparisonResultFile(final String resultFile) {
        final String resultFileSimpleName = CuBatchFileUtils.getFileNameWithoutPath(resultFile);
        LOG.info("processVendorEmployeeComparisonResultFile, Processing employee comparison result file: {}",
                resultFileSimpleName);
        final List<VendorEmployeeComparisonResult> parsedResult = parseVendorComparisonResultFile(resultFile);
        LOG.info("processVendorEmployeeComparisonResultFile, Generating report for result file: {}",
                resultFileSimpleName);
        final File reportFile = vendorEmployeeComparisonReportService.generateReportForVendorEmployeeComparisonResults(
                resultFile, parsedResult);
        LOG.info("processVendorEmployeeComparisonResultFile, Finished generating report for result file: {}",
                resultFileSimpleName);
        reportFileTracker.accept(resultFileSimpleName, reportFile);
    }

    @SuppressWarnings("unchecked")
    private List<VendorEmployeeComparisonResult> parseVendorComparisonResultFile(final String resultFile) {
        final byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(resultFile);
        try {
            final List<VendorEmployeeComparisonResult> parsedResult =
                    (List<VendorEmployeeComparisonResult>) batchInputFileService.parse(
                            vendorEmployeeComparisonResultFileType, fileByteContent);
            return parsedResult;
        } catch (Exception e) {
            LOG.error("parseVendorComparisonResultFile, Failed to parse comparison result file", e);
            throw new RuntimeException(e);
        }
    }

    // Copied and tweaked this method from CustomerLoadServiceImpl class.
    protected void removeDoneFiles(final List<String> dataFileNames) {
        for (final String dataFileName : dataFileNames) {
            final File doneFile = new File(
                    StringUtils.substringBeforeLast(dataFileName, KFSConstants.DELIMITER) + FileExtensions.DONE);
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    public void setCsvEmployeeComparisonFileCreationDirectory(
            final String csvEmployeeComparisonFileCreationDirectory) {
        this.csvEmployeeComparisonFileCreationDirectory = csvEmployeeComparisonFileCreationDirectory;
    }

    public void setCsvEmployeeComparisonFileExportDirectory(final String csvEmployeeComparisonFileExportDirectory) {
        this.csvEmployeeComparisonFileExportDirectory = csvEmployeeComparisonFileExportDirectory;
    }

    public void setVendorDao(final CuVendorDao vendorDao) {
        this.vendorDao = vendorDao;
    }

    public void setVendorEmployeeComparisonReportService(
            final VendorEmployeeComparisonReportService vendorEmployeeComparisonReportService) {
        this.vendorEmployeeComparisonReportService = vendorEmployeeComparisonReportService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setBatchInputFileService(final BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setVendorEmployeeComparisonResultFileType(
            final BatchInputFileType vendorEmployeeComparisonResultFileType) {
        this.vendorEmployeeComparisonResultFileType = vendorEmployeeComparisonResultFileType;
    }

    @ForUnitTestConvenience
    public void setReportFileTracker(final BiConsumer<String, File> reportFileTracker) {
        this.reportFileTracker = reportFileTracker;
    }

}
