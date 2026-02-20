package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.service.impl.CemiFileWriter;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;
import edu.cornell.kfs.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.vnd.batch.service.CemiSupplierExtractService;
import edu.cornell.kfs.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

public class CemiSupplierExtractServiceImpl implements CemiSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private String supplierFileCreationDirectory;
    private String supplierFileExportDirectory;
    private CuVendorDao cuVendorDao;
    private CemiVendorDao cemiVendorDao;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable Vendors from the previous run (if present)...");
        cemiVendorDao.clearExistingListOfExtractableVendorIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeVendorActivityDateRangeSettings() {
        LOG.info("initializeVendorActivityDateRangeSettings, Setting from/to date range from parameter value...");
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CreateCemiSupplierExtractStep.class, CuVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);
        final String[] dateStrings = parameterValues.toArray(String[]::new);
        Validate.validState(dateStrings.length == 2, "Parameter %s should have had 2 values, but had %s instead",
                CuVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE, dateStrings.length);

        final LocalDate fromDate = parseDate(dateStrings[0]);
        final LocalDate toDate = parseDate(dateStrings[1]);
        Validate.validState(fromDate.compareTo(toDate) <= 0,
                "Parameter %s contained a 'from' date that is later than the 'to' date",
                CuVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);

        cemiVendorDao.updateSupplierExtractQuerySettings(fromDate, toDate);
    }

    private LocalDate parseDate(final String value) {
        try {
            return dateTimeService.convertToLocalDate(value);
        } catch (final ParseException e) {
            LOG.error("parseDate, failed to parse date string: {}", value, e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeVendors() {
        LOG.info("populateListOfInScopeVendors, Querying and storing the list of extractable Vendors...");
        cemiVendorDao.queryAndStoreVendorIdsForSupplierExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateSupplierExtractFile() {
        try {
            LOG.info("generateSupplierExtractFile, Starting creation of CEMI Supplier Extract file...");
            final String newFileName = generateSupplierExtractFileName();
            final File tempFile = qualifyAndGetFilePath(supplierFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(supplierFileExportDirectory, newFileName);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);
            Validate.validState(!finalFile.exists(), "Final file already exists: %s", newFileName);

            LOG.info("generateSupplierExtractFile, Copying template file...");
            copyTemplateFileTo(tempFile);

            LOG.info("generateSupplierExtractFile, Updating copied template with supplier data...");
            updateSupplierExtractFile(tempFile);

            LOG.info("generateSupplierExtractFile, Moving file to export folder...");
            moveSupplierExtractFileToExportDirectory(tempFile, finalFile);

            LOG.info("generateSupplierExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateSupplierExtractFile, Creation of Supplier Extract file failed", e);
            throw new RuntimeException(e);
        }
    }

    private String generateSupplierExtractFileName() {
        final LocalDateTime currentDateTime = dateTimeService.getLocalDateTimeNow();
        final DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
                .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));
        final String currentDateString = dateFormatter.format(currentDateTime);
        return StringUtils.join(CemiVendorConstants.SUPPLIER_EXTRACT_FILENAME_PREFIX, currentDateString,
                FileExtensions.XLSX);
    }

    private File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }

    private void copyTemplateFileTo(final File newFile) throws IOException {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiVendorConstants.SUPPLIER_TEMPLATE_FILE_PATH);
            final OutputStream outputStream = new FileOutputStream(newFile);
        ) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    private void updateSupplierExtractFile(final File file) throws IOException, InvalidFormatException {
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForSupplierExtract();

        try (
            final CemiFileWriter writer = new CemiFileWriter(outputDefinition, file, false);
            final Stream<VendorDetail> vendors = cuVendorDao.getVendorsForCemiSupplierExtractAsCloseableStream();
        ) {
            final Iterator<VendorDetail> vendorsIterator = vendors.iterator();
            final CemiSupplierFileAppender supplierFileAppender = new CemiSupplierFileAppender(writer, vendorsIterator);
            supplierFileAppender.writeVendorsToFile();
            LOG.info("updateSupplierExtractFile, Wrote {} Vendors to the Supplier Extract file",
                    supplierFileAppender.getVendorCount());
        }
    }

    private CemiOutputDefinition getOutputDefinitionForSupplierExtract() throws IOException {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiVendorConstants.SUPPLIER_OUTPUT_DEFINITION_FILE_PATH);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
        }
    }

    private void moveSupplierExtractFileToExportDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path exportFilePath = targetFile.toPath();
            Files.move(creationFilePath, exportFilePath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.error("moveSupplierExtractFileToExportDirectory, Failed to move file to export directory", e);
            throw new UncheckedIOException(e);
        }
    }

    public void setSupplierFileCreationDirectory(final String supplierFileCreationDirectory) {
        this.supplierFileCreationDirectory = supplierFileCreationDirectory;
    }

    public void setSupplierFileExportDirectory(final String supplierFileExportDirectory) {
        this.supplierFileExportDirectory = supplierFileExportDirectory;
    }

    public void setCuVendorDao(final CuVendorDao cuVendorDao) {
        this.cuVendorDao = cuVendorDao;
    }

    public void setCemiVendorDao(final CemiVendorDao cemiVendorDao) {
        this.cemiVendorDao = cemiVendorDao;
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
