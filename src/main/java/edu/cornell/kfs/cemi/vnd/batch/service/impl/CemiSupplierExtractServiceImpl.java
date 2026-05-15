package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.impl.CemiWorkbookOrmHandler;
import edu.cornell.kfs.cemi.sys.batch.service.CemiOutputDefinitionService;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelReader;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

@SuppressWarnings("deprecation")
public class CemiSupplierExtractServiceImpl implements CemiSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;

    private String stagingDirectory;
    private String supplierFileCreationDirectory;
    private String supplierFileOutboundDirectory;
    private CemiVendorOrmDao cemiVendorOrmDao;
    private CemiVendorDao cemiVendorDao;
    private CemiSheetDao cemiSheetDao;
    private CemiOutputDefinitionService cemiOutputDefinitionService;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;

    public CemiSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean performOneTimeCopyOfWorkbookToDatabaseIfNecessary() {
        final String dateRangeParameter = parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);
        if (!StringUtils.contains(dateRangeParameter, CUKFSConstants.EQUALS_SIGN)) {
            return false;
        }

        LOG.info("performOneTimeCopyOfWorkbookToDatabaseIfNecessary, Date range parameter has a 'key=value' string; "
                + "will treat it as a datetime-and-filepath pair, cancel normal processing and instead copy a prior "
                + "run's existing Supplier Extract sheet data rows into the equivalent database tables");

        final String runDateString = StringUtils.substringBefore(dateRangeParameter, CUKFSConstants.EQUALS_SIGN);
        final String workbookFilePath = StringUtils.substringAfter(dateRangeParameter, CUKFSConstants.EQUALS_SIGN);

        LOG.info("performOneTimeCopyOfWorkbookToDatabaseIfNecessary, Extracted data will be mapped to the following "
                + "run-datetime string: {}", runDateString);
        LOG.info("performOneTimeCopyOfWorkbookToDatabaseIfNecessary, Data will be extracted from the following "
                + "workbook (relative to the 'staging' directory): {}", workbookFilePath);

        extractSupplierWorkbookToDatabase(runDateString, workbookFilePath);

        LOG.info("performOneTimeCopyOfWorkbookToDatabaseIfNecessary, Success! Workbook data has been fully processed");
        return true;
    }

    private void extractSupplierWorkbookToDatabase(final String jobRunDate, final String workbookFilePath) {
        Validate.validState(CemiUtils.isFormattedAsValidFilePath(workbookFilePath),
                "Workbook file path is blank or malformed");
        final String fullWorkbookPath = StringUtils.join(stagingDirectory, CUKFSConstants.SLASH, workbookFilePath);
        final File workbookFile = new File(fullWorkbookPath);
        Validate.validState(workbookFile.exists(), "Workbook file not found: %s", workbookFilePath);

        final CemiOutputDefinition outputDefinition = getOutputDefinitionForSupplierExtract();
        final CemiWorkbookOrmHandler ormHandler = new CemiWorkbookOrmHandler(
                outputDefinition, businessObjectService, cemiSheetDao);
        final TriConsumer<String, Integer, String[]> rowDataHandler = (sheetName, rowIndex, rowData) -> {
            ormHandler.storeArrayBasedSheetRow(sheetName, rowData, jobRunDate, rowIndex + 1L);
        };

        try (
            final CemiExcelReader excelReader = new CemiExcelReader(workbookFile, outputDefinition, rowDataHandler);
        ) {
            excelReader.parse();
        } catch (final Exception e) {
            LOG.error("extractSupplierWorkbookToDatabase, Failed to extract workbook", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable Vendors from the previous run (if present)...");
        getCemiVendorDao().clearExistingListOfBaseVendorData();
        getCemiVendorDao().clearExistingListOfExtractableVendorIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeVendorActivityDateRangeSettings() {
        LOG.info("initializeVendorActivityDateRangeSettings, Setting from/to date range from parameter value...");
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);
        final String[] dateStrings = parameterValues.toArray(String[]::new);
        Validate.validState(dateStrings.length == 2, "Parameter %s should have had 2 values, but had %s instead",
                CemiVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE, dateStrings.length);

        final LocalDate fromDate = parseDate(dateStrings[0]);
        final LocalDate toDate = parseDate(dateStrings[1]);
        Validate.validState(fromDate.compareTo(toDate) <= 0,
                "Parameter %s contained a 'from' date that is later than the 'to' date",
                CemiVendorParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);

        getCemiVendorDao().updateSupplierExtractQuerySettings(fromDate, toDate);
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
    public void populateListOfBaseVendorData() {
        LOG.info("populateListOfBaseVendorData, Preparing base Vendor data needed for subsequent Vendor query...");
        getCemiVendorDao().prepareBaseVendorDataNeededForMainVendorIdQuery();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeVendors() {
        LOG.info("populateListOfInScopeVendors, Querying and storing the list of extractable Vendors...");
        getCemiVendorDao().queryAndStoreVendorIdsForSupplierExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateSupplierExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateSupplierExtractData, Generating data rows for Supplier spreadsheet "
                + "and placing in intermediate storage...");
        try {
            final CemiOutputDefinition outputDefinition = getOutputDefinitionForSupplierExtract();
            generateSupplierExtractData(outputDefinition, jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateSupplierExtractData, Creation of Supplier Extract data failed", e);
            throw new RuntimeException(e);
        }
    }

    private void generateSupplierExtractData(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate) throws IOException {
        try (
            // Replace this builder with a temp table implementation when ready.
            //final CemiSupplierDataBuilderCsvImpl dataBuilder = new CemiSupplierDataBuilderCsvImpl(
            //        getOutputDefinitionForSupplierExtract(), getCemiVendorDao(), jobRunDate, supplierFileCreationDirectory, 
            //        shouldMaskCemiSensitiveData());
            final CemiSupplierDataBuilderOrmImpl dataBuilder = new CemiSupplierDataBuilderOrmImpl(
                    outputDefinition, cemiVendorDao, jobRunDate,
                    shouldMaskCemiSensitiveData(), businessObjectService, cemiSheetDao);
            final Stream<VendorDetail> vendors = getCemiVendorOrmDao().getVendorsForCemiSupplierExtractAsCloseableStream();
        ) {
            final Iterator<VendorDetail> vendorsIterator = vendors.iterator();
            dataBuilder.writeSupplierDataToIntermediateStorage(
                    vendorsIterator, this::findAllActiveAccountsForVendor, jobRunDate);
        }
    }

    private Collection<PayeeACHAccount> findAllActiveAccountsForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier) throws IOException {
        final String vendorId = StringUtils.join(
                vendorHeaderGeneratedIdentifier, KFSConstants.DASH, vendorDetailAssignedIdentifier);
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(PdpPropertyConstants.PAYEE_ID_NUMBER, vendorId),
                Map.entry(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.VENDOR_ID),
                Map.entry(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR)
        );
        return businessObjectService.findMatchingOrderBy(
                PayeeACHAccount.class, criteria, PdpPropertyConstants.ACH_ACCOUNT_GENERATED_IDENTIFIER, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateSupplierExtractFile(final LocalDateTime jobRunDate) {
        try {
            LOG.info("generateSupplierExtractFile, Starting creation of CEMI Supplier Extract file...");
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(
                    jobRunDate, CemiVendorConstants.SUPPLIER_EXTRACT_FILENAME_PREFIX, FileExtensions.XLSX);
            final File tempFile = qualifyAndGetFilePath(supplierFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(
                    supplierFileOutboundDirectory, CemiVendorConstants.SUPPLIER_EXTRACT_PLAIN_FILENAME);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateSupplierExtractFile(tempFile, jobRunDate);

            if (shouldCopySupplierExtractFileToOutboundDirectory()) {
                LOG.info("generateSupplierExtractFile, Copying file to outbound folder under the Supplier.xlsx name...");
                copySupplierExtractFileToOutboundDirectory(tempFile, finalFile);
            } else {
                LOG.info("generateSupplierExtractFile, Copying of the file to the outbound folder has been disabled. "
                        + "The copying operation will be skipped.");
            }

            LOG.info("generateSupplierExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateSupplierExtractFile, Creation of Supplier Extract file failed", e);
            throw new RuntimeException(e);
        }
    }

    private File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }

    private void createAndPopulateSupplierExtractFile(final File file, final LocalDateTime jobRunDate)
            throws IOException, InvalidFormatException {
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForSupplierExtract();

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiVendorConstants.SUPPLIER_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            // Replace this appender with a temp table implementation when ready.
            //final CemiSupplierFileAppenderCsvImpl supplierFileAppender = new CemiSupplierFileAppenderCsvImpl(
            //        outputDefinition, jobRunDate, supplierFileCreationDirectory);
            final CemiSupplierFileAppenderOrmImpl supplierFileAppender = new CemiSupplierFileAppenderOrmImpl(
                    outputDefinition, jobRunDate, businessObjectService, cemiSheetDao);
            supplierFileAppender.populateSupplierFileFromIntermediateDataStorage(writer);
            writer.commit();
            supplierFileAppender.cleanUpIntermediateStorage();
        }
    }

    private CemiOutputDefinition getOutputDefinitionForSupplierExtract() {
        return cemiOutputDefinitionService.getCemiOutputDefinition(
                CemiVendorConstants.VENDOR_MODULE_PATH, CemiVendorConstants.SUPPLIER_OUTPUT_DEFINITION_NAME);
    }

    private boolean shouldCopySupplierExtractFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.COPY_CEMI_SUPPLIER_FILE_TO_OUTBOUND_FOLDER);
    }

    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
        return StringUtils.equalsIgnoreCase(maskingParameterValue, CemiBaseConstants.UNMASK);
    }

    private boolean isCemiEnvironment() {
        return StringUtils.equalsIgnoreCase(environment.getLane(), CemiBaseConstants.CEMI_ENVIRONMENT_LANE_NAME);
    }

    private boolean shouldUnmaskCemiSensitiveData() {
        return isCemiEnvironment() && isCemiSensitiveDataSetToUnmask();
    }

    private boolean shouldMaskCemiSensitiveData() {
        return !shouldUnmaskCemiSensitiveData();
    }

    private void copySupplierExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("copySupplierExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }

    public void setStagingDirectory(final String stagingDirectory) {
        this.stagingDirectory = stagingDirectory;
    }

    public void setSupplierFileCreationDirectory(final String supplierFileCreationDirectory) {
        this.supplierFileCreationDirectory = supplierFileCreationDirectory;
    }

    public void setSupplierFileOutboundDirectory(final String supplierFileOutboundDirectory) {
        this.supplierFileOutboundDirectory = supplierFileOutboundDirectory;
    }


    public void setCemiOutputDefinitionService(final CemiOutputDefinitionService cemiOutputDefinitionService) {
        this.cemiOutputDefinitionService = cemiOutputDefinitionService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public CemiVendorDao getCemiVendorDao() {
        return cemiVendorDao;
    }

    public void setCemiVendorDao(CemiVendorDao cemiVendorDao) {
        this.cemiVendorDao = cemiVendorDao;
    }

    public CemiVendorOrmDao getCemiVendorOrmDao() {
        return cemiVendorOrmDao;
    }

    public void setCemiVendorOrmDao(CemiVendorOrmDao cemiVendorOrmDao) {
        this.cemiVendorOrmDao = cemiVendorOrmDao;
    }

    public void setCemiSheetDao(final CemiSheetDao cemiSheetDao) {
        this.cemiSheetDao = cemiSheetDao;
    }

}
