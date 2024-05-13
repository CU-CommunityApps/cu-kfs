package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.util.EnumConfiguredMappingStrategy;
import edu.cornell.kfs.vnd.CUVendorConstants;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorEmployeeSearchCsv;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeSearchService;
import edu.cornell.kfs.vnd.businessobject.VendorWithSSN;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

public class VendorEmployeeSearchServiceImpl implements VendorEmployeeSearchService {

    private static final Logger LOG = LogManager.getLogger();

    private String csvEmployeeComparisonFileCreationDirectory;
    private String csvEmployeeComparisonFileExportDirectory;
    private CuVendorDao vendorDao;
    private DateTimeService dateTimeService;

    @Override
    public void generateFileContainingPotentialVendorEmployees() {
        final String csvFileName = generateEmployeeComparisonCsvOutboundFileName();
        final String qualifiedCsvFileName = fullyQualifyEmployeeComparisonCsvOutboundFileName(csvFileName);

        LOG.info("generateFileContainingPotentialVendorEmployees, Creating employee comparison file: {}", csvFileName);
        final int ssnVendorCount = writeEmployeeComparisonOutboundCSVFile(qualifiedCsvFileName);
        LOG.info("generateFileContainingPotentialVendorEmployees, Successfully created employee comparison file "
                + "containing {} data rows: {}", ssnVendorCount, csvFileName);
    }

    private String generateEmployeeComparisonCsvOutboundFileName() {
        final Date currentDate = dateTimeService.getCurrentDate();
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US);
        final String currentDateString = dateFormatter.format(currentDate.toInstant());
        return StringUtils.join(CUVendorConstants.EMPLOYEE_COMPARISON_OUTBOUND_FILE_PREFIX, currentDateString,
                FileExtensions.CSV);
    }

    private String fullyQualifyEmployeeComparisonCsvOutboundFileName(final String fileName) {
        return csvEmployeeComparisonFileCreationDirectory + fileName;
    }

    private int writeEmployeeComparisonOutboundCSVFile(final String qualifiedCsvFileName) {
        try (
                final Stream<VendorWithSSN> ssnVendors = vendorDao.getPotentialEmployeeVendorsAsCloseableStream();
                final FileOutputStream fileStream = new FileOutputStream(qualifiedCsvFileName);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
                final BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
        ) {
            final int ssnVendorCount = writeSSNVendorsToCSVFile(ssnVendors, bufferedWriter);
            bufferedWriter.flush();
            return ssnVendorCount;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int writeSSNVendorsToCSVFile(final Stream<VendorWithSSN> ssnVendors,
            final BufferedWriter bufferedWriter) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        final EnumConfiguredMappingStrategy<VendorWithSSN, VendorEmployeeSearchCsv> mappingStrategy =
                new EnumConfiguredMappingStrategy<>(VendorEmployeeSearchCsv.class,
                        VendorEmployeeSearchCsv::getHeaderLabel, VendorEmployeeSearchCsv::getVendorDtoPropertyName);
        mappingStrategy.setType(VendorWithSSN.class);

        final StatefulBeanToCsv<VendorWithSSN> csvWriter = new StatefulBeanToCsvBuilder<VendorWithSSN>(bufferedWriter)
                .withMappingStrategy(mappingStrategy)
                .build();

        int ssnVendorCount = 0;
        for (VendorWithSSN ssnVendor : IteratorUtils.asIterable(ssnVendors.iterator())) {
            csvWriter.write(ssnVendor);
            ssnVendorCount++;
        }

        return ssnVendorCount;
    }

    public void setVendorDao(final CuVendorDao vendorDao) {
        this.vendorDao = vendorDao;
    }

    public void setCsvEmployeeComparisonFileCreationDirectory(
            final String csvEmployeeComparisonFileCreationDirectory) {
        this.csvEmployeeComparisonFileCreationDirectory = csvEmployeeComparisonFileCreationDirectory;
    }

    public void setCsvEmployeeComparisonFileExportDirectory(final String csvEmployeeComparisonFileExportDirectory) {
        this.csvEmployeeComparisonFileExportDirectory = csvEmployeeComparisonFileExportDirectory;
    }

}
