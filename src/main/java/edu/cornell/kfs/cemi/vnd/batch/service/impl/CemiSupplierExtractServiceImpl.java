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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
import edu.cornell.kfs.cemi.sys.CemiKFSConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDaoJdbc;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDaoOjb;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

@SuppressWarnings("deprecation")
public class CemiSupplierExtractServiceImpl implements CemiSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;

    private String supplierFileCreationDirectory;
    private String supplierFileOutboundDirectory;
    private CemiVendorDaoOjb cemiVendorDaoOjb;
    private CemiVendorDaoJdbc cemiVendorDaoJdbc;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;

    public CemiSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable Vendors from the previous run (if present)...");
        getCemiVendorDaoJdbc().clearExistingListOfBaseVendorData();
        getCemiVendorDaoJdbc().clearExistingListOfExtractableVendorIds();
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

        getCemiVendorDaoJdbc().updateSupplierExtractQuerySettings(fromDate, toDate);
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
        getCemiVendorDaoJdbc().prepareBaseVendorDataNeededForMainVendorIdQuery();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeVendors() {
        LOG.info("populateListOfInScopeVendors, Querying and storing the list of extractable Vendors...");
        cemiVendorDaoJdbc.queryAndStoreVendorIdsForSupplierExtract();
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
            final CemiSupplierDataBuilderCsvImpl dataBuilder = new CemiSupplierDataBuilderCsvImpl(
                    getOutputDefinitionForSupplierExtract(), getCemiVendorDaoJdbc(), jobRunDate, supplierFileCreationDirectory, 
                    shouldMaskCemiSensitiveData());
            final Stream<VendorDetail> vendors = getCemiVendorDaoOjb().getVendorsForCemiSupplierExtractAsCloseableStream();
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
            final CemiSupplierFileAppenderCsvImpl supplierFileAppender = new CemiSupplierFileAppenderCsvImpl(
                outputDefinition, jobRunDate, supplierFileCreationDirectory);
            supplierFileAppender.populateSupplierFileFromIntermediateDataStorage(writer);
            writer.commit();
            supplierFileAppender.cleanUpIntermediateStorage();
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

    public void setSupplierFileCreationDirectory(final String supplierFileCreationDirectory) {
        this.supplierFileCreationDirectory = supplierFileCreationDirectory;
    }

    public void setSupplierFileOutboundDirectory(final String supplierFileOutboundDirectory) {
        this.supplierFileOutboundDirectory = supplierFileOutboundDirectory;
    }


    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
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

    public CemiVendorDaoJdbc getCemiVendorDaoJdbc() {
        return cemiVendorDaoJdbc;
    }

    public void setCemiVendorDaoJdbc(CemiVendorDaoJdbc cemiVendorDaoJdbc) {
        this.cemiVendorDaoJdbc = cemiVendorDaoJdbc;
    }

    public CemiVendorDaoOjb getCemiVendorDaoOjb() {
        return cemiVendorDaoOjb;
    }

    public void setCemiVendorDaoOjb(CemiVendorDaoOjb cemiVendorDaoOjb) {
        this.cemiVendorDaoOjb = cemiVendorDaoOjb;
    }

}
